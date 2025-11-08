package de.nak.iaa.sundenbock.service.navigation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Utility class for extracting roles and authorities from Spring Security annotations.
 * <p>
 * Supports extraction from {@link PreAuthorize} annotations
 * at both class and method levels.
 * </p>
 */
public final class RoleExtractor {

    private static final Pattern HAS_ROLE      = Pattern.compile("hasRole\\('(.*?)'\\)");
    private static final Pattern HAS_ANY_ROLE  = Pattern.compile("hasAnyRole\\((.*?)\\)");
    private static final Pattern HAS_AUTHORITY = Pattern.compile("hasAuthority\\('(.*?)'\\)");
    private static final Pattern HAS_ANY_AUTHORITY = Pattern.compile("hasAnyAuthority\\((.*?)\\)");

    private RoleExtractor() {}

    /**
     * Extracts all required permissions (roles and authorities) from the given class.
     *
     * @param type the class to inspect for security annotations
     * @return a set of required permissions
     */
    public static Set<String> extractRequiredPermissions(Class<?> type) {
        Stream<String> classPermissions = extractFromPreAuthorize(type.getAnnotation(PreAuthorize.class)).stream();
        Stream<String> methodPermissions = extractFromMethods(type).stream();
        return Stream.concat(classPermissions, methodPermissions).collect(toSet());
    }

    /**
     * Extracts permissions from a {@link PreAuthorize} annotation.
     *
     * @param preAuthorizeAnnotation the PreAuthorize annotation to extract roles/authorities from
     * @return a set of normalized roles and authorities
     */
    private static Set<String> extractFromPreAuthorize(PreAuthorize preAuthorizeAnnotation) {
        if (preAuthorizeAnnotation == null) return Set.of();

        String expressionString = preAuthorizeAnnotation.value();

        Stream<String> roleStream = Stream.concat(
                        findAllMatches(HAS_ROLE, expressionString),
                        findAllMatches(HAS_ANY_ROLE, expressionString)
                )
                .flatMap(matchedGroup -> matchedGroup.contains(",") ?
                        Stream.of(matchedGroup.split(",")) : Stream.of(matchedGroup))
                .map(String::trim)
                .map(RoleExtractor::stripQuotes)
                .filter(StringUtils::hasText)
                .map(RoleExtractor::normalizeRole);

        Stream<String> authorityStream = Stream.concat(
                        findAllMatches(HAS_AUTHORITY, expressionString),
                        findAllMatches(HAS_ANY_AUTHORITY, expressionString)
                )
                .flatMap(matchedGroup -> matchedGroup.contains(",") ?
                        Stream.of(matchedGroup.split(",")) : Stream.of(matchedGroup))
                .map(String::trim)
                .map(RoleExtractor::stripQuotes)
                .filter(StringUtils::hasText)
                .map(RoleExtractor::trimString);

        return Stream.concat(roleStream, authorityStream).collect(toSet());
    }

    /**
     * Extracts permissions from all declared methods of the given class.
     *
     * @param type the class to inspect
     * @return a set of permissions from method-level annotations
     */
    private static Set<String> extractFromMethods(Class<?> type) {
        return Stream.of(type.getDeclaredMethods())
                .flatMap(method -> extractFromPreAuthorize(method.getAnnotation(PreAuthorize.class)).stream())
                .collect(toSet());
    }

    /**
     * Finds all regex matches in the given text using the provided pattern.
     *
     * @param pattern the regex pattern to match
     * @param text    the text to search
     * @return a stream of matching groups
     */
    private static Stream<String> findAllMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        Stream.Builder<String> matchesBuilder = Stream.builder();
        while (matcher.find()) { matchesBuilder.add(matcher.group(1)); }
        return matchesBuilder.build();
    }

    /**
     * Strips leading and trailing quotes from a string.
     *
     * @param text the string to process
     * @return the string without surrounding quotes
     */
    private static String stripQuotes(String text) {
        return text.replaceAll("^['\"]|['\"]$", "");
    }

    /**
     * Normalizes a role string by removing the "ROLE_" prefix if present.
     *
     * @param roleString the role string to normalize
     * @return the normalized role
     */
    private static String normalizeRole(String roleString) {
        String role = roleString.trim();
        if (role.startsWith("ROLE_")) role = role.substring(5);
        return role;
    }

    /**
     * Trims leading and trailing whitespace from the string.
     *
     * @param string the string to trim
     * @return the trimmed string
     */
    public static String trimString(String string) {
        return string.trim();
    }

}
