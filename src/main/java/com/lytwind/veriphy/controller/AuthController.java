package com.lytwind.veriphy.controller;

import com.lytwind.veriphy.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    // In a real app, this would be a POST login taking a username/password!
    @GetMapping("/token")
    public ResponseEntity<String> getAdminToken() {
        String token = jwtService.generateToken("SuperAdmin");
        return ResponseEntity.ok(token);
    }
}
