package org.romaframework.aspect.console;

import java.util.HashMap;
import java.util.Map;

import org.romaframework.aspect.console.feature.ConsoleActionFeatures;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.reflection.SchemaActionReflection;

public class ClassCommands {

	private SchemaClass								schemaClass;
	private Map<String, SchemaAction>	actions	= new HashMap<String, SchemaAction>();

	public ClassCommands(SchemaClass schemaClass) {
		for (SchemaAction action : schemaClass.getActions().values()) {
			String name;
			if (action.isSettedFeature(ConsoleActionFeatures.NAME)) {
				name = action.getFeature(ConsoleActionFeatures.NAME);
			} else {
				name = ((SchemaActionReflection) action).getMethod().getName();
			}
			actions.put(name, action);
		}
		this.schemaClass = schemaClass;
	}

	public SchemaClass getSchemaClass() {
		return schemaClass;
	}

	public SchemaAction getAction(String alias) {
		return actions.get(alias);
	}

}