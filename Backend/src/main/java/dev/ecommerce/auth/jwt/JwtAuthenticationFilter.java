package dev.ecommerce.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // if requesting for authentication then skip token filter
        if (request.getServletPath().startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String headerPrefix = "Bearer ";
        Cookie[] cookies = request.getCookies();

        // if Authorization header doesn't have Bearer token, and have cookie with Auth name,
        // then fail the jwt filter
        if (authHeader == null || !authHeader.startsWith(headerPrefix)) {
            if (cookies == null || checkCookiesHaveName(cookies, "Auth") == -1) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // get first cookie or get authorization header
        String jwtToken = (cookies != null) ? cookies[0].getValue() : authHeader.substring(headerPrefix.length());
        final String username = jwtService.extractUsername(jwtToken);
        boolean isTokenValid = false;

        // every request will have different SecurityContextHolder, will always be null initially,
        // but still check to avoid same thread authentication (next filter)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwtToken, userDetails)) {
                isTokenValid = true;
                setDetailInSecurityContextHolder(userDetails, request);
            }
        }

        // cookie is still valid but the jwt inside has expired as it has shorter duration
        // still authenticate the user but update the jwt with a new one and new cookie time
        if (!isTokenValid && cookies != null && checkCookiesHaveName(cookies, "Auth") != -1) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isCookieTokenValid(jwtToken, userDetails)) {
                isTokenValid = true;
                setDetailInSecurityContextHolder(userDetails, request);
                ResponseCookie cookie = jwtService.makeAuthenticateCookie(userDetails);
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                //response.addCookie(jwtService.makeAuthenticateCookie(userDetails));
            }
        }

        if (!isTokenValid)
            throw new BadCredentialsException("Invalid token");
        filterChain.doFilter(request, response);
    }

    private int checkCookiesHaveName(Cookie[] cookies, String name) {
        for (int j = 0; j < cookies.length; j++) {
            if (cookies[j].getName().equals(name)) {
                return j;
            }
        }
        return -1;
    }

    private void setDetailInSecurityContextHolder(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
