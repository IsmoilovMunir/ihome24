package com.ihome24.ihome24.service.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Реальная отправка СМС через внешний провайдер sms.ru.
 *
 * Документация: https://sms.ru/api/send
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "smsru")
public class SmsRuService implements SmsService {

    @Value("${app.sms.smsru.api-id}")
    private String apiId;

    @Value("${app.sms.smsru.url:https://sms.ru/sms/send}")
    private String smsRuUrl;

    /**
     * Буквенный отправитель, одобренный в личном кабинете sms.ru.
     * Может быть пустым — тогда используется отправитель по умолчанию.
     */
    @Value("${app.sms.smsru.from:}")
    private String from;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        if (apiId == null || apiId.isBlank()) {
            log.error("SMS.ru api-id is not configured (property app.sms.smsru.api-id)");
            return false;
        }

        String message = "Код подтверждения iHome24: " + code;

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_id", apiId.trim());
        params.add("to", phone);
        params.add("msg", message);
        params.add("json", "1");
        if (from != null && !from.isBlank()) {
            params.add("from", from.trim());
        }

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(smsRuUrl, params, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("SMS.ru request failed. HTTP status: {}, body: {}", response.getStatusCode(), response.getBody());
                return false;
            }

            Map<?, ?> body = response.getBody();
            Object status = body.get("status");
            if (!"OK".equals(status)) {
                log.error("SMS.ru returned error status: {} full body: {}", status, body);
                return false;
            }

            Object smsObj = body.get("sms");
            if (smsObj instanceof Map<?, ?> smsMap) {
                Object phoneInfo = smsMap.get(phone);
                if (phoneInfo instanceof Map<?, ?> infoMap) {
                    Object smsStatus = infoMap.get("status");
                    if ("OK".equals(smsStatus)) {
                        log.info("SMS.ru: SMS sent successfully to {} (sms_id={})", phone, infoMap.get("sms_id"));
                        return true;
                    } else {
                        log.error("SMS.ru: failed to send SMS to {}. status={}, info={}", phone, smsStatus, infoMap);
                        return false;
                    }
                }
            }

            log.warn("SMS.ru: unexpected response format for phone {}: {}", phone, body);
            return false;
        } catch (Exception e) {
            log.error("SMS.ru: exception while sending SMS to {}: {}", phone, e.getMessage(), e);
            return false;
        }
    }
}

