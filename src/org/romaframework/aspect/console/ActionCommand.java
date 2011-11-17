package org.romaframework.aspect.console;

import org.romaframework.aspect.console.feature.ConsoleActionFeatures;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.reflection.SchemaActionReflection;

public class ActionCommand {
	private SchemaAction	action;
	
	public ActionCommand(SchemaAction action) {
		this.action =action;
	}
	
	public String getName(){
		String name;
		if (action.isSettedFeature(ConsoleActionFeatures.NAME)) {
			name = action.getFeature(ConsoleActionFeatures.NAME);
		} else {
			name = ((SchemaActionReflection) action).getMethod().getName();
		}
		return name;
	}
	
	public SchemaAction getAction() {
		return action;
	}
}
