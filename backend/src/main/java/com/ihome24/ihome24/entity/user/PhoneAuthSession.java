package com.ihome24.ihome24.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сессия для завершения регистрации после верификации телефона.
 * Создаётся при verify-code, когда пользователь не найден.
 */
@Entity
@Table(name = "phone_auth_sessions", indexes = {
    @Index(name = "idx_phone_auth_token", columnList = "token"),
    @Index(name = "idx_phone_auth_expires", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneAuthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(10);
        }
    }

    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiresAt);
    }
}
