package com.ihome24.ihome24.repository.user;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.entity.user.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByUserAndCodeAndDeviceFingerprintAndUsedFalse(
            User user, String code, String deviceFingerprint);
    
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiresAt < :now OR v.used = true")
    void deleteExpiredAndUsedCodes(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE VerificationCode v SET v.used = true WHERE v.user = :user AND v.deviceFingerprint = :deviceFingerprint AND v.used = false")
    void markAllCodesAsUsedForDevice(@Param("user") User user, @Param("deviceFingerprint") String deviceFingerprint);
}
