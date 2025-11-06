package de.nak.iaa.sundenbock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NavItem {
    String label();
    String path();
    String icon() default "";
    String[] permissions() default {};
}
