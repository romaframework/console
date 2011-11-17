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
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassResolver;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaParameter;

public class DefaultConsoleAspect extends SelfRegistrantModule implements ConsoleAspect {

	public enum Mode {
		AUTO, INLINE, CONSOLE;
	}

	private static final Log						log				= LogFactory.getLog(DefaultConsoleAspect.class);
	private Map<String, ClassCommands>	commands	= new HashMap<String, ClassCommands>();
	private Mode												mode;

	@Override
	public String aspectName() {
		return ASPECT_NAME;
	}

	@Override
	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	@Override
	public void configClass(SchemaClassDefinition iClass) {
	}

	@Override
	public void configField(SchemaField iField) {
	}

	@Override
	public void configAction(SchemaAction iAction) {
	}

	@Override
	public void configEvent(SchemaEvent iEvent) {
	}

	@Override
	public void endConfigClass(SchemaClassDefinition iClass) {

	}

	@Override
	public Object getUnderlyingComponent() {
		return null;
	}

	@Override
	public void startup() throws RuntimeException {
		SchemaClassResolver classResolver = Roma.component(SchemaClassResolver.class);
		String pack = Utility.getApplicationAspectPackage(aspectName());
		Roma.component(SchemaClassResolver.class).addPackage(pack);
		classResolver.addPackage(pack);

		List<SchemaClass> classes = Roma.schema().getSchemaClassesByPackage(pack);

		for (SchemaClass clazz : classes) {
			ClassCommands comm = new ClassCommands(clazz.getSchemaClass());
			commands.put(comm.getName(), comm);
		}

	}

	@Override
	public void shutdown() throws RuntimeException {

	}

	@Override
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
					log.warn("Not Found command:" + args[0]);
				} else {
					ActionCommand commandAction = null;
					int subLength = 2;
					if (args.length > 1)
						commandAction = command.getAction(args[1], args.length - 2);
					else
						subLength--;
					if (commandAction == null) {
						commandAction = command.getAction(command.getSchemaClass().getFeature(ConsoleClassFeatures.DEFAULT_ACTION), args.length - subLength);
					}
					if (commandAction == null) {
						if (args.length > 1)
							log.warn("Not Found command:" + args[1] + " in command: " + args[0]);
						else
							log.warn("Not Default command in command: " + args[0]);
					} else {
						SchemaAction action = commandAction.getAction();
						if (action.getParameterNumber() != args.length - subLength) {
							log.warn("Wrong number of arguments was:" + args.length + " expected:" + (action.getParameterNumber() + subLength));
						} else {
							Object[] actionArgs = new Object[action.getParameterNumber()];
							System.arraycopy(args, subLength, actionArgs, 0, action.getParameterNumber());
							try {
								action.invoke(command.getSchemaClass().newInstance(), actionArgs);
							} catch (Exception e) {
								log.error("Error on execute command:" + command, e);
							}
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

	@Override
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
			builder.append(command.getName()).append(" ");
			if (command.getSchemaClass().getFeature(ConsoleClassFeatures.DESCRIPTION) != null)
				builder.append(command.getSchemaClass().getFeature(ConsoleClassFeatures.DESCRIPTION));
			builder.append("\n\t");
			for (ActionCommand action : command.getActions()) {
				builder.append(" ").append(action.getName());
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	public String buildHelpCommandGroup(String className) {
		StringBuilder builder = new StringBuilder();
		ClassCommands command = commands.get(className);
		for (ActionCommand action : command.getActions()) {
			builder.append(action.getName());
			builder.append(" ").append(action.getAction().getFeature(ConsoleActionFeatures.DESCRIPTION)).append("\n\r");
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
				for (SchemaParameter parameter : ac.getAction().getParameters().values()) {
					String pName = parameter.getFeature(ConsoleParameterFeatures.NAME);
					if (pName != null) {
						pName = parameter.getType().getName();
					}
					builder.append(pName);
					builder.append(" ").append(parameter.getFeature(ConsoleParameterFeatures.DESCRIPTION)).append("\n\r");
				}
			}
			return builder.toString();
		}
	}
}
