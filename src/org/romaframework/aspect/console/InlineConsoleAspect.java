package org.romaframework.aspect.console;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.console.feature.ConsoleClassFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.Utility;
import org.romaframework.core.module.SelfRegistrantModule;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassResolver;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;

public class InlineConsoleAspect extends SelfRegistrantModule implements ConsoleAspect {

	private static final Log						log				= LogFactory.getLog(InlineConsoleAspect.class);
	private Map<String, ClassCommands>	commands	= new HashMap<String, ClassCommands>();

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
			String name;
			if (clazz.isSettedFeature(ConsoleClassFeatures.NAME)) {
				name = clazz.getFeature(ConsoleClassFeatures.NAME);
			} else {
				name = clazz.getName();
			}
			if (name != null) {
				commands.put(name, new ClassCommands(clazz.getSchemaClass()));
			}

		}

	}

	@Override
	public void shutdown() throws RuntimeException {

	}

	@Override
	public void execute(String[] args) {
		if (args.length == 0) {
			log.warn("Not Found command: no arguments");
			return;
		}
		ClassCommands command = commands.get(args[0]);
		if (command == null) {
			log.warn("Not Found command:" + args[0]);
			return;
		}
		SchemaAction action = command.getAction(args[1]);
		if (action.getParameterNumber() != args.length - 2) {
			log.warn("Wrong number of arguments was:" + args.length + " expected:" + (action.getParameterNumber() + 2));
			return;
		}
		Object[] actionArgs = new Object[action.getParameterNumber()];
		System.arraycopy(args, 2, actionArgs, 0, action.getParameterNumber());
		try {
			action.invoke(command.getSchemaClass().newInstance(), actionArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String moduleName() {
		return ASPECT_NAME;
	}

}
