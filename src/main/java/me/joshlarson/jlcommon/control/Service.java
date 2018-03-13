package me.joshlarson.jlcommon.control;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * A Service is a class that does a specific job for the application
 */
public abstract class Service {
	
	private IntentManager intentManager;
	
	public Service() {
		this.intentManager = IntentManager.getInstance();
	}
	
	/**
	 * Initializes this service. If the service returns false on this method then the initialization failed and may not work as intended.
	 *
	 * @return TRUE if initialization was successful, FALSE otherwise
	 */
	public boolean initialize() {
		return true;
	}
	
	/**
	 * Starts this service. If the service returns false on this method then the service failed to start and may not work as intended.
	 *
	 * @return TRUE if starting was successful, FALSE otherwise
	 */
	public boolean start() {
		return true;
	}
	
	/**
	 * Stops the service. If the service returns false on this method then the service failed to stop and may not have fully locked down.
	 *
	 * @return TRUE if stopping was successful, FALSe otherwise
	 */
	public boolean stop() {
		return true;
	}
	
	/**
	 * Terminates this service. If the service returns false on this method then the service failed to shut down and resources may not have been cleaned up.
	 *
	 * @return TRUE if termination was successful, FALSE otherwise
	 */
	public boolean terminate() {
		IntentManager im = IntentManager.getInstance();
		if (im != null)
			im.terminate();
		return true;
	}
	
	/**
	 * Determines whether or not this service is operational
	 *
	 * @return TRUE if this service is operational, FALSE otherwise
	 */
	public boolean isOperational() {
		return true;
	}
	
	/**
	 * Registers for the intent using the specified consumer
	 *
	 * @param c        the class of intent to register for
	 * @param consumer the consumer to run when the intent is fired
	 */
	protected <T extends Intent> void registerForIntent(@Nonnull Class<T> c, @Nonnull Consumer<T> consumer) {
		intentManager.registerForIntent(c, consumer);
	}
	
	/**
	 * Unregisters for the intent using the specified consumer
	 *
	 * @param c        the class of intent to unregister
	 * @param consumer the consumer that was previous registered
	 */
	protected <T extends Intent> void unregisterForIntent(@Nonnull Class<T> c, @Nonnull Consumer<T> consumer) {
		intentManager.unregisterForIntent(c, consumer);
	}
	
	public void setIntentManager(@Nonnull IntentManager intentManager) {
		this.intentManager = intentManager;
	}
	
	@CheckForNull
	public IntentManager getIntentManager() {
		return intentManager;
	}
	
}
