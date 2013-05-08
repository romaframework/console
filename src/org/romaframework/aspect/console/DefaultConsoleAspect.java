package org.romaframework.aspect.console;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.console.feature.ConsoleActionFeatures;
import org.romaframework.aspect.console.feature.ConsoleClassFeatures;
import org.romaframework.aspect.console.feature.ConsoleParameterFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.Utility;
import org.romaframework.core.module.SelfRegistrantModule;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassResolver;
import org.romaframework.core.schema.SchemaParameter;

public class DefaultConsoleAspect extends SelfRegistrantModule implements ConsoleAspect {

	public enum Mode {
		AUTO, INLINE, CONSOLE;
	}

	private static final Log						log				= LogFactory.getLog(DefaultConsoleAspect.class);
	private Map<String, ClassCommands>	commands	= new HashMap<String, ClassCommands>();
	private Mode												mode;

	public String aspectName() {
		return ASPECT_NAME;
	}

	public Object getUnderlyingComponent() {
		return null;
	}

	public void startup() throws RuntimeException {
		SchemaClassResolver classResolver = Roma.component(SchemaClassResolver.class);
		String pack = Utility.getApplicationAspectPackage(aspectName());
		Roma.component(SchemaClassResolver.class).addPackage(pack);
		classResolver.addPackage(pack);

		List<SchemaClass> classes = Roma.schema().getSchemaClassesByPackage(pack);

		for (SchemaClass clazz : classes) {
			Class<?> cc = ((Class<?>) clazz.getLanguageType());
			if (!cc.isMemberClass() && !cc.isAnonymousClass() && !cc.isLocalClass()) {
				ClassCommands comm = new ClassCommands(clazz.getSchemaClass());
				commands.put(comm.getName(), comm);
			}
		}

	}

	public void shutdown() throws RuntimeException {

	}

	public void execute(String[] args) {
		boolean exit;
		if (mode == Mode.INLINE) {
			if (args.length == 0) {
				log.warn("Not Found command: no arguments");
				return;
			}
			exit = true;
		} else if (mode == Mode.AUTO || mode == null) {
			exit = args.length != 0;
		} else {
			exit = false;
		}
		do {
			if (args.length != 0) {
				ClassCommands command = commands.get(args[0]);
				if (command == null) {
					log.warn("Not found class <" + args[0] + ">");
				} else {
					ActionCommand commandAction = null;
					int subLength = 2;
					if (args.length > 1)
						commandAction = command.getAction(args[1], args.length - subLength);
					if (commandAction == null) {
						if (command.getSchemaClass().isSetFeature(ConsoleClassFeatures.DEFAULT_ACTION)) {
							subLength--;
							commandAction = command.getDefaultAction(args, args.length - subLength);
							if (commandAction == null) {
								log.warn("Not found action <" + command.getSchemaClass().getFeature(ConsoleClassFeatures.DEFAULT_ACTION) + "> with " + (args.length - subLength)
										+ " arguments in class <" + args[0] + "> ");
							}
						} else {
							if (args.length > 1)
								log.warn("Not found action <" + args[1] + "> with " + (args.length - 2) + " arguments in class <" + args[0] + "> ");
						}
					}
					if (commandAction != null) {
						String[] actionArgs = new String[args.length - subLength];
						System.arraycopy(args, subLength, actionArgs, 0, args.length - subLength);
						try {
							commandAction.execute(actionArgs);
						} catch (Exception e) {
							log.error("Error on execute command:" + command, e);
						}
					}
				}
			}
			if (!exit) {
				System.out.print('>');
				args = readCommand();
				if (args.length == 1 && "exit".equals(args[0]))
					exit = true;
			}
		} while (!exit);
	}

