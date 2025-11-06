package de.nak.iaa.sundenbock.service.navigation;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public final class RoleExtractor {

    private static final Pattern HAS_ROLE      = Pattern.compile("hasRole\\('(.*?)'\\)");
    private static final Pattern HAS_ANY_ROLE  = Pattern.compile("hasAnyRole\\((.*?)\\)");
    private static final Pattern HAS_AUTHORITY = Pattern.compile("hasAuthority\\('(.*?)'\\)");
    private static final Pattern HAS_ANY_AUTHORITY = Pattern.compile("hasAnyAuthority\\((.*?)\\)");

    private RoleExtractor() {}

    public static Set<String> extractRequiredPermissions(Class<?> type) {
        Stream<String> classPermissions = Stream.concat(
                extractFromSecured(type.getAnnotation(Secured.class)).stream(),
                extractFromPreAuthorize(type.getAnnotation(PreAuthorize.class)).stream()
        );
        Stream<String> methodPermissions = extractFromMethods(type).stream();
        return Stream.concat(classPermissions, methodPermissions).collect(toSet());
    }

    private static Set<String> extractFromSecured(Secured secured) {
        if (secured == null) return Set.of();
        return Stream.of(secured.value())
                .map(RoleExtractor::normalizeRole)
                .collect(toSet());
    }

    private static Set<String> extractFromPreAuthorize(PreAuthorize preAuthorizeAnnotation) {
        if (preAuthorizeAnnotation == null) return Set.of();

        String expressionString = preAuthorizeAnnotation.value();

        Stream<String> atomicExpressions = Stream.of(
                        Stream.of(expressionString.split(" or ")),
                        Stream.of(expressionString.split(" and "))
                )
                .flatMap(stringStream -> stringStream);


        Stream<String> roleStream = atomicExpressions
                .flatMap(expressionPart -> Stream.concat(
                        findAllMatches(HAS_ROLE, expressionPart),
                        findAllMatches(HAS_ANY_ROLE, expressionPart)
                ))
                .flatMap(matchedGroup -> matchedGroup.contains(",") ?
                        Stream.of(matchedGroup.split(",")) : Stream.of(matchedGroup))
                .map(String::trim)
                .map(RoleExtractor::stripQuotes)
                .filter(StringUtils::hasText)
                .map(RoleExtractor::normalizeRole);

        Stream<String> atomicExpressionsForAuthorities = Stream.of(
                        Stream.of(expressionString.split(" or ")),
                        Stream.of(expressionString.split(" and "))
                )
                .flatMap(stringStream -> stringStream);

        Stream<String> authorityStream = atomicExpressionsForAuthorities
                .flatMap(expressionPart -> Stream.concat(
                        findAllMatches(HAS_AUTHORITY, expressionPart),
                        findAllMatches(HAS_ANY_AUTHORITY, expressionPart)
                ))
                .flatMap(matchedGroup -> matchedGroup.contains(",") ?
                        Stream.of(matchedGroup.split(",")) : Stream.of(matchedGroup))
                .map(String::trim)
                .map(RoleExtractor::stripQuotes)
                .filter(StringUtils::hasText)
                .map(RoleExtractor::trimString);

        return Stream.concat(roleStream, authorityStream).collect(toSet());
    }

    private static Set<String> extractFromMethods(Class<?> type) {
        return Stream.of(type.getDeclaredMethods())
                .flatMap(method -> {
                    Stream<String> permissionsFromPre = extractFromPreAuthorize(method.getAnnotation(PreAuthorize.class)).stream();
                    Stream<String> permissionsFromSecured = extractFromSecured(method.getAnnotation(Secured.class)).stream();
                    return Stream.concat(permissionsFromPre, permissionsFromSecured);
                })
                .collect(toSet());
    }

    private static Stream<String> findAllMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        Stream.Builder<String> matchesBuilder = Stream.builder();
        while (matcher.find()) { matchesBuilder.add(matcher.group(1)); }
        return matchesBuilder.build();
    }

    private static String stripQuotes(String text) {
        return text.replaceAll("^['\"]|['\"]$", "");
    }

    private static String normalizeRole(String roleString) {
        String role = roleString.trim();
        if (role.startsWith("ROLE_")) role = role.substring(5);
        return role;
    }

    public static String trimString(String string) {
        return string.trim();
    }

}
