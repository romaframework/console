package org.romaframework.aspect.console.feature;

import org.romaframework.aspect.console.ConsoleAspect;
import org.romaframework.core.schema.Feature;
import org.romaframework.core.schema.FeatureType;

public class ConsoleActionFeatures {

	public static final Feature<String>		NAME							= new Feature<String>(ConsoleAspect.ASPECT_NAME, "name", FeatureType.ACTION, String.class);
	public static final Feature<String>		DESCRIPTION				= new Feature<String>(ConsoleAspect.ASPECT_NAME, "description", FeatureType.ACTION, String.class);
	public static final Feature<String[]>	PARAMETERS_ORDER	= new Feature<String[]>(ConsoleAspect.ASPECT_NAME, "parametersOrder", FeatureType.ACTION,
																															String[].class);

}
