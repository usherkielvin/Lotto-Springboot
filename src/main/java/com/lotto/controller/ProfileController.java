package com.lotto.controller;

import com.lotto.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("X-User-Id") @NonNull Long userId) {
        try {
            return ResponseEntity.ok(profileService.getProfile(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestHeader("X-User-Id") @NonNull Long userId,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(profileService.updateProfile(userId, body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("X-User-Id") @NonNull Long userId,
            @RequestBody Map<String, String> body) {
        try {
            profileService.changePassword(userId, body.get("currentPassword"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password updated."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
