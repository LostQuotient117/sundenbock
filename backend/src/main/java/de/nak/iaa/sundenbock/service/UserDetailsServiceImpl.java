package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.UserRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), user.isEnabled(),
                true, true, true, getAuthorities(user)
        );
    }

    /**
     * Sammelt die Berechtigungen (Rollen und individuelle Berechtigungen) eines Benutzers.
     *
     * @param user Der Benutzer, dessen Berechtigungen gesammelt werden sollen.
     * @return Eine Sammlung von GrantedAuthority-Objekten.
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
        });
        user.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
        return authorities;
    }
}
