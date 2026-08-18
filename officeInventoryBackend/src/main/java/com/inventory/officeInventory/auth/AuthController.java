package com.inventory.officeInventory.auth;

import com.inventory.officeInventory.auth.dto.ChangePasswordRequest;
import com.inventory.officeInventory.auth.dto.LoginRequest;
import com.inventory.officeInventory.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {

        String username = authentication.getName();

        authService.changePassword(
                username,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Password changed successfully."
                )
        );
    }
}