/***********************************************************************************
 * MIT License                                                                     *
 *                                                                                 *
 * Copyright (c) 2018 Josh Larson                                                  *
 *                                                                                 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy    *
 * of this software and associated documentation files (the "Software"), to deal   *
 * in the Software without restriction, including without limitation the rights    *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell       *
 * copies of the Software, and to permit persons to whom the Software is           *
 * furnished to do so, subject to the following conditions:                        *
 *                                                                                 *
 * The above copyright notice and this permission notice shall be included in all  *
 * copies or substantial portions of the Software.                                 *
 *                                                                                 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR      *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,        *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE     *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER          *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,   *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE   *
 * SOFTWARE.                                                                       *
 ***********************************************************************************/
package me.joshlarson.jlcommon.network;

import me.joshlarson.jlcommon.callback.CallbackManager;
import me.joshlarson.jlcommon.concurrency.Delay;
import me.joshlarson.jlcommon.log.Log;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class TCPSocket {
	
	private static final Pattern SOCKET_CLOSED_MESSAGE = Pattern.compile(".*socket.+closed.*", Pattern.CASE_INSENSITIVE);
	
	private final CallbackManager<TCPSocketCallback> callbackManager;
	private final TCPSocketListener listener;
	private final AtomicReference<InetSocketAddress> address;
	private final AtomicReference<SocketState> state;
	private final Object stateMutex;
	private final int bufferSize;
	private Socket socket;
	private InputStream socketInputStream;
	private OutputStream socketOutputStream;
	
	public TCPSocket(InetSocketAddress address, int bufferSize) {
		this.callbackManager = new CallbackManager<>("tcpsocket-"+address, 1);
		this.listener = new TCPSocketListener();
		this.address = new AtomicReference<>(address);
		this.state = new AtomicReference<>(SocketState.CLOSED);
		this.stateMutex = new Object();
		this.bufferSize = bufferSize;
		
		this.socket = null;
		this.socketInputStream = null;
		this.socketOutputStream = null;
	}
	
	public TCPSocket(int bufferSize) {
		this(null, bufferSize);
	}
	
	public TCPSocket() {
		this(1024);
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public InetSocketAddress getRemoteAddress() {
		return address.get();
	}
	
	public void setRemoteAddress(InetSocketAddress address) {
		this.address.set(address);
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public boolean isAlive() {
		synchronized (stateLock()) {
			return socket != null && listener.isAlive();
		}
	}
	
	public boolean isConnected() {
		synchronized (stateLock()) {
			return socket != null && socket.isConnected();
		}
	}
	
	public void setCallback(TCPSocketCallback callback) {
		callbackManager.setCallback(callback);
	}
	
	public void removeCallback() {
		callbackManager.clearCallbacks();
	}
	
	public void createConnection() {
		synchronized (stateLock()) {
			checkAndSetState(SocketState.CLOSED, SocketState.CREATED);
			try {
				socket = createSocket();
			} catch (IOException e) {
				socket = null;
				checkAndSetState(SocketState.CREATED, SocketState.CLOSED);
				throw new RuntimeException(e);
			}
		}
	}
	
	public void startConnection() throws IOException {
		synchronized (stateLock()) {
			try {
				checkAndSetState(SocketState.CREATED, SocketState.CONNECTING);
				socket.connect(getRemoteAddress());
				socketInputStream = socket.getInputStream();
				socketOutputStream = socket.getOutputStream();
			} catch (IOException e) {
				checkAndSetState(SocketState.CONNECTING, SocketState.CLOSED);
				socket = null;
				socketInputStream = null;
				socketOutputStream = null;
				throw e;
			}
			
			callbackManager.start();
			listener.start();
			checkAndSetState(SocketState.CONNECTING, SocketState.CONNECTED);
			callbackManager.callOnEach((callback) -> callback.onConnected(this));
		}
	}
	
	public void connect() throws IOException {
		createConnection();
		startConnection();
	}
	
	public boolean disconnect() {
		synchronized (stateLock()) {
			if (socket == null)
				return true;
			try {
				checkAndSetState(SocketState.CONNECTED, SocketState.CLOSED);
				socket.close();
				socket = null;
				socketInputStream = null;
				socketOutputStream = null;
				
				if (listener.isAlive()) {
					listener.stop();
					listener.awaitTermination();
				}
				
				if (callbackManager.isRunning()) {
					callbackManager.callOnEach((callback) -> callback.onDisconnected(this));
					callbackManager.stop();
				}
				return true;
			} catch (IOException e) {
				Log.e(e);
			}
			return false;
		}
	}
	
	public boolean send(ByteBuffer data) {
		return send(data.array(), data.position(), data.remaining());
	}
	
	public boolean send(byte [] data) {
		return send(data, 0, data.length);
	}
	
	public boolean send(byte [] data, int offset,  int length) {
		synchronized (stateLock()) {
			try {
				if (socket == null)
					return false;
				
				if (length > 0)
					socketOutputStream.write(data, offset, length);
				
				return true;
			} catch (IOException e) {
				Log.e(e);
			}
			return false;
		}
	}
	
	protected Socket createSocket() throws IOException {
		return new Socket();
	}
	
	protected final Object stateLock() {
		return stateMutex;
	}
	
	/**
	 * Checks the current state to see if it matches the expected, and if so, changes it to the new state. If not, it fails the assertion
	 * @param expected the expected state
	 * @param state the new state
	 */
	private void checkAndSetState(SocketState expected, SocketState state) {
		Objects.requireNonNull(expected, "Expected state cannot be null!");
		Objects.requireNonNull(state, "New state cannot be null!");
		if (!this.state.compareAndSet(expected, state))
			Log.w("Failed to set state! Was: %s  Expected: %s  Update: %s", this.state.get(), expected, state);
	}
	
	public interface TCPSocketCallback {
		void onConnected(TCPSocket socket);
		void onDisconnected(TCPSocket socket);
		void onIncomingData(TCPSocket socket, byte[] data);
	}
	
	private enum SocketState {
		CLOSED,
		CREATED,
		CONNECTING,
		CONNECTED
	}
	
	private class TCPSocketListener implements Runnable {
		
		private final AtomicBoolean running;
		private final AtomicBoolean alive;
		
		private Thread thread;
		
		public TCPSocketListener() {
			this.running = new AtomicBoolean(false);
			this.alive = new AtomicBoolean(false);
			this.thread = null;
		}
		
		public void start() {
			if (running.get() || thread != null)
				throw new IllegalStateException("Cannot start listener! Already started");
			thread = new Thread(this, "TCPServer Port#" + getRemoteAddress().getPort());
			running.set(true);
			thread.start();
		}
		
		public void stop() {
			if (!running.get() || thread == null)
				throw new IllegalStateException("Cannot stop listener! Already stopped");
			running.set(false);
			if (thread != null)
				thread.interrupt();
			thread = null;
		}
		
		public void awaitTermination() {
			while (isAlive()) {
				if (!Delay.sleepMicro(5))
					break;
			}
		}
		
		public boolean isAlive() {
			return alive.get();
		}
		
		@Override
		public void run() {
			try {
				alive.set(true);
				InputStream input = TCPSocket.this.socketInputStream;
				byte[] buffer = new byte[TCPSocket.this.bufferSize];
				while (running.get()) {
					waitIncoming(input, buffer);
				}
			} catch (EOFException e) {
				// We're all fine here - just means the socket closed normally
			} catch (IOException e) {
				String message = e.getMessage();
				if (message != null && SOCKET_CLOSED_MESSAGE.matcher(message).matches())
					return;
				Log.e("IO exception within the TCP socket - terminating...");
				Log.e(e);
			} catch (Throwable t) {
				Log.e("Uncaught exception within the TCP socket - terminating...");
				Log.e(t);
			} finally {
				running.set(false);
				alive.set(false);
				thread = null;
				disconnect();
			}
		}
		
		private void waitIncoming(InputStream input, byte [] buffer) throws IOException {
			int n = input.read(buffer);
			if (n == 0)
				return;
			if (n < 0)
				throw new EOFException();
			byte [] data = new byte[n];
			System.arraycopy(buffer, 0, data, 0, n);
			callbackManager.callOnEach((callback) -> callback.onIncomingData(TCPSocket.this, data));
		}
		
	}
}
