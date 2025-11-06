package de.nak.iaa.sundenbock.service.user;

import de.nak.iaa.sundenbock.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Spring Security's {@link UserDetailsService}.
 * <p>
 * Responsible for loading user-specific data from the database during authentication.
 * Returns a {@link UserDetails} object representing the user or throws {@link UsernameNotFoundException}
 * if the user cannot be found.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username.
     *
     * @param username the username to look up
     * @return a {@link UserDetails} representing the user
     * @throws UsernameNotFoundException when no user with the given username exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
