package de.nak.iaa.sundenbock.config.security;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-Annotation, die prüft, ob der Benutzer die Berechtigung 'ROLE_MANAGE' besitzt.
 * Wird auch für die Verwaltung von Permissions verwendet.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority('ROLE_MANAGE')")
public @interface CanManageRoles {}
