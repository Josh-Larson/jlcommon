package me.joshlarson.jlcommon.log.log_wrapper;

import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.jlcommon.log.LogWrapper;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class StreamLogWrapper implements LogWrapper {
	
	private final BufferedWriter writer;
	
	public StreamLogWrapper(@Nonnull OutputStream os) {
		writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
	}
	
	@Override
	public void onLog(@Nonnull Log.LogLevel level, @Nonnull String str) {
		try {
			writer.write(str);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
