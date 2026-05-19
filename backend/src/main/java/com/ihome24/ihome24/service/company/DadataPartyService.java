package com.ihome24.ihome24.service.company;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome24.ihome24.dto.response.company.CompanyPartySuggestion;
import com.ihome24.ihome24.dto.response.company.CompanySuggestResponse;
import com.ihome24.ihome24.config.EnvLoader;
import com.ihome24.ihome24.util.CompanyRequisitesFormat;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DadataPartyService {

    private static final String SUGGEST_URL = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/party";
    private static final String FIND_URL = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/findById/party";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Environment environment;

    private String apiKey;

    public DadataPartyService(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void logDadataStatus() {
        apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("DADATA_API_KEY не задан — поиск организаций в checkout отключён. "
                    + "Локально: backend/.env; prod: .env.prod + environment в docker-compose.prod.yml.");
        } else {
            log.info("DaData: ключ загружен ({}…), подсказки по организациям включены",
                    apiKey.substring(0, Math.min(8, apiKey.length())));
        }
    }

    private String resolveApiKey() {
        String fromFile = EnvLoader.lookup("DADATA_API_KEY");
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile.trim();
        }
        String fromProp = System.getProperty("DADATA_API_KEY");
        if (fromProp != null && !fromProp.isBlank()) {
            return fromProp.trim();
        }
        String fromEnv = System.getenv("DADATA_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromSpring = environment.getProperty("app.dadata.api-key");
        if (fromSpring != null && !fromSpring.isBlank()) {
            return fromSpring.trim();
        }
        fromSpring = environment.getProperty("DADATA_API_KEY");
        if (fromSpring != null && !fromSpring.isBlank()) {
            return fromSpring.trim();
        }
        return "";
    }

    public CompanySuggestResponse suggest(String query, int count) {
        String key = apiKey != null && !apiKey.isBlank() ? apiKey : resolveApiKey();
        if (key == null || key.isBlank()) {
            return CompanySuggestResponse.builder()
                    .enabled(false)
                    .suggestions(List.of())
                    .message("Не настроен DADATA_API_KEY. Локально: backend/.env. Prod: infrastructure/.env.prod и DADATA_API_KEY в docker-compose.prod.yml → перезапуск backend.")
                    .build();
        }
        String q = query != null ? query.trim() : "";
        if (q.length() < 2) {
            return CompanySuggestResponse.builder()
                    .enabled(true)
                    .suggestions(List.of())
                    .build();
        }

        int limit = Math.min(Math.max(count, 1), 20);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + key.trim());
            headers.set("Accept", "application/json");

            Map<String, Object> body = Map.of(
                    "query", q,
                    "count", limit
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String raw = restTemplate.postForObject(SUGGEST_URL, entity, String.class);
            if (raw == null || raw.isBlank()) {
                return emptyEnabled();
            }

            JsonNode root = objectMapper.readTree(raw);
            if (root.has("family") && "CLIENT_ERROR".equals(root.path("family").asText())) {
                String msg = root.path("message").asText("Ошибка DaData");
                log.warn("DaData API: {}", msg);
                if (msg.toLowerCase().contains("disabled")) {
                    return disabled("Тариф DaData ещё не активирован (на бесплатном тарифе проверка до 24 ч). "
                            + "Пока можно заполнить реквизиты вручную ниже.");
                }
                return disabled(msg);
            }

            JsonNode suggestions = root.path("suggestions");
            List<CompanyPartySuggestion> items = new ArrayList<>();
            if (suggestions.isArray()) {
                for (JsonNode node : suggestions) {
                    CompanyPartySuggestion item = mapSuggestion(node);
                    if (item != null && item.getInn() != null && !item.getInn().isBlank()) {
                        items.add(item);
                    }
                }
            }
            return CompanySuggestResponse.builder()
                    .enabled(true)
                    .suggestions(items)
                    .build();
        } catch (HttpStatusCodeException e) {
            String detail = parseDadataError(e);
            log.warn("DaData HTTP {}: {}", e.getStatusCode().value(), detail);
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                String hint = detail != null && detail.toLowerCase().contains("disabled")
                        ? "Тариф DaData ещё не активирован (на бесплатном тарифе проверка до 24 ч). "
                        + "Пока можно заполнить реквизиты вручную ниже."
                        : "Ключ DaData без доступа к подсказкам. Проверьте API-ключ в dadata.ru → Профиль.";
                return disabled(hint);
            }
            return CompanySuggestResponse.builder()
                    .enabled(true)
                    .suggestions(List.of())
                    .message("Сервис подсказок временно недоступен. Заполните реквизиты вручную.")
                    .build();
        } catch (Exception e) {
            log.warn("DaData suggest failed: {}", e.getMessage());
            return CompanySuggestResponse.builder()
                    .enabled(true)
                    .suggestions(List.of())
                    .build();
        }
    }

    private static String parseDadataError(HttpStatusCodeException e) {
        try {
            JsonNode root = new ObjectMapper().readTree(e.getResponseBodyAsString());
            if (root.has("message")) {
                return root.path("message").asText();
            }
        } catch (Exception ignored) {
        }
        return e.getMessage();
    }

    private static CompanySuggestResponse disabled(String message) {
        return CompanySuggestResponse.builder()
                .enabled(false)
                .suggestions(List.of())
                .message(message)
                .build();
    }

    private static CompanySuggestResponse emptyEnabled() {
        return CompanySuggestResponse.builder()
                .enabled(true)
                .suggestions(List.of())
                .build();
    }

    /** Полные реквизиты по ИНН или ОГРН (DaData findById/party). */
    public CompanyPartySuggestion findParty(String query) {
        String key = apiKey != null && !apiKey.isBlank() ? apiKey : resolveApiKey();
        if (key == null || key.isBlank()) {
            return null;
        }
        String q = query != null ? query.trim().replaceAll("\\D", "") : "";
        if (q.length() != 10 && q.length() != 12 && q.length() != 13 && q.length() != 15) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + key.trim());
            headers.set("Accept", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("query", q), headers);
            String raw = restTemplate.postForObject(FIND_URL, entity, String.class);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            JsonNode root = objectMapper.readTree(raw);
            JsonNode suggestions = root.path("suggestions");
            if (!suggestions.isArray() || suggestions.isEmpty()) {
                return null;
            }
            return mapSuggestion(suggestions.get(0));
        } catch (Exception e) {
            log.warn("DaData findParty failed: {}", e.getMessage());
            return null;
        }
    }

    private static CompanyPartySuggestion mapSuggestion(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        JsonNode data = node.path("data");
        String name = textOrNull(node.path("value"));
        if (name == null) {
            name = textOrNull(data.path("name").path("short_with_opf"));
        }
        if (name == null) {
            name = textOrNull(data.path("name").path("full_with_opf"));
        }

        String address = CompanyRequisitesFormat.buildAddressFromDadata(data.path("address"));
        JsonNode finance = data.path("finance");

        return CompanyPartySuggestion.builder()
                .name(name)
                .inn(CompanyRequisitesFormat.formatInn(textOrNull(data.path("inn"))))
                .kpp(CompanyRequisitesFormat.formatKpp(textOrNull(data.path("kpp"))))
                .ogrn(CompanyRequisitesFormat.formatOgrn(textOrNull(data.path("ogrn"))))
                .okpo(CompanyRequisitesFormat.formatOkpo(textOrNull(data.path("okpo"))))
                .address(address)
                .bik(CompanyRequisitesFormat.formatBik(textOrNull(finance.path("bik"))))
                .settlementAccount(CompanyRequisitesFormat.formatBankAccount(textOrNull(finance.path("account"))))
                .corrAccount(CompanyRequisitesFormat.formatBankAccount(
                        textOrNull(finance.path("correspondent_account"))))
                .type(textOrNull(data.path("type")))
                .build();
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String s = node.asText(null);
        return s != null && !s.isBlank() ? s.trim() : null;
    }
}
