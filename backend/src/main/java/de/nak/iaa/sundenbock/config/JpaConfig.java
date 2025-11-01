package de.nak.iaa.sundenbock.config;

import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JpaConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuditorAware<User> auditorAware() {
        return () -> {
            var auth =  SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()){
                return userRepository.findByUsername(auth.getName());
            }

            return userRepository.findByUsername("system")
                    .or(() -> userRepository.findByUsername("admin"))
                    .or(() -> {
                        User systemUser = new User();
                        systemUser.setUsername("system");
                        systemUser.setEmail("system@local");
                        systemUser.setPassword(passwordEncoder.encode("systemPassword-LE")); // Braucht ein PW
                        systemUser.setEnabled(false); // Deaktiviert
                        User savedSystemUser = userRepository.save(systemUser);
                        return Optional.of(savedSystemUser);
                    });

        };
    }
}
