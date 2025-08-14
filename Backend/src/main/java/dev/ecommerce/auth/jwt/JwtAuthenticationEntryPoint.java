package dev.ecommerce.auth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String loginPage = "http://localhost:8081/login";
        response.addCookie(removeAuthCookie());
        String redirect = (request.getRequestURI().equals(loginPage)) ? "" : ("?r=" + request.getRequestURI());
        response.sendRedirect(loginPage + redirect);
    }

    private Cookie removeAuthCookie() {
        Cookie cookie = new Cookie("auth", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}
