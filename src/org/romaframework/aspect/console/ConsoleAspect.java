package org.romaframework.aspect.console;

import org.romaframework.core.aspect.Aspect;

public interface ConsoleAspect extends Aspect {

	public static final String	ASPECT_NAME	= "console";

	/**
	 * Exec an operation on args
	 * 
	 * @param args
	 */
	public void execute(String[] args);

	public String buildHelp();

	public String buildHelpCommandGroup(String className);

	public String buildHelpCommand(String className, String action);

}
