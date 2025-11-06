package de.nak.iaa.sundenbock.navigation;

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
    String[] roles() default {};
}
