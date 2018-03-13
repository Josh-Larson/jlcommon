package me.joshlarson.jlcommon.control;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class IntentChain {
	
	private final IntentManager intentManager;
	private final AtomicReference<Intent> intent;
	
	public IntentChain() {
		this(IntentManager.getInstance());
	}
	
	public IntentChain(IntentManager intentManager) {
		this(intentManager, null);
	}
	
	public IntentChain(@Nullable Intent i) {
		this(IntentManager.getInstance(), i);
	}
	
	public IntentChain(IntentManager intentManager, @Nullable Intent i) {
		Objects.requireNonNull(intentManager, "IntentManager is null");
		this.intentManager = intentManager;
		this.intent = new AtomicReference<>(null);
	}
	
	public void reset() {
		intent.set(null);
	}
	
	public void broadcastAfter(@Nonnull Intent i) {
		i.broadcastAfterIntent(intent.getAndSet(i), intentManager);
	}
	
	public static void broadcastChain(Intent... intents) {
		for (int i = 1; i < intents.length; i++) {
			intents[i].broadcastAfterIntent(intents[i - 1]);
		}
		intents[0].broadcast();
	}
	
}
