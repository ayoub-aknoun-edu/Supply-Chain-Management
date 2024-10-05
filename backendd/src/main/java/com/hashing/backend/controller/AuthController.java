package com.hashing.backend.controller;

import com.hashing.backend.model.AuthenticationResponse;
import com.hashing.backend.model.LoginRequest;
import com.hashing.backend.model.User;
import com.hashing.backend.security.CustomUserDetails;
import com.hashing.backend.security.JwtUtil;
import com.hashing.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserService userService;

    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        final String jwt = jwtUtil.generateToken(userDetails);
        final String role = String.valueOf(userService.findByUsername(loginRequest.getUsername()).getRole());

        return ResponseEntity.ok(new AuthenticationResponse(jwt, role));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) throws Exception {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }
}
