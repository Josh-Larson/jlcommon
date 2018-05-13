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

import me.joshlarson.jlcommon.annotations.Unused;
import me.joshlarson.jlcommon.concurrency.BasicThread;
import me.joshlarson.jlcommon.concurrency.ThreadPool;
import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.jlcommon.network.TCPServer.TCPSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class TCPServer<T extends TCPSession> {
	
	private final ThreadPool callbackThread;
	private final Map<SocketChannel, T> channels;
	private final Map<Long, T> sessionIdToChannel;
	
	private final BasicThread listener;
	private final AtomicBoolean running;
	private final InetSocketAddress addr;
	private final Function<SocketChannel, T> sessionCreator;
	private ServerSocketChannel channel;
	
	private final ByteBuffer buffer;
	private final ByteArrayOutputStream bufferStream;
	private final WritableByteChannel byteBufferChannel;
	
	public TCPServer(int port, int bufferSize, @NotNull Function<SocketChannel, T> sessionCreator) {
		this(new InetSocketAddress((InetAddress) null, port), bufferSize, sessionCreator);
	}
	
	public TCPServer(@NotNull InetSocketAddress addr, int bufferSize, @NotNull Function<SocketChannel, T> sessionCreator) {
		this.callbackThread = new ThreadPool(false, 1, "tcpserver-" + addr.getPort());
		this.channels = new ConcurrentHashMap<>();
		this.sessionIdToChannel = new ConcurrentHashMap<>();
		this.listener = new BasicThread("tcpserver-listener-" + addr.getPort(), this::runListener);
		this.running = new AtomicBoolean(false);
		this.addr = addr;
		this.channel = null;
		this.sessionCreator = sessionCreator;
		this.buffer = ByteBuffer.allocateDirect(bufferSize);
		this.bufferStream = new ByteArrayOutputStream(bufferSize);
		this.byteBufferChannel = Channels.newChannel(bufferStream);
	}
	
	public int getPort() {
		return channel.socket().getLocalPort();
	}
	
	public void bind() throws IOException {
		bind(50);
	}
	
	public void bind(int backlog) throws IOException {
		assert !running.get() : "TCPServer is already running";
		if (running.getAndSet(true))
			return;
		callbackThread.start();
		channel = ServerSocketChannel.open();
		channel.bind(addr, backlog);
		channel.configureBlocking(false);
		listener.start();
	}
	
	public void disconnect(long sessionId) {
		T session = sessionIdToChannel.remove(sessionId);
		if (session == null) {
			Log.w("TCPServer - unknown session id in disconnect: %d", sessionId);
			return;
		}
		
		disconnect(session.getChannel());
	}
	
	public void disconnect(@NotNull T session) {
		disconnect(session.getChannel());
	}
	
	public void disconnect(@NotNull SocketChannel sc) {
		T session = channels.remove(sc);
		if (session == null) {
			Log.w("TCPServer - unknown channel in disconnect: %d", sc);
			return;
		}
		sessionIdToChannel.remove(session.getSessionId());
		
		session.close();
		callbackThread.execute(session::onDisconnected);
	}
	
	@Nullable
	public T getSession(long sessionId) {
		return sessionIdToChannel.get(sessionId);
	}
	
	@Nullable
	public T getSession(@NotNull SocketChannel sc) {
		return channels.get(sc);
	}
	
	public void close() {
		assert running.get() : "TCPServer isn't running";
		if (!running.getAndSet(false))
			return;
		callbackThread.stop(false);
		listener.stop(true);
		safeClose(channel);
	}
	
	private void runListener() {
		try (Selector selector = Selector.open()) {
			channel.register(selector, SelectionKey.OP_ACCEPT);
			while (running.get()) {
				selector.select();
				accept(selector);
				selector.selectedKeys().forEach(this::read);
			}
		} catch (IOException e) {
			Log.e(e);
		}
	}
	
	private void accept(@NotNull Selector selector) {
		try {
			while (channel.isOpen()) {
				SocketChannel sc = channel.accept();
				if (sc == null)
					return;
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
				acceptConnection(sc);
			}
		} catch (ClosedChannelException e) {
			// Ignored
		} catch (Throwable t) {
			Log.w("TCPServer - IOException in accept(): %s", t.getMessage());
		}
	}
	
	private void acceptConnection(@NotNull SocketChannel sc) {
		T session = sessionCreator.apply(sc);
		if (session == null) {
			Log.w("Session creator for TCPServer-%d created a null session!", addr.getPort());
			safeClose(sc);
			return;
		}
		if (session.getChannel() != sc) {
			Log.w("Session creator for TCPServer-%d created a session with an invalid channel!", addr.getPort());
			safeClose(sc);
			return;
		}
		channels.put(sc, session);
		sessionIdToChannel.put(session.getSessionId(), session);
		callbackThread.execute(session::onConnected);
	}
	
	private void read(@NotNull SelectionKey key) {
		SelectableChannel selectableChannel = key.channel();
		if (selectableChannel == channel)
			return;
		SocketChannel sc = (SocketChannel) selectableChannel;
		T session = getSession(sc);
		if (session == null || !sc.isConnected()) {
			invalidate(sc, key);
			return;
		}
		try {
			bufferStream.reset();
			int n = 1;
			while (n > 0) {
				buffer.clear();
				n = sc.read(buffer);
				buffer.flip();
				byteBufferChannel.write(buffer);
			}
			if (bufferStream.size() > 0) {
				byte[] data = bufferStream.toByteArray();
				callbackThread.execute(() -> session.onIncomingData(data));
			}
			if (n < 0) {
				invalidate(sc, key);
			}
		} catch (ClosedChannelException e) {
			// Ignored
		} catch (Throwable t) {
			Log.w("TCPServer - IOException in read(): %s", t.getMessage());
			invalidate(sc, key);
		}
	}
	
	private void invalidate(@NotNull SocketChannel sc, @NotNull SelectionKey key) {
		key.cancel();
		disconnect(sc);
	}
	
	private static void safeClose(@NotNull Channel c) {
		try {
			c.close();
		} catch (IOException e) {
			// Ignored - as long as it's closed
		}
	}
	
	public abstract static class TCPSession {
		
		private static final AtomicLong GLOBAL_SESSION_ID = new AtomicLong(0);
		
		private final SocketChannel sc;
		private final SocketAddress addr;
		private final long sessionId;
		
		protected TCPSession(@NotNull SocketChannel sc) {
			this.sc = sc;
			this.sessionId = GLOBAL_SESSION_ID.incrementAndGet();
			
			SocketAddress addr;
			try {
				addr = sc.getRemoteAddress();
			} catch (IOException e) {
				addr = null;
			}
			this.addr = addr;
		}
		
		protected abstract void onConnected();
		
		protected abstract void onDisconnected();
		
		/**
		 * Returns a globally unique session id for this particular connection
		 *
		 * @return the unique session id
		 */
		protected final long getSessionId() {
			return sessionId;
		}
		
		/**
		 * Returns the socket channel associated with this session
		 *
		 * @return the socket channel
		 */
		@NotNull
		protected final SocketChannel getChannel() {
			return sc;
		}
		
		/**
		 * Returns the remote address that this socket is/was connected to
		 *
		 * @return the remote socket address
		 */
		@Unused(reason = "API")
		@NotNull
		protected final SocketAddress getRemoteAddress() {
			return addr;
		}
		
		@Unused(reason = "API")
		protected void writeToChannel(@NotNull ByteBuffer data) throws IOException {
			sc.write(data);
		}
		
		@Unused(reason = "API")
		protected void writeToChannel(@NotNull byte[] data) throws IOException {
			sc.write(ByteBuffer.wrap(data));
		}
		
		protected void close() {
			safeClose(sc);
		}
		
		protected abstract void onIncomingData(@NotNull byte[] data);
	}
	
}
