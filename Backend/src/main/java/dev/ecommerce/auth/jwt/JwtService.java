package dev.ecommerce.auth.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private String SECRET_KEY = "e9bafb2bcaa7a7dec93447313079432534ef8088978cd10fae044542f1e477f40";

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(claims) // replaces setClaims()
                .subject(userDetails.getUsername()) // replaces setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 900))
                .signWith(getSignInKey()) // algorithm inferred from key
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isCookieTokenValid(String token, UserDetails userDetails) {
        Claims claims;
        try {
            claims = extractClaims(token);
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        } catch (Exception ignored) { return false; }

        String userName = claims.getSubject();
        Instant expiredTime = claims.getExpiration().toInstant();
        Instant cookiesMaxTime = new Date((Long)claims.get("cookieMaxTime")).toInstant();
        boolean expiredWithin = expiredTime.isBefore(cookiesMaxTime);
        return (userName.equals(userDetails.getUsername())) && expiredWithin;
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
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Cookie makeAuthenticateCookie(UserDetails userDetails) {
        Date cookieMaxTime = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10);
        Map<String, Object> claims = Map.of("cookieMaxTime", cookieMaxTime);
        String token = generateToken(claims, userDetails);
        Cookie cookie = new Cookie("Auth", token);
        cookie.setMaxAge(1000 * 60 * 60 * 10);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
