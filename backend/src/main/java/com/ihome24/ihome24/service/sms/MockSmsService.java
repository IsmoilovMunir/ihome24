package com.ihome24.ihome24.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsService implements SmsService {

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        log.info("=== MOCK SMS === Код для {}: {} === (в продакшене будет отправлена реальная SMS)", phone, code);
        return true;
    }
}
