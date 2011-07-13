package org.romaframework.console;

import java.io.File;

import org.romaframework.aspect.console.ConsoleAspect;
import org.romaframework.core.Roma;
import org.romaframework.core.config.RomaApplicationContext;

public class RomaMain {

	public RomaMain() {
		RomaApplicationContext.setApplicationPath(new File(".").getAbsolutePath());
		RomaApplicationContext.getInstance().startup();
	}

	public static void main(String[] args) {
		RomaMain console = new RomaMain();
		console.start(args);
		console.shutdown();
	}

	private void start(String[] args) {
		Roma.component(ConsoleAspect.class).execute(args);
	}

	private void shutdown() {
		RomaApplicationContext.getInstance().shutdown();
	}

}
