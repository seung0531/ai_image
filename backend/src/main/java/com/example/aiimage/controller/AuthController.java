package com.example.aiimage.controller;

import com.example.aiimage.security.JwtService;
import com.example.aiimage.dto.LoginRequest;
import com.example.aiimage.dto.LoginResponse;
import com.example.aiimage.dto.RegisterRequest;
import com.example.aiimage.dto.RegisterResponse;
import com.example.aiimage.model.User;
import com.example.aiimage.security.AuthCookieService;
import com.example.aiimage.security.AuthenticatedUser;
import com.example.aiimage.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            AuthCookieService authCookieService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);

        RegisterResponse response = new RegisterResponse(
                "회원가입이 완료되었습니다.",
                user.getId(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {
        User user = authService.login(request);

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        ResponseCookie cookie =
                authCookieService.createAccessTokenCookie(token);

        LoginResponse response = new LoginResponse(
                "로그인에 성공했습니다.",
                user.getId(),
                user.getEmail()
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie =
                authCookieService.createLogoutCookie();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        Map.of(
                                "message",
                                "로그아웃되었습니다."
                        )
                );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        return ResponseEntity.ok(
                Map.of(
                        "userId", user.userId(),
                        "email", user.email()
                )
        );
    }
}