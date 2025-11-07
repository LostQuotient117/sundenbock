package de.nak.iaa.sundenbock.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for JWT settings, mapped from
 * application.yaml at the prefix "application.security.jwt".
 */
@ConfigurationProperties(prefix = "application.security.jwt")
public record JwtProperties(
        String secretKey,
        long expiration
) {}
