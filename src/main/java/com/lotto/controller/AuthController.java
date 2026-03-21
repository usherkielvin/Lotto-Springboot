package com.lotto.controller;

import com.lotto.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.login(body.get("username"), body.get("password")));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/demo")
    public ResponseEntity<?> demo() {
        try {
            return ResponseEntity.ok(authService.demoLogin());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.register(
                    body.get("username"),
                    body.get("password"),
                    body.get("displayName")
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless JWT — nothing to invalidate server-side.
        // Client clears its own session; this endpoint exists so the app doesn't 404.
        return ResponseEntity.ok(Map.of("message", "Logged out."));
    }

    @GetMapping("/hash")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return ResponseEntity.ok(Map.of("hash", encoder.encode(password)));
    }
}
