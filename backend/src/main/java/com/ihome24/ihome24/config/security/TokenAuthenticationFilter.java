package com.ihome24.ihome24.config.security;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
/**
 * Фильтр для аутентификации по Bearer токену (token_userId_timestamp).
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    private static final long TOKEN_TTL_MILLIS = 60L * 60L * 1000L; // 1 час

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token) && !isExpired(token)) {
                Long userId = parseUserId(token);
                if (userId != null) {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        user = userRepository.findByUsernameWithRole(user.getUsername())
                                .orElse(user);
                    }
                    if (user != null && !isTokenIssuedBeforeLastPasswordChange(token, user)) {
                        UserPrincipal principal = UserPrincipal.create(user);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user.getUsername(),
                                        null,
                                        principal.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Token auth failed: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private Long parseUserId(String token) {
        if (token == null || !token.startsWith("token_")) return null;
        String[] parts = token.split("_");
        if (parts.length >= 2) {
            try {
                return Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private boolean isExpired(String token) {
        if (token == null || !token.startsWith("token_")) return true;
        String[] parts = token.split("_");
        if (parts.length < 3) {
            return true;
        }
        try {
            long issuedAt = Long.parseLong(parts[2]);
            long now = System.currentTimeMillis();
            return now - issuedAt > TOKEN_TTL_MILLIS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Если пароль менялся после выдачи токена, старый токен должен стать недействительным.
     */
    private boolean isTokenIssuedBeforeLastPasswordChange(String token, User user) {
        if (token == null || user == null || user.getUpdatedAt() == null) {
            return false;
        }
        String[] parts = token.split("_");
        if (parts.length < 3) {
            return true;
        }
        try {
            long issuedAt = Long.parseLong(parts[2]);
            long updatedAt = user.getUpdatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            return issuedAt < updatedAt;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
