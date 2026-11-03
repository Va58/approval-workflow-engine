package com.project.controller;

import com.project.dto.LoginDto;
import com.project.dto.LoginResponse;
import com.project.entity.User;
import com.project.repository.UserRepository;
import com.project.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + dto.getEmail()));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new IllegalArgumentException("Invalid password!");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getEmail(), user.getRole().name());
    }
}
