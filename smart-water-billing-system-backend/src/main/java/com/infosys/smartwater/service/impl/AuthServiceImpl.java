package com.infosys.smartwater.service.impl;

import com.infosys.smartwater.config.JwtProperties;
import com.infosys.smartwater.dto.request.LoginRequest;
import com.infosys.smartwater.dto.request.UserRegistrationRequest;
import com.infosys.smartwater.dto.response.AuthResponse;
import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.ApprovalStatus;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.UserMapper;
import com.infosys.smartwater.repository.UserRepository;
import com.infosys.smartwater.service.AuthService;
import com.infosys.smartwater.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Implementation of {@link AuthService}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    @Override
    public AuthResponse register(UserRegistrationRequest request) {
        log.info("Processing public Building Owner registration application for email: '{}'", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        Role requestedRole = request.getRole() != null ? request.getRole() : Role.BUILDING_OWNER;

        if (requestedRole == Role.SUPERADMIN || requestedRole == Role.RESIDENT) {
            throw new InvalidOperationException(
                    "Public self-registration is only allowed for Building Owners. " +
                    "Superadmin accounts are built-in, and Resident accounts are provisioned by Building Owners."
            );
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.BUILDING_OWNER)
                .approvalStatus(ApprovalStatus.PENDING)
                .isEnabled(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Building Owner application submitted — id={}, email='{}', status=PENDING", savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(null)
                .tokenType("Bearer")
                .expiresIn(0L)
                .userId(savedUser.getId().toString())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Authentication attempt for email/username: '{}'", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> userRepository.findByUsername(request.getEmail())
                        .orElseThrow(() -> new InvalidOperationException("Invalid email or password")));

        if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new InvalidOperationException("Your Building Owner registration application is pending approval by Superadmin.");
        }

        if (user.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new InvalidOperationException("Your registration application was rejected by Superadmin.");
        }

        if (!Boolean.TRUE.equals(user.getIsEnabled())) {
            throw new InvalidOperationException("Account is disabled. Please contact system administrator.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidOperationException("Invalid email or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtils.generateToken(Map.of("role", user.getRole().name()), userDetails);

        log.info("User authenticated successfully — email='{}', role='{}'", user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration())
                .userId(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new InvalidOperationException("No authenticated user found in context");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return userMapper.toResponse(user);
    }
}
