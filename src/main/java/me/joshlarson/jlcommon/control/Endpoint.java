package me.joshlarson.jlcommon.control;

public abstract class Endpoint extends Manager {
	
	public final boolean startEndpoint() {
		return initialize() && start();
	}
	
	public final void stopEndpoint() {
		stop();
		terminate();
	}
	
}
