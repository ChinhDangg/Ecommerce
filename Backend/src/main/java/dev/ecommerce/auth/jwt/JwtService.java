package dev.ecommerce.auth.jwt;

import java.util.*;

import dev.ecommerce.configuration.RsaKeyProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RsaKeyProperties rsaKey;

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + (1000 * 60 * 30)); // 30 minutes for token expiry

        return Jwts.builder()
                .header()                    // --- begin header edits ---
                    .keyId(rsaKey.getKid())              // sets "kid" header
                .and()
                .issuer("issuer")
                .claims(claims) // replaces setClaims()
                .subject(userDetails.getUsername()) // replaces setSubject()
                .issuedAt(now)
                .expiration(exp) // 15 minutes for token expiry
                .signWith(rsaKey.getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (Exception ignored) {}
        return null;
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaims(token).getExpiration();
        } catch(ExpiredJwtException e) {
            return e.getClaims().getExpiration();
        } catch (Exception ignored) {}
        return null;
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(rsaKey.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

   public static final String AUTH_COOKIE_NAME = "Auth";

    public ResponseCookie makeAuthenticateCookie(UserDetails userDetails) {
        long maxAgeSeconds = 60 * 60; // 1 hour
        Date cookieMaxTime = new Date(System.currentTimeMillis() + (maxAgeSeconds * 1000));

        // Extract roles from UserDetails
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // e.g. "USER", "ADMIN"
                .toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("cookieMaxTime", cookieMaxTime);
        claims.put("scope", roles);
        String token = generateToken(claims, userDetails);

        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // change to true upon production with https enable
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public static Cookie removeAuthCookie() {
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}