	protected String[] readCommand() {
		List<String> params = new ArrayList<String>();
		StringBuilder buff = new StringBuilder();
		boolean inDouble = false, inSingle = false, escape = false;

		Reader reader;
		if (System.console() != null)
			reader = System.console().reader();
		else
			reader = new InputStreamReader(System.in);
		int ch;
		try {
			do {
				ch = reader.read();
				if (ch == '\\') {
					escape = true;
					ch = reader.read();
				}

				if (ch == '\'' && !inDouble && !escape) {
					if (inSingle) {
						inSingle = false;
						fill(params, buff, false);
					} else {
						inSingle = true;
						fill(params, buff, true);
					}
					continue;
				}
				if (ch == '"' && !inSingle && !escape) {
					if (inDouble) {
						inDouble = false;
						fill(params, buff, false);
					} else {
						inDouble = true;
						fill(params, buff, true);
					}
					continue;
				}
				if (ch == '\t' && !inDouble && !inSingle && !escape) {
					handleComplete(params, buff);
					continue;
				}
				if (ch == ' ' && !inDouble && !inSingle && !escape) {
					fill(params, buff, true);
					continue;
				}
				escape = false;
				if (ch != '\n')
					buff.append((char) ch);
			} while (ch != '\n');
			fill(params, buff, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return params.toArray(new String[params.size()]);
	}

	protected void fill(List<String> params, StringBuilder buff, boolean chekEmpty) {
		String buffVal = buff.toString();
		if (!buffVal.trim().isEmpty() || !chekEmpty) {
			params.add(buffVal);
		}
		buff.setLength(0);
	}

	protected void handleComplete(List<String> params, StringBuilder buff) {

	}

	public String moduleName() {
		return ASPECT_NAME;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public String buildHelp() {
		StringBuilder builder = new StringBuilder();
		for (ClassCommands command : commands.values()) {
			if (command.getSchemaClass().isSetFeature(ConsoleClassFeatures.DESCRIPTION))
				builder.append(command.getSchemaClass().getFeature(ConsoleClassFeatures.DESCRIPTION)).append("\n");

			for (ActionCommand action : command.getActions()) {
				builder.append("- ").append(command.getName()).append(" ");
				builder.append(action.getName()).append(" ");
				for (SchemaParameter parameter : action.getAction().getParameters().values()) {
					String pName = parameter.getFeature(ConsoleParameterFeatures.NAME);
					if (pName == null) {
						pName = parameter.getType().getName();
					}
					builder.append("<").append(pName).append("> ");
				}
				if (action.getAction().isSetFeature(ConsoleActionFeatures.DESCRIPTION))
					builder.append(" -> ").append(action.getAction().getFeature(ConsoleActionFeatures.DESCRIPTION));
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	public String buildHelpCommandGroup(String className) {
		StringBuilder builder = new StringBuilder();
		ClassCommands command = commands.get(className);
		if (command.getSchemaClass().isSetFeature(ConsoleClassFeatures.DESCRIPTION))
			builder.append(command.getSchemaClass().getFeature(ConsoleClassFeatures.DESCRIPTION)).append("\n");

		for (ActionCommand action : command.getActions()) {
			builder.append("- ").append(command.getName()).append(" ").append(action.getName()).append(" ");
			for (SchemaParameter parameter : action.getAction().getParameters().values()) {
				String pName = parameter.getFeature(ConsoleParameterFeatures.NAME);
				if (pName == null) {
					pName = parameter.getType().getName();
				}
				builder.append("<").append(pName).append("> ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	public String buildHelpCommand(String className, String action) {
		StringBuilder builder = new StringBuilder();
		ClassCommands command = commands.get(className);
		if (command == null) {
			return buildHelp();
		}
		Map<Integer, ActionCommand> actions = command.getActions(action);
		if (actions == null) {
			return buildHelpCommandGroup(className);
		} else {
			for (ActionCommand ac : actions.values()) {
				builder.append(command.getName()).append(" ").append(ac.getName()).append(" -> ").append(ac.getAction().getFeature(ConsoleActionFeatures.DESCRIPTION)).append("\n");
				for (SchemaParameter parameter : ac.getAction().getParameters().values()) {
					String pName = parameter.getFeature(ConsoleParameterFeatures.NAME);
					if (pName == null) {
						pName = parameter.getType().getName();
					}
					builder.append("\t<").append(pName).append(">");
					if (parameter.isSetFeature(ConsoleParameterFeatures.DESCRIPTION))
						builder.append(" -> ").append(parameter.getFeature(ConsoleParameterFeatures.DESCRIPTION));
					builder.append("\n");
				}
			}
			return builder.toString();
		}
	}
}
