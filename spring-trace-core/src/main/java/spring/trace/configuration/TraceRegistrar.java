package spring.trace.configuration;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

/**
 * @author: holyeye
 */
public class TraceRegistrar implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        Class<?> annotationType = EnableTrace.class;

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
                annotationType.getName(), false));

        Assert.notNull(attributes, String.format("@%s is not present on importing class '%s' as expected",
                annotationType.getSimpleName(), importingClassMetadata.getClassName()));

        return new String[]{SimpleTraceConfiguration.class.getName()};
    }

}
