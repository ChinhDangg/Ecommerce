package dev.ecommerce.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Allow auth endpoints and CORS preflight to pass through
        String path = request.getServletPath();
        if (path.startsWith("/api/auth/") || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String bearer = "Bearer ";
        final Cookie[] cookies = request.getCookies();

        // Prefer Authorization header; if absent, fall back to Auth cookie
        String token = null;
        if (header != null && header.regionMatches(true, 0, bearer, 0, bearer.length())) {
            String candidate = header.substring(bearer.length()).trim();
            if (!candidate.isEmpty()) token = candidate;
        }
        if (token == null && cookies != null) {
            Cookie authCookie = getCookieByName(cookies, JwtService.AUTH_COOKIE_NAME);
            if (authCookie != null && authCookie.getValue() != null && !authCookie.getValue().isBlank()) {
                token = authCookie.getValue();
            }
        }

        // If no credentials, continue as anonymous
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validate signature & expiry first (your jwtService should do full verification)
            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Optional: check revocation/blacklist/jti here
                if (jwtService.isTokenValid(token, userDetails)) {
                    setAuth(userDetails, request);
                } else {
                    // Token present but invalid -> reject
                    throw new BadCredentialsException("Invalid token");
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Access token expired — do NOT refresh here. Let client call /api/auth/refresh.
            throw new BadCredentialsException("Token expired", e);
        } catch (Exception e) {
            // Any other parsing/validation failure
            throw new BadCredentialsException("Invalid token", e);
        }

        filterChain.doFilter(request, response);
    }

    private Cookie getCookieByName(Cookie[] cookies, String name) {
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c;
        }
        return null;
    }

    private void setAuth(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

}
