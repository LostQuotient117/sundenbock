package de.nak.iaa.sundenbock.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Creates the application-wide {@link CacheManager} backed by Caffeine.
     * <p>
     * The manager provides the following caches:
     * <ul>
     *   <li>{@code nav:all} – intended for caching precomputed navigation structures for all users.</li>
     *   <li>{@code nav:byPermissions} – intended for caching navigation filtered by user permissions.</li>
     * </ul>
     * Each cache is configured with a maximum size of 500 entries and an {@code expireAfterWrite}
     * policy of 10 minutes. These settings aim to balance memory usage and freshness of navigation data.
     * </p>
     * <p>
     * Typical usage is via Spring’s caching annotations such as {@code @Cacheable}, {@code @CachePut},
     * and {@code @CacheEvict} on service methods.
     * </p>
     *
     * @return a configured {@link CacheManager} instance
     */
    @Bean
    public CacheManager cacheManager() {
        var mgr = new CaffeineCacheManager("nav:all", "nav:byPermissions");
        mgr.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES));
        return mgr;
    }
}