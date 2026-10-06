package com.example.aiimage.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = getTokenFromCookie(request);

        System.out.println("==============================");
        System.out.println("요청 URI: " + request.getRequestURI());
        System.out.println("요청 메서드: " + request.getMethod());
        System.out.println("JWT 쿠키 존재 여부: " + (token != null));

        try {
            Authentication currentAuthentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (
                    token != null
                            && currentAuthentication == null
                            && jwtService.isValid(token)
            ) {
                String userId = jwtService.getUserId(token);
                String email = jwtService.getEmail(token);

                AuthenticatedUser principal =
                        new AuthenticatedUser(userId, email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of()
                        );

                SecurityContext securityContext =
                        SecurityContextHolder.createEmptyContext();

                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);

                System.out.println("JWT 인증 성공: " + email);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();

            System.out.println(
                    "JWT 인증 실패: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage()
            );
        }

        Authentication result =
                SecurityContextHolder.getContext().getAuthentication();

        System.out.println("최종 인증 객체 존재: " + (result != null));
        System.out.println(
                "최종 인증 상태: "
                        + (result != null && result.isAuthenticated())
        );
        System.out.println("==============================");

        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(
            HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (
                    AuthCookieService.COOKIE_NAME.equals(
                            cookie.getName()
                    )
            ) {
                return cookie.getValue();
            }
        }

        return null;
    }
}