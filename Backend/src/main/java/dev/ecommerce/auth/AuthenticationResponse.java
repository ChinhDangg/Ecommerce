package dev.ecommerce.auth;

import jakarta.servlet.http.Cookie;

public record AuthenticationResponse(
        Cookie cookie
) {
}
