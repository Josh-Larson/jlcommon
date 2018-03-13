package me.joshlarson.jlcommon.network;

import me.joshlarson.jlcommon.annotations.Unused;
import me.joshlarson.jlcommon.concurrency.BasicThread;
import me.joshlarson.jlcommon.concurrency.ThreadPool;
import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.jlcommon.network.TCPServer.TCPSession;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
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
	
	public TCPServer(int port, @Nonnegative int bufferSize, @Nonnull Function<SocketChannel, T> sessionCreator) {
		this(new InetSocketAddress((InetAddress) null, port), bufferSize, sessionCreator);
	}
	
	public TCPServer(@Nonnull InetSocketAddress addr, @Nonnegative int bufferSize, @Nonnull Function<SocketChannel, T> sessionCreator) {
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
	
	public void disconnect(@Nonnull T session) {
		disconnect(session.getChannel());
	}
	
	public void disconnect(@Nonnull SocketChannel sc) {
		T session = channels.remove(sc);
		if (session == null) {
			Log.w("TCPServer - unknown channel in disconnect: %d", sc);
			return;
		}
		sessionIdToChannel.remove(session.getSessionId());
		
		session.close();
		callbackThread.execute(session::onDisconnected);
	}
	
	@CheckForNull
	public T getSession(long sessionId) {
		return sessionIdToChannel.get(sessionId);
	}
	
	@CheckForNull
	public T getSession(@Nonnull SocketChannel sc) {
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
	
	private void accept(@Nonnull Selector selector) {
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
	
	private void acceptConnection(@Nonnull SocketChannel sc) {
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
	
	private void read(@Nonnull SelectionKey key) {
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
	
	private void invalidate(@Nonnull SocketChannel sc, @Nonnull SelectionKey key) {
		key.cancel();
		disconnect(sc);
	}
	
	private static void safeClose(@Nonnull Channel c) {
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
		
		protected TCPSession(@Nonnull SocketChannel sc) {
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
		@Nonnegative
		protected final long getSessionId() {
			return sessionId;
		}
		
		/**
		 * Returns the socket channel associated with this session
		 *
		 * @return the socket channel
		 */
		@Nonnull
		protected final SocketChannel getChannel() {
			return sc;
		}
		
		/**
		 * Returns the remote address that this socket is/was connected to
		 *
		 * @return the remote socket address
		 */
		@Unused(reason = "API")
		@Nonnull
		protected final SocketAddress getRemoteAddress() {
			return addr;
		}
		
		@Unused(reason = "API")
		protected void writeToChannel(@Nonnull ByteBuffer data) throws IOException {
			sc.write(data);
		}
		
		@Unused(reason = "API")
		protected void writeToChannel(@Nonnull byte[] data) throws IOException {
			sc.write(ByteBuffer.wrap(data));
		}
		
		protected void close() {
			safeClose(sc);
		}
		
		protected abstract void onIncomingData(@Nonnull byte[] data);
	}
	
}
