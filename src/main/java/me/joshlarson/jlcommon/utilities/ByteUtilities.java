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
package me.joshlarson.jlcommon.utilities;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

public class ByteUtilities {
	
	private static final ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	@Nonnull
	public static String getHexString(@Nonnull byte[] bytes) {
		char[] data = new char[bytes.length * 2 + (bytes.length > 0 ? bytes.length - 1 : 0)];
		byte b;
		int ind;
		for (int i = 0; i < bytes.length; i++) {
			b = bytes[i];
			ind = i * 3;
			data[ind++] = HEX[(b & 0xFF) >>> 4];
			data[ind++] = HEX[b & 0x0F];
			if (ind < data.length)
				data[ind] = ' ';
		}
		return new String(data);
	}
	
	@Nonnull
	public static byte[] getHexStringArray(@Nonnull String string) {
		int len = string.length();
		if (len % 2 != 0)
			return new byte[0];
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character.digit(string.charAt(i + 1), 16));
		}
		return data;
	}
	
	@Nonnull
	public static byte[] longToBytes(long l) {
		byte[] b = new byte[Long.SIZE];
		synchronized (buffer) {
			buffer.putLong(0, l);
			System.arraycopy(buffer.array(), 0, b, 0, Long.SIZE);
		}
		return b;
	}
	
	public static long bytesToLong(@Nonnull byte[] a) {
		return bytesToLong(a, 0);
	}
	
	public static long bytesToLong(@Nonnull byte[] a, int offset) {
		synchronized (buffer) {
			for (int i = 0; i < Long.SIZE; i++) {
				if (i < a.length)
					buffer.put(i, a[i + offset]);
				else
					buffer.put(i, (byte) 0);
			}
			return buffer.getLong(0);
		}
	}
	
	@Nonnull
	public static String nextString(@Nonnull ByteBuffer data) {
		byte[] bData = data.array();
		StringBuilder str = new StringBuilder("");
		for (int i = data.position(); i < bData.length && bData[i] >= ' ' && bData[i] <= '~'; i++)
			str.append((char) data.get());
		return str.toString();
	}
}
