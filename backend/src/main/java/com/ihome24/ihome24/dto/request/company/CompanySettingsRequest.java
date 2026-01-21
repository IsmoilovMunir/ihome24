package com.ihome24.ihome24.dto.request.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanySettingsRequest {

    @NotBlank(message = "Название компании обязательно")
    @Size(max = 255, message = "Название компании не должно превышать 255 символов")
    private String name;

    @Size(max = 20, message = "ИНН не должен превышать 20 символов")
    private String inn;

    @Size(max = 20, message = "ОГРН не должен превышать 20 символов")
    private String ogrn;

    @Size(max = 20, message = "КПП не должен превышать 20 символов")
    private String kpp;

    @NotBlank(message = "Страна обязательна")
    @Size(max = 100, message = "Страна не должна превышать 100 символов")
    private String country = "Russia";

    @Size(max = 500, message = "Юридический адрес не должен превышать 500 символов")
    private String legalAddress;

    @Size(max = 500, message = "Фактический адрес не должен превышать 500 символов")
    private String actualAddress;

    @Size(max = 50, message = "Телефон не должен превышать 50 символов")
    private String phone;

    @Size(max = 100, message = "Email не должен превышать 100 символов")
    private String email;

    @Size(max = 255, message = "Веб-сайт не должен превышать 255 символов")
    private String website;

    @Size(max = 255, message = "ФИО директора не должно превышать 255 символов")
    private String directorName;

    @Size(max = 255, message = "ФИО главного бухгалтера не должно превышать 255 символов")
    private String accountantName;

    @Size(max = 255, message = "Название банка не должно превышать 255 символов")
    private String bankName;

    @Size(max = 50, message = "Расчетный счет не должен превышать 50 символов")
    private String bankAccount;

    @Size(max = 50, message = "Корреспондентский счет не должен превышать 50 символов")
    private String correspondentAccount;

    @Size(max = 20, message = "БИК не должен превышать 20 символов")
    private String bik;
}
