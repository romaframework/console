package org.romaframework.aspect.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.romaframework.aspect.console.feature.ConsoleClassFeatures;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClass;

public class ClassCommands {

	private SchemaClass																schemaClass;
	private Map<String, Map<Integer, ActionCommand>>	actionsMap	= new HashMap<String, Map<Integer, ActionCommand>>();
	private List<ActionCommand>												actions			= new ArrayList<ActionCommand>();

	public ClassCommands(SchemaClass schemaClass) {
		for (SchemaAction action : schemaClass.getActions().values()) {
			ActionCommand comm = new ActionCommand(action,this);
			Map<Integer, ActionCommand> maps = actionsMap.get(comm.getName());
			if (maps == null) {
				maps = new HashMap<Integer, ActionCommand>();
				actionsMap.put(comm.getName(), maps);
			}
			maps.put(comm.getAction().getParameterNumber(), comm);
			actions.add(comm);
		}
		this.schemaClass = schemaClass;
	}

	public List<ActionCommand> getActions() {
		return actions;
	}

	public SchemaClass getSchemaClass() {
		return schemaClass;
	}

	public Map<Integer, ActionCommand> getActions(String alias) {
		return actionsMap.get(alias);
	}

	public ActionCommand getAction(String alias, Integer nArgs) {
		Map<Integer, ActionCommand> maps = actionsMap.get(alias);
		if (maps != null) {
			return maps.get(nArgs);
		}
		return null;
	}

	public String getName() {
		String name;
		if (this.schemaClass.isSettedFeature(ConsoleClassFeatures.NAME)) {
			name = this.schemaClass.getFeature(ConsoleClassFeatures.NAME);
		} else {
			name = this.schemaClass.getName();
		}
		return name;
	}
}