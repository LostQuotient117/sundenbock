package de.nak.iaa.sundenbock.config;

import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    public JpaConfig(UserRepository userRepository) {
    }

    @Bean
    public AuditorAware<User> auditorAware(UserRepository userRepository) {
        return () -> {
            var auth =  SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()){
                return userRepository.findByUsername(auth.getName());
            }

            return userRepository.findByUsername("admin")
                    .or(() -> userRepository.findByUsername("dev"))
                    .or(() -> {
                        // letzter Fallback – temporärer User (nicht gespeichert)
                        User systemUser = new User();
                        systemUser.setUsername("system");
                        systemUser.setEmail("system@local");
                        systemUser.setPassword("system");
                        return Optional.of(systemUser);
                    });
        };
    }
}
