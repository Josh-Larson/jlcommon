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

import org.jetbrains.annotations.NotNull;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

public class SecureTCPSocket extends TCPSocket {
	
	private @NotNull SocketFactory socketFactory;
	
	public SecureTCPSocket() {
		this.socketFactory = SSLSocketFactory.getDefault();
	}
	
	public SecureTCPSocket(int bufferSize) {
		super(bufferSize);
		this.socketFactory = SSLSocketFactory.getDefault();
	}
	
	public SecureTCPSocket(InetSocketAddress address, int bufferSize) {
		super(address, bufferSize);
		this.socketFactory = SSLSocketFactory.getDefault();
	}
	
	public void setSocketFactory(@NotNull SocketFactory socketFactory) {
		this.socketFactory = Objects.requireNonNull(socketFactory);
	}
	
	@Override
	protected Socket createSocket() throws IOException {
		return socketFactory.createSocket();
	}
	
}
