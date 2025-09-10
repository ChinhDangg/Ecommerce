package dev.ecommerce.auth.jwt;

import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import dev.ecommerce.configuration.RsaKeyProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

        String kid = computeKid(rsaKey.publicKey());

        return Jwts.builder()
                .header()                    // --- begin header edits ---
                    .keyId(kid)              // sets "kid" header
                .and()
                .claims(claims) // replaces setClaims()
                .subject(userDetails.getUsername()) // replaces setSubject()
                .issuedAt(now)
                .expiration(exp) // 15 minutes for token expiry
                .signWith(rsaKey.privateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public static String computeKid(RSAPublicKey publicKey) {
        try {
            byte[] der = publicKey.getEncoded(); // X.509 SubjectPublicKeyInfo
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(der);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute kid", e);
        }
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
                .verifyWith(rsaKey.publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

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

        return ResponseCookie.from("Auth", token)
                .httpOnly(true)
                .secure(false) // change to true upon production with https enable
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
