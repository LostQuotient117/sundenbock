package de.nak.iaa.sundenbock.config.security;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-Annotation, die prüft, ob der Benutzer die Berechtigung 'USER_MANAGE' besitzt.
 * Entspricht @PreAuthorize("hasAuthority('USER_MANAGE')").
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority('USER_MANAGE')")
public @interface CanManageUsers {}
