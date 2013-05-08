package org.romaframework.aspect.console;

import org.romaframework.aspect.console.feature.ConsoleActionFeatures;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.reflection.SchemaActionReflection;

public class ActionCommand {
	private SchemaAction	action;
	private ClassCommands	container;

	public ActionCommand(SchemaAction action, ClassCommands classCommands) {
		this.action = action;
		this.container = classCommands;
	}

	public String getName() {
		String name;
		if (action.isSetFeature(ConsoleActionFeatures.NAME)) {
			name = action.getFeature(ConsoleActionFeatures.NAME);
		} else {
			name = ((SchemaActionReflection) action).getMethod().getName();
		}
		return name;
	}

	public SchemaAction getAction() {
		return action;
	}

	public void execute(String[] actionArgs) throws Exception {
		if (action.getParameterNumber() == 1 && action.getParameterIterator().next().getType().isAssignableAs(String[].class))
			action.invoke(container.getSchemaClass().newInstance(), new Object[] { actionArgs });
		else
			action.invoke(container.getSchemaClass().newInstance(), (Object[]) actionArgs);
	}
}
