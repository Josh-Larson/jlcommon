module me.joshlarson.jlcommon {
	requires java.base;
	requires annotations;
	
	exports me.joshlarson.jlcommon.log;
	exports me.joshlarson.jlcommon.log.log_wrapper;
	exports me.joshlarson.jlcommon.info;
	exports me.joshlarson.jlcommon.utilities;
	exports me.joshlarson.jlcommon.data;
	exports me.joshlarson.jlcommon.callback;
	exports me.joshlarson.jlcommon.control;
	exports me.joshlarson.jlcommon.collections;
	exports me.joshlarson.jlcommon.concurrency;
	exports me.joshlarson.jlcommon.concurrency.beans;
	exports me.joshlarson.jlcommon.network;
	exports me.joshlarson.jlcommon.annotations;
}
