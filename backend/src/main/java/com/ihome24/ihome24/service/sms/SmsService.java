package com.ihome24.ihome24.service.sms;

/**
 * Сервис отправки SMS.
 * В dev используется MockSmsService (логирует код в консоль).
 * В prod — реализация через sms.ru, smsc.ru, Twilio и т.д.
 */
public interface SmsService {
    /**
     * Отправить SMS с кодом подтверждения на указанный номер.
     * @param phone Номер в формате 79991234567
     * @param code 4-значный код
     * @return true если отправлено успешно
     */
    boolean sendVerificationCode(String phone, String code);
}
