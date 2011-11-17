package org.romaframework.aspect.console.feature;

import org.romaframework.aspect.console.ConsoleAspect;
import org.romaframework.core.schema.Feature;
import org.romaframework.core.schema.FeatureType;

public class ConsoleClassFeatures {

	public static final Feature<String>	NAME				= new Feature<String>(ConsoleAspect.ASPECT_NAME, "name", FeatureType.CLASS, String.class);
	public static final Feature<String>	DESCRIPTION	= new Feature<String>(ConsoleAspect.ASPECT_NAME, "description", FeatureType.CLASS, String.class);
	public static final Feature<String>	DEFAULT_ACTION	= new Feature<String>(ConsoleAspect.ASPECT_NAME, "defaultAction", FeatureType.CLASS, String.class);

}
