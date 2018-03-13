package me.joshlarson.jlcommon.log;

import javax.annotation.Nonnull;

public interface LogWrapper {
	
	void onLog(@Nonnull Log.LogLevel level, @Nonnull String str);
	
}
