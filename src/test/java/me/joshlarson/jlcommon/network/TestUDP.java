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

import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.jlcommon.log.log_wrapper.ConsoleLogWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class TestUDP {
	
	@Test
	public void testSimple() throws InterruptedException, SocketException {
		Log.addWrapper(new ConsoleLogWrapper());
		BlockingQueue<DatagramPacket> aQueue = new LinkedBlockingQueue<>();
		BlockingQueue<DatagramPacket> bQueue = new LinkedBlockingQueue<>();
		UDPServer a = new UDPServer(new InetSocketAddress(0), aQueue::offer);
		UDPServer b = new UDPServer(new InetSocketAddress(0), bQueue::offer);
		a.bind();
		b.bind();
		
		a.send(b.getPort(), "localhost", new byte[]{1,2,3,4});
		DatagramPacket request = bQueue.poll(1, TimeUnit.SECONDS);
		Assert.assertNotNull(request);
		Assert.assertTrue(aQueue.isEmpty());
		Assert.assertTrue(bQueue.isEmpty());
		byte [] data = request.getData();
		
		b.send(a.getPort(), "localhost", data);
		DatagramPacket response = aQueue.poll(1, TimeUnit.SECONDS);
		Assert.assertNotNull(response);
		Assert.assertArrayEquals(data, response.getData());
	}
	
}
