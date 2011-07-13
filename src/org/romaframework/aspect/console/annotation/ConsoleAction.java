package org.romaframework.aspect.console.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.romaframework.aspect.core.annotation.AnnotationConstants;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ConsoleAction {

	public String name() default AnnotationConstants.DEF_VALUE;

	public String description() default AnnotationConstants.DEF_VALUE;

	public String[] parametersOrder() default AnnotationConstants.DEF_VALUE;

}
