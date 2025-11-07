package de.nak.iaa.sundenbock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Registers global CORS mappings.
     * <p>
     * Applies CORS to all endpoints ({@code /**}) and allows requests from
     * {@code http://localhost:4200} using the HTTP methods GET, POST, PUT, and DELETE.
     * All headers are accepted and credentials (cookies/authorization headers) are allowed.
     * </p>
     *
     * @param registry the {@link CorsRegistry} used to configure the CORS mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}