package com.ihome24.ihome24.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenService {

    private final Key signingKey;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    /**
     * Храним момент старта текущего экземпляра приложения.
     * Это позволяет сделать JWT "валидным только после текущего запуска",
     * т.к. при перезапуске старые токены должны стать невалидными.
     */
    private final Instant serverStartTime;

    public JwtTokenService(
            @Value("${app.security.jwt.secret}") String base64Secret,
            @Value("${app.security.jwt.access-ttl-seconds:900}") long accessTokenTtlSeconds,
            @Value("${app.security.jwt.refresh-ttl-seconds:1209600}") long refreshTokenTtlSeconds // 14 days
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.serverStartTime = Instant.now();
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenTtlSeconds, Map.of(
                "type", "access",
                "authorities", extractAuthorities(userDetails)
        ));
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenTtlSeconds, Map.of("type", "refresh"));
    }

    private String generateToken(UserDetails userDetails, long ttlSeconds, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            boolean usernameMatches = username.equals(userDetails.getUsername());
            boolean notExpired = !isTokenExpired(token);
            boolean issuedAfterServerStart = !isTokenIssuedAtBeforeServerStart(token);
            return usernameMatches && notExpired && issuedAfterServerStart;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenIssuedAtBeforeServerStart(String token) {
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        if (issuedAt == null)
            return true; // нет iat => считаем токен невалидным
        return issuedAt.toInstant().isBefore(serverStartTime);
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private String extractAuthorities(UserDetails userDetails) {
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}

