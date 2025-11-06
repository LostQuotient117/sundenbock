package de.nak.iaa.sundenbock.navigation;

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

    private RoleExtractor() {}

    public static Set<String> extractRequiredRoles(Class<?> type) {
        var classRoles = Stream.concat(
                extractFromSecured(type.getAnnotation(Secured.class)).stream(),
                extractFromPreAuthorize(type.getAnnotation(PreAuthorize.class)).stream()
        );
        var methodRoles = extractFromMethods(type).stream(); // ruft die neue Methode auf
        return Stream.concat(classRoles, methodRoles).collect(toSet());
    }

    private static Set<String> extractFromSecured(Secured secured) {
        if (secured == null) return Set.of();
        return Stream.of(secured.value())
                .map(RoleExtractor::normalizeRole)
                .collect(toSet());
    }

    private static Set<String> extractFromPreAuthorize(PreAuthorize pre) {
        if (pre == null) return Set.of();
        var expr = pre.value();
        return Stream.concat(
                        matchMany(HAS_ROLE, expr),
                        Stream.concat(
                                matchMany(HAS_ANY_ROLE, expr),
                                matchMany(HAS_AUTHORITY, expr)
                        )
                )
                .flatMap(s -> s.contains(",") ? Stream.of(s.split(",")) : Stream.of(s))
                .map(String::trim)
                .map(RoleExtractor::stripQuotes)
                .filter(StringUtils::hasText)
                .map(RoleExtractor::normalizeRole)
                .collect(toSet());
    }

    public static Set<String> extractFromMethods(Class<?> type) {
        return Stream.of(type.getDeclaredMethods())
                .flatMap(method -> {
                    var rolesFromPre = extractFromPreAuthorize(method.getAnnotation(PreAuthorize.class)).stream();
                    var rolesFromSecured = extractFromSecured(method.getAnnotation(Secured.class)).stream();
                    return Stream.concat(rolesFromPre, rolesFromSecured);
                })
                .collect(toSet());
    }

    private static Stream<String> matchMany(Pattern p, String input) {
        Matcher m = p.matcher(input);
        Stream.Builder<String> b = Stream.builder();
        while (m.find()) { b.add(m.group(1)); }
        return b.build();
    }

    private static String stripQuotes(String s) {
        return s.replaceAll("^['\"]|['\"]$", "");
    }

    public static String normalizeRole(String r) {
        var role = r.trim();
        if (role.startsWith("ROLE_")) role = role.substring(5);
        return role;
    }

}
