package com.groceryshop.controller;

import com.groceryshop.dto.ApiResponse;
import com.groceryshop.dto.AuthRequest;
import com.groceryshop.model.User;
import com.groceryshop.service.EmailService;
import com.groceryshop.service.FirestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private FirestoreService firestoreService;

    @PostMapping("/register-admin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerAdmin(@Valid @RequestBody AuthRequest request) {
        try {
            // Check if admin already exists
            List<Map<String, Object>> existingAdmins = firestoreService.findByField("users", "role", "ADMIN");
            if (!existingAdmins.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Admin already exists"));
            }

            // Create admin user
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("email", request.getEmail());
            adminData.put("name", request.getName());
            adminData.put("phone", request.getPhone());
            adminData.put("role", "ADMIN");
            adminData.put("isVerified", true);
            adminData.put("createdAt", LocalDateTime.now().toString());
            adminData.put("updatedAt", LocalDateTime.now().toString());

            String adminId = firestoreService.save("users", adminData);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", adminId);
            response.put("email", request.getEmail());
            response.put("role", "ADMIN");

            return ResponseEntity.ok(ApiResponse.success("Admin registered successfully", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOTP(@Valid @RequestBody AuthRequest request) {
        try {
            String otp = emailService.generateAndSendOTP(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", otp));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody AuthRequest request) {
        try {
            // Verify OTP
            if (!emailService.verifyOTP(request.getEmail(), request.getOtp())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid OTP"));
            }

            // Check if user already exists
            List<Map<String, Object>> existingUsers = firestoreService.findByField("users", "email", request.getEmail());
            if (!existingUsers.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User already exists"));
            }

            // Create customer user
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", request.getEmail());
            userData.put("name", request.getName());
            userData.put("phone", request.getPhone());
            userData.put("role", "CUSTOMER");
            userData.put("isVerified", true);
            userData.put("createdAt", LocalDateTime.now().toString());
            userData.put("updatedAt", LocalDateTime.now().toString());

            String userId = firestoreService.save("users", userData);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("email", request.getEmail());
            response.put("name", request.getName());
            response.put("role", "CUSTOMER");

            return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody AuthRequest request) {
        try {
            // Verify OTP
            if (!emailService.verifyOTP(request.getEmail(), request.getOtp())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid OTP"));
            }

            // Find user
            List<Map<String, Object>> users = firestoreService.findByField("users", "email", request.getEmail());
            if (users.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User not found"));
            }

            Map<String, Object> user = users.get(0);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.get("id"));
            response.put("email", user.get("email"));
            response.put("name", user.get("name"));
            response.put("role", user.get("role"));

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
}
