package com.ihome24.ihome24.repository.user;

import com.ihome24.ihome24.entity.user.PhoneAuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PhoneAuthSessionRepository extends JpaRepository<PhoneAuthSession, Long> {

    Optional<PhoneAuthSession> findByTokenAndExpiresAtAfter(String token, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM PhoneAuthSession p WHERE p.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
