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

import me.joshlarson.jlcommon.concurrency.Delay;
import me.joshlarson.jlcommon.network.TCPServer.TCPSession;
import me.joshlarson.jlcommon.network.TCPSocket.TCPSocketCallback;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(JUnit4.class)
public class TestTCP {
	
	@Test
	public void testServerAndSocket() throws IOException {
		AtomicBoolean serverConnection = new AtomicBoolean(false);
		AtomicBoolean clientConnection = new AtomicBoolean(false);
		AtomicReference<TCPSession> sess = new AtomicReference<>(null);
		Queue<byte[]> dataQueue = new ArrayDeque<>();
		TCPServer<TCPSession> server = new TCPServer<>(0, 128, c -> new TCPSession(c) {
			protected void onConnected() { sess.set(this); serverConnection.set(true); }
			protected void onDisconnected() { serverConnection.set(false); }
			protected void onIncomingData(@NotNull byte[] data) { dataQueue.add(data); }
		});
		
		server.bind();
		
		TCPSocket socket = new TCPSocket(new InetSocketAddress(InetAddress.getLoopbackAddress(), server.getPort()), 128);
		socket.setCallback(new TCPSocketCallback() {
			public void onConnected(TCPSocket socket) { clientConnection.set(true); }
			public void onDisconnected(TCPSocket socket) { clientConnection.set(false); }
			public void onIncomingData(TCPSocket socket, byte[] data) { dataQueue.add(data); }
		});
		
		Assert.assertFalse(serverConnection.get());
		Assert.assertFalse(clientConnection.get());
		Assert.assertTrue(dataQueue.isEmpty());
		
		socket.connect();
		
		for (int i = 0; i < 100 && !clientConnection.get(); i++)
			Delay.sleepMilli(1); // Waiting for the concurrent callback
		
		Assert.assertNotNull(sess.get());
		Assert.assertTrue(serverConnection.get());
		Assert.assertTrue(clientConnection.get());
		Assert.assertTrue(dataQueue.isEmpty());
		
		// Client -> Server
		socket.send(new byte[]{1, 2, 3, 4});
		for (int i = 0; i < 100 && dataQueue.isEmpty(); i++)
			Delay.sleepMilli(1); // Waiting for the concurrent callback
		Assert.assertArrayEquals(new byte[]{1,2,3,4}, dataQueue.poll());
		
		// Server -> Client
		sess.get().writeToChannel(new byte[]{1, 2, 3, 4});
		for (int i = 0; i < 100 && dataQueue.isEmpty(); i++)
			Delay.sleepMilli(1); // Waiting for the concurrent callback
		Assert.assertArrayEquals(new byte[]{1,2,3,4}, dataQueue.poll());
		
		Assert.assertTrue(serverConnection.get());
		Assert.assertTrue(clientConnection.get());
	}
	
}
