package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.AuthRequest;
import com.github.fjbaldon.attendex.backend.dto.AuthResponse;
import com.github.fjbaldon.attendex.backend.dto.RegisterRequest;
import com.github.fjbaldon.attendex.backend.security.JwtService;
import com.github.fjbaldon.attendex.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt, "Bearer"));
    }

    @PostMapping("/register-organization")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerNewOrganization(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        authService.verifyUser(token);
        return ResponseEntity.ok("Account verified successfully! You can now log in.");
    }
}
