package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementierung des Spring Security UserDetailsService.
 * Verantwortlich für das Laden von benutzerspezifischen Daten aus der Datenbank.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Lädt einen Benutzer anhand seines Benutzernamens.
     *
     * @param username Der Benutzername des zu ladenden Benutzers.
     * @return ein UserDetails-Objekt, das die Kerninformationen des Benutzers enthält.
     * @throws UsernameNotFoundException wenn kein Benutzer mit dem angegebenen Benutzernamen gefunden wird.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
