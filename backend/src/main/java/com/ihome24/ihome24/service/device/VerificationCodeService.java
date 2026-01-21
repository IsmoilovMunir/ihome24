package com.ihome24.ihome24.service.device;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.entity.user.VerificationCode;
import com.ihome24.ihome24.repository.user.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Генерирует 6-значный код подтверждения (от 100000 до 999999)
     */
    public String generateCode() {
        int code = 100000 + random.nextInt(900000); // 100000-999999
        String codeStr = String.valueOf(code);
        // Гарантируем, что код всегда 6-значный
        int attempts = 0;
        while (codeStr.length() != 6 && attempts < 10) {
            log.warn("Generated code length is not 6: {} (length: {}), regenerating...", codeStr, codeStr.length());
            code = 100000 + random.nextInt(900000);
            codeStr = String.valueOf(code);
            attempts++;
        }
        if (codeStr.length() != 6) {
            log.error("Failed to generate 6-digit code after {} attempts. Generated: {} (length: {})", attempts, codeStr, codeStr.length());
            // Форсируем 6-значный код
            codeStr = String.format("%06d", code % 1000000);
        }
        // Маскируем код в логах (показываем только первые 2 символа)
        String maskedCode = codeStr.length() >= 2 ? codeStr.substring(0, 2) + "****" : "******";
        log.info("Generated 6-digit verification code: {} (length: {})", maskedCode, codeStr.length());
        return codeStr;
    }

    /**
     * Создает и сохраняет код подтверждения для устройства
     */
    @Transactional
    public VerificationCode createVerificationCode(User user, String deviceFingerprint, String ipAddress) {
        // Помечаем все предыдущие коды для этого устройства как использованные
        verificationCodeRepository.markAllCodesAsUsedForDevice(user, deviceFingerprint);
        
        String code = generateCode();
        // Маскируем код в логах
        String maskedCode = code.length() >= 2 ? code.substring(0, 2) + "****" : "******";
        log.info("Creating verification code for user: {}, deviceFingerprint: {}, code: {}", 
                user.getUsername(), deviceFingerprint, maskedCode);
        
        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .deviceFingerprint(deviceFingerprint)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        
        verificationCode = verificationCodeRepository.save(verificationCode);
        // Маскируем код в логах (переиспользуем переменную)
        maskedCode = verificationCode.getCode().length() >= 2 
                ? verificationCode.getCode().substring(0, 2) + "****" 
                : "******";
        log.info("Verification code saved: id={}, code={}, expiresAt={}, deviceFingerprint={}", 
                verificationCode.getId(), maskedCode, 
                verificationCode.getExpiresAt(), verificationCode.getDeviceFingerprint());
        
        return verificationCode;
    }

    /**
     * Проверяет код подтверждения
     */
    @Transactional(readOnly = true)
    public Optional<VerificationCode> verifyCode(User user, String code, String deviceFingerprint) {
        // Маскируем код в логах
        String maskedCode = code != null && code.length() >= 2 ? code.substring(0, 2) + "****" : "******";
        log.info("Verifying code for user: {} (id: {}), code: {}, deviceFingerprint: {}", 
                user.getUsername(), user.getId(), maskedCode, deviceFingerprint);
        
        // Сначала попробуем найти код с точным совпадением
        Optional<VerificationCode> codeOpt = verificationCodeRepository
                .findByUserAndCodeAndDeviceFingerprintAndUsedFalse(user, code, deviceFingerprint);
        
        if (codeOpt.isPresent()) {
            VerificationCode verificationCode = codeOpt.get();
            // Маскируем код в логах (переиспользуем переменную)
            maskedCode = verificationCode.getCode().length() >= 2 
                    ? verificationCode.getCode().substring(0, 2) + "****" 
                    : "******";
            log.info("Found verification code: id={}, code={}, expiresAt={}, used={}, isValid={}, deviceFingerprint={}", 
                    verificationCode.getId(), maskedCode, verificationCode.getExpiresAt(), 
                    verificationCode.getUsed(), verificationCode.isValid(), verificationCode.getDeviceFingerprint());
            
            if (verificationCode.isValid()) {
                log.info("Verification code is valid for user: {}", user.getUsername());
                return codeOpt;
            } else {
                log.warn("Verification code expired or invalid for user: {}, device: {}, expiresAt: {}, now: {}, used: {}", 
                        user.getUsername(), deviceFingerprint, verificationCode.getExpiresAt(), 
                        LocalDateTime.now(), verificationCode.getUsed());
            }
        } else {
            log.warn("Verification code not found with exact match. User: {}, code: {}, deviceFingerprint: {}", 
                    user.getUsername(), code, deviceFingerprint);
            log.warn("This could mean: 1) Code doesn't exist, 2) Code already used, 3) Device fingerprint mismatch, 4) Code expired");
        }
        
        return Optional.empty();
    }

    /**
     * Помечает код как использованный
     */
    @Transactional
    public void markCodeAsUsed(VerificationCode verificationCode) {
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);
    }

    /**
     * Очищает истекшие коды
     */
    @Transactional
    public void cleanupExpiredCodes() {
        verificationCodeRepository.deleteExpiredAndUsedCodes(LocalDateTime.now());
    }
}
