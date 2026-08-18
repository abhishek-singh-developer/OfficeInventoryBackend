package com.inventory.officeInventory.auth;

import com.inventory.officeInventory.auth.dto.LoginRequest;
import com.inventory.officeInventory.auth.dto.LoginResponse;
import com.inventory.officeInventory.repository.UserRepository;
import com.inventory.officeInventory.security.JwtService;
import com.inventory.officeInventory.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // LOGIN
    // =========================================================

    public LoginResponse login(LoginRequest request) {

        log.info(
                "Login attempt for username: {}",
                request.getUsername()
        );

        var authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        String token =
                jwtService.generateToken(authentication);

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("");

        log.info(
                "Login successful for username: {}, role: {}",
                request.getUsername(),
                role
        );

        return new LoginResponse(
                token,
                request.getUsername(),
                role
        );
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    public void changePassword(
            String username,
            String currentPassword,
            String newPassword
    ) {

        log.info(
                "Change password request for username: {}",
                username
        );

        // -----------------------------------------------------
        // Find logged-in user
        // -----------------------------------------------------

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        // -----------------------------------------------------
        // Validate current password
        // -----------------------------------------------------

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        )) {

            log.warn(
                    "Incorrect current password for username: {}",
                    username
            );

            throw new RuntimeException(
                    "Current password is incorrect."
            );
        }

        // -----------------------------------------------------
        // Prevent using same password
        // -----------------------------------------------------

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {

            log.warn(
                    "User attempted to reuse current password: {}",
                    username
            );

            throw new RuntimeException(
                    "New password must be different from current password."
            );
        }

        // -----------------------------------------------------
        // Save new password
        // -----------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        log.info(
                "Password changed successfully for username: {}",
                username
        );
    }
}