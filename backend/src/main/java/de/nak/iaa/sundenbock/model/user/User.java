package de.nak.iaa.sundenbock.model.user;

import de.nak.iaa.sundenbock.model.permission.Permission;
import de.nak.iaa.sundenbock.model.role.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Entity representing a User in the system.
 * <p>
 * This class implements Spring Security's {@link UserDetails} interface.
 * This allows the {@link User} object itself to be stored as the Principal
 * in the SecurityContextHolder.
 * <p>
 * This design is crucial for the {@link de.nak.iaa.sundenbock.config.JpaConfig}
 * AuditorAware bean, as it allows retrieving the current user from memory
 * without triggering a database query, preventing a StackOverflowError during
 * transactional auto-flushing.
 */
@Entity
@Table(name = "users") // "user" ist oft reserviert
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private boolean enabled = true;

    // zwischentable rollen und user
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // zwischentable permissions und user
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    private Set<Permission> permissions = new HashSet<>();


    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (permissions != null) {
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
        }

        if (roles != null) {
            roles.forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role.getName()));
                role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
            });
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
