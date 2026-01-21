package com.ihome24.ihome24.service.device;

import com.ihome24.ihome24.entity.user.Device;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;

    /**
     * Генерирует уникальный fingerprint устройства на основе User-Agent и IP
     */
    public String generateDeviceFingerprint(String userAgent, String ipAddress) {
        // Простой fingerprint на основе user-agent и IP
        // В продакшене можно использовать более сложные методы (canvas fingerprint, WebGL и т.д.)
        String fingerprint = (userAgent != null ? userAgent : "") + "|" + (ipAddress != null ? ipAddress : "");
        return String.valueOf(fingerprint.hashCode());
    }

    /**
     * Проверяет, является ли устройство новым для пользователя
     */
    @Transactional(readOnly = true)
    public boolean isNewDevice(User user, String deviceFingerprint) {
        return !deviceRepository.existsByUserAndDeviceFingerprint(user, deviceFingerprint);
    }

    /**
     * Сохраняет или обновляет информацию об устройстве
     */
    @Transactional
    public Device saveOrUpdateDevice(User user, String deviceFingerprint, String userAgent, 
                                     String ipAddress, String deviceName, String browser, String os) {
        Optional<Device> existingDevice = deviceRepository.findByUserAndDeviceFingerprint(user, deviceFingerprint);
        
        if (existingDevice.isPresent()) {
            Device device = existingDevice.get();
            device.setLastLoginAt(LocalDateTime.now());
            if (userAgent != null) device.setUserAgent(userAgent);
            if (ipAddress != null) device.setIpAddress(ipAddress);
            if (deviceName != null) device.setDeviceName(deviceName);
            if (browser != null) device.setBrowser(browser);
            if (os != null) device.setOs(os);
            return deviceRepository.save(device);
        } else {
            Device newDevice = Device.builder()
                    .user(user)
                    .deviceFingerprint(deviceFingerprint)
                    .userAgent(userAgent)
                    .ipAddress(ipAddress)
                    .deviceName(deviceName)
                    .browser(browser)
                    .os(os)
                    .isTrusted(false)
                    .lastLoginAt(LocalDateTime.now())
                    .build();
            return deviceRepository.save(newDevice);
        }
    }

    /**
     * Помечает устройство как доверенное
     */
    @Transactional
    public void markDeviceAsTrusted(User user, String deviceFingerprint) {
        Optional<Device> deviceOpt = deviceRepository.findByUserAndDeviceFingerprint(user, deviceFingerprint);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setIsTrusted(true);
            deviceRepository.save(device);
            log.info("Device marked as trusted for user: {}, fingerprint: {}", user.getUsername(), deviceFingerprint);
        }
    }

    /**
     * Извлекает информацию о браузере и ОС из User-Agent
     */
    public String extractBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("Opera")) return "Opera";
        return "Unknown";
    }

    public String extractOS(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iOS")) return "iOS";
        return "Unknown";
    }
}
