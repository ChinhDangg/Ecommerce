package dev.ecommerce.auth.jwt;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private String secretKey;

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 900))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
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
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
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
