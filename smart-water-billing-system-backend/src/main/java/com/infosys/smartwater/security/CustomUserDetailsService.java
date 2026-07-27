package com.infosys.smartwater.security;

import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Spring Security {@link UserDetailsService} implementation that loads user details
 * by email address (used as the principal identity throughout the application).
 *
 * <p>This service is consumed by:
 * <ul>
 *   <li>{@link JwtAuthenticationFilter} — to verify the user in the JWT subject</li>
 *   <li>{@code DaoAuthenticationProvider} in {@code SecurityConfig} — to authenticate
 *       credentials during login</li>
 * </ul>
 *
 * <p><b>Role mapping:</b> Each user's {@code Role} enum is prefixed with {@code ROLE_}
 * to satisfy Spring Security's {@code hasRole()} convention:
 * <ul>
 *   <li>{@code Role.ADMIN}    → {@code GrantedAuthority("ROLE_ADMIN")}</li>
 *   <li>{@code Role.RESIDENT} → {@code GrantedAuthority("ROLE_RESIDENT")}</li>
 * </ul>
 *
 * <p><b>Transaction:</b> {@code @Transactional(readOnly = true)} ensures
 * that lazy associations on the {@link User} entity can be safely initialised
 * within this service's method scope.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a {@link UserDetails} instance by the user's email address.
     *
     * <p>If the user does not exist, Spring Security's login flow will throw
     * {@code BadCredentialsException} (not {@code UsernameNotFoundException}),
     * preventing email-enumeration attacks when exception messages are hidden.
     *
     * @param email the email address (used as the Spring Security "username")
     * @return a fully-populated {@link UserDetails} object
     * @throws UsernameNotFoundException if no user with this email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authentication attempt with unknown email: {}", email);
                    return new UsernameNotFoundException(
                            "User not found with email: " + email);
                });

        return buildUserDetails(user);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a domain {@link User} entity into a Spring Security
     * {@link org.springframework.security.core.userdetails.User} object.
     *
     * <p>The Spring Security user's "username" is set to {@code email} so that
     * all JWT subjects and authentication tokens use a stable, unique identifier.
     *
     * @param user the domain user entity
     * @return a Spring Security {@link UserDetails} instance
     */
    private UserDetails buildUserDetails(User user) {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())               // principal = email
                .password(user.getPassword())            // BCrypt-hashed
                .authorities(List.of(authority))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!Boolean.TRUE.equals(user.getIsEnabled()))
                .build();
    }
}
