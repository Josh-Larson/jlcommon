/***********************************************************************************
 * MIT License                                                                     *
 *                                                                                 *
 * Copyright (c) 2019 Josh Larson                                                  *
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

package me.joshlarson.jlcommon.data;

import me.joshlarson.jlcommon.log.Log;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileBackedBuffer implements AutoCloseable {
	
	private static final byte[] EMPTY_DATA = new byte[1024];
	
	private final @NotNull File file;
	private final @NotNull FileChannel fileChannel;
	private final @NotNull FileLock fileLock;
	private final @NotNull MappedByteBuffer buffer;
	
	private FileBackedBuffer(@NotNull File file, @NotNull FileChannel fileChannel, @NotNull FileLock fileLock, @NotNull MappedByteBuffer buffer) {
		this.file = file;
		this.fileChannel = fileChannel;
		this.fileLock = fileLock;
		this.buffer = buffer;
	}
	
	/**
	 * Gets the underlying MappedByteBuffer
	 * @return the underlying MappedByteBuffer
	 */
	@NotNull
	public MappedByteBuffer getBuffer() {
		return buffer;
	}
	
	/**
	 * Closes the buffer and deletes the file after wiping the contents with zeros
	 */
	@Override
	public void close() {
		try {
			buffer.position(0);
			buffer.limit((int) fileChannel.size());
			while (buffer.hasRemaining()) {
				buffer.put(EMPTY_DATA, 0, Math.min(EMPTY_DATA.length, buffer.remaining()));
			}
			buffer.clear();
		} catch (IOException e) {
			Log.w("Failed to wipe file buffer data: %s", file);
		}
		try {
			fileLock.release();
		} catch (IOException e) {
			Log.w("Failed to release file lock: %s", file);
		}
		if (!file.delete())
			Log.w("Failed to delete buffer file: %s", file);
	}
	
	@Override
	@NotNull
	public String toString() {
		return "FileBackedBuffer[maxSize="+buffer.capacity()+" file="+file+"]";
	}
	
	/**
	 * Creates a file backed buffer with the specified size and the default prefix "fbb" and suffix ".bin"
	 * @param fileSize the maximum size of the buffer
	 * @return a new FileBackedBuffer
	 * @throws IOException if an error occurs when creating the underlying file buffer
	 */
	@NotNull
	public static FileBackedBuffer create(int fileSize) throws IOException {
		return create("fbb", ".bin", fileSize);
	}
	
	@NotNull
	public static FileBackedBuffer create(String filePrefix, String fileSuffix, int fileSize) throws IOException {
		Path path = Files.createTempFile(filePrefix, fileSuffix);
		return create(path, fileSize);
	}
	
	@NotNull
	public static FileBackedBuffer create(File file, int fileSize) throws IOException {
		return create(file.toPath(), fileSize);
	}
	
	@NotNull
	public static FileBackedBuffer create(Path path, int fileSize) throws IOException {
		File file = path.toFile();
		FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.SPARSE, StandardOpenOption.READ, StandardOpenOption.WRITE);
		FileLock fileLock = fileChannel.lock(0, fileSize, false);
		MappedByteBuffer buffer = fileChannel.map(MapMode.READ_WRITE, 0, fileSize);
		return new FileBackedBuffer(file, fileChannel, fileLock, buffer);
	}
}
