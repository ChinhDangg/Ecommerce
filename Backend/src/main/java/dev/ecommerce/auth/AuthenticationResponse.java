package dev.ecommerce.auth;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;

public record AuthenticationResponse(
        ResponseCookie cookie
) {
}
