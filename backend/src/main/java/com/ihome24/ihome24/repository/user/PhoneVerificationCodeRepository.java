package com.ihome24.ihome24.repository.user;

import com.ihome24.ihome24.entity.user.PhoneVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PhoneVerificationCodeRepository extends JpaRepository<PhoneVerificationCode, Long> {

    Optional<PhoneVerificationCode> findTopByPhoneAndCodeAndUsedFalseOrderByCreatedAtDesc(
            String phone, String code);

    @Modifying
    @Query("UPDATE PhoneVerificationCode p SET p.used = true WHERE p.phone = :phone AND p.used = false")
    void markAllAsUsedForPhone(@Param("phone") String phone);

    @Modifying
    @Query("DELETE FROM PhoneVerificationCode p WHERE p.expiresAt < :now OR p.used = true")
    void deleteExpiredAndUsed(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(p) FROM PhoneVerificationCode p WHERE p.phone = :phone AND p.createdAt > :since")
    long countByPhoneSince(@Param("phone") String phone, @Param("since") LocalDateTime since);
}
