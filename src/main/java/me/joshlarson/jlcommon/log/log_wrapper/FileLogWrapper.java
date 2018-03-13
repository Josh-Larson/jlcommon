package me.joshlarson.jlcommon.log.log_wrapper;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FileLogWrapper extends StreamLogWrapper {
	
	public FileLogWrapper(@Nonnull File file) {
		super(safeCreateOutputStream(file));
	}
	
	@Nonnull
	private static OutputStream safeCreateOutputStream(@Nonnull File file) {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new OutputStream() {
				
				public void write(int b) { }
			};
		}
	}
	
}
