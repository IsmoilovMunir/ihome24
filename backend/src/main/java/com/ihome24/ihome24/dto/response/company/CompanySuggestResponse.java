package com.ihome24.ihome24.dto.response.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySuggestResponse {
    private boolean enabled;
    private List<CompanyPartySuggestion> suggestions;
    /** Пояснение, если поиск недоступен (нет ключа, тариф DaData и т.д.) */
    private String message;
}
