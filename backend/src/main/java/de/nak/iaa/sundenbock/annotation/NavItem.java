package de.nak.iaa.sundenbock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a navigable item in the application's UI navigation.
 * <p>
 * This annotation is intended to be placed on classes that represent
 * pages or views which should appear in a navigation menu. The provided
 * metadata (label, path, icon, permissions) can be used by the frontend
 * or a navigation service to build structured navigation trees and to
 * enforce access control.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NavItem {
    String label();
    String path();
    String icon() default "";
    String[] permissions() default {};
}
