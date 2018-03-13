package me.joshlarson.jlcommon.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Unused {
	
	String reason() default "";
	
}
