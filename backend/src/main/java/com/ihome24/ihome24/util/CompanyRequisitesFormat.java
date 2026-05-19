package com.ihome24.ihome24.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/** Нормализация реквизитов организации (ЕГРЮЛ / DaData) для хранения и отображения. */
public final class CompanyRequisitesFormat {

    private CompanyRequisitesFormat() {
    }

    public static String digitsOnly(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String d = raw.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }

    /** ИНН: 10 (юрлицо) или 12 (ИП) цифр, без пробелов. */
    public static String formatInn(String raw) {
        return digitsOnly(raw);
    }

    /** КПП: 9 цифр, с ведущими нулями. */
    public static String formatKpp(String raw) {
        String d = digitsOnly(raw);
        if (d == null) {
            return null;
        }
        if (d.length() > 9) {
            return d.substring(0, 9);
        }
        return d.length() == 9 ? d : String.format("%09d", Long.parseLong(d));
    }

    /**
     * ОГРН: 13 цифр (ИП) или 15 (юрлицо).
     */
    public static String formatOgrn(String raw) {
        String d = digitsOnly(raw);
        if (d == null) {
            return null;
        }
        if (d.length() <= 13) {
            return String.format("%013d", Long.parseLong(d));
        }
        if (d.length() >= 15) {
            return d.length() == 15 ? d : d.substring(0, 15);
        }
        return String.format("%015d", Long.parseLong(d));
    }

    /**
     * ОКПО: 8 или 10 цифр, с ведущими нулями (например 0192744623).
     */
    public static String formatOkpo(String raw) {
        String d = digitsOnly(raw);
        if (d == null) {
            return null;
        }
        if (d.length() <= 8) {
            return String.format("%08d", Long.parseLong(d));
        }
        if (d.length() <= 10) {
            return String.format("%010d", Long.parseLong(d));
        }
        return d.substring(0, 10);
    }

    /** БИК: 9 цифр. */
    public static String formatBik(String raw) {
        String d = digitsOnly(raw);
        if (d == null) {
            return null;
        }
        if (d.length() > 9) {
            return d.substring(0, 9);
        }
        return d.length() == 9 ? d : String.format("%09d", Long.parseLong(d));
    }

    /** Расчётный или корр. счёт: 20 цифр. */
    public static String formatBankAccount(String raw) {
        String d = digitsOnly(raw);
        if (d == null) {
            return null;
        }
        if (d.length() > 20) {
            return d.substring(0, 20);
        }
        return d.length() == 20 ? d : String.format("%020d", Long.parseLong(d));
    }

    /** Адрес: индекс, регион, район, город — через запятую. */
    public static String formatAddress(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim().replaceAll("\\s+", " ");
        s = s.replaceAll("\\s*,\\s*", ", ");
        return s;
    }

    public static String buildAddressFromDadata(JsonNode addressNode) {
        if (addressNode == null || addressNode.isMissingNode()) {
            return null;
        }
        String unrestricted = textOrNull(addressNode.path("unrestricted_value"));
        if (unrestricted != null) {
            return formatAddress(unrestricted);
        }
        String value = textOrNull(addressNode.path("value"));
        if (value != null) {
            return formatAddress(value);
        }
        JsonNode data = addressNode.path("data");
        if (data.isMissingNode()) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        String postal = textOrNull(data.path("postal_code"));
        if (postal != null) {
            parts.add(postal);
        }
        appendPart(parts, data, "region_with_type");
        appendPart(parts, data, "area_with_type");
        appendPart(parts, data, "city_with_type");
        appendPart(parts, data, "settlement_with_type");
        appendPart(parts, data, "street_with_type");
        String house = textOrNull(data.path("house"));
        if (house != null) {
            String houseType = textOrNull(data.path("house_type"));
            parts.add(houseType != null ? houseType + " " + house : "д " + house);
        }
        String flat = textOrNull(data.path("flat"));
        if (flat != null) {
            String flatType = textOrNull(data.path("flat_type"));
            parts.add(flatType != null ? flatType + " " + flat : "кв " + flat);
        }
        if (parts.isEmpty()) {
            return null;
        }
        return formatAddress(String.join(", ", parts));
    }

    private static void appendPart(List<String> parts, JsonNode data, String field) {
        String v = textOrNull(data.path(field));
        if (v != null && !v.isBlank()) {
            parts.add(v.trim());
        }
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String s = node.asText(null);
        return s != null && !s.isBlank() ? s.trim() : null;
    }
}
