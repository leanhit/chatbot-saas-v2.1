package com.chatbot.shared.utils;

import java.util.*;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
    );

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    public static String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength);
    }

    public static String truncateWithEllipsis(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    public static String capitalize(String str) {
        if (isEmpty(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String capitalizeWords(String str) {
        if (isEmpty(str)) return str;
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            result.append(capitalize(words[i]));
        }
        
        return result.toString();
    }

    public static String camelCase(String str) {
        if (isEmpty(str)) return str;
        String[] words = str.toLowerCase().split("[\\s_-]+");
        StringBuilder result = new StringBuilder(words[0]);
        
        for (int i = 1; i < words.length; i++) {
            result.append(capitalize(words[i]));
        }
        
        return result.toString();
    }

    public static String snakeCase(String str) {
        if (isEmpty(str)) return str;
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static String kebabCase(String str) {
        if (isEmpty(str)) return str;
        return str.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }

    public static String reverse(String str) {
        if (str == null) return null;
        return new StringBuilder(str).reverse().toString();
    }

    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) return false;
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }

    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) return false;
        return str.toLowerCase().startsWith(prefix.toLowerCase());
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) return false;
        return str.toLowerCase().endsWith(suffix.toLowerCase());
    }

    public static String removeSpecialCharacters(String str) {
        if (isEmpty(str)) return str;
        return str.replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    public static String removeNumbers(String str) {
        if (isEmpty(str)) return str;
        return str.replaceAll("\\d", "");
    }

    public static String removeLetters(String str) {
        if (isEmpty(str)) return str;
        return str.replaceAll("[a-zA-Z]", "");
    }

    public static String extractNumbers(String str) {
        if (isEmpty(str)) return str;
        return str.replaceAll("[^0-9]", "");
    }

    public static String extractLetters(String str) {
        if (isEmpty(str)) return str;
        return str.replaceAll("[^a-zA-Z]", "");
    }

    public static boolean isNumeric(String str) {
        if (isEmpty(str)) return false;
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static boolean isAlpha(String str) {
        if (isEmpty(str)) return false;
        return str.matches("[a-zA-Z]+");
    }

    public static boolean isAlphaNumeric(String str) {
        if (isEmpty(str)) return false;
        return str.matches("[a-zA-Z0-9]+");
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone.replaceAll("[\\s\\-\\(\\)]", "")).matches();
    }

    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) return email;
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return username + "@" + domain;
        }
        
        String maskedUsername = username.substring(0, 2) + "*".repeat(username.length() - 2);
        return maskedUsername + "@" + domain;
    }

    public static String maskPhone(String phone) {
        if (isEmpty(phone)) return phone;
        
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        if (cleanPhone.length() <= 4) return phone;
        
        return cleanPhone.substring(0, 4) + "*".repeat(cleanPhone.length() - 4);
    }

    public static String padLeft(String str, int size, char padChar) {
        if (str == null) str = "";
        if (str.length() >= size) return str;
        
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size - str.length(); i++) {
            sb.append(padChar);
        }
        sb.append(str);
        
        return sb.toString();
    }

    public static String padRight(String str, int size, char padChar) {
        if (str == null) str = "";
        if (str.length() >= size) return str;
        
        StringBuilder sb = new StringBuilder(size);
        sb.append(str);
        for (int i = 0; i < size - str.length(); i++) {
            sb.append(padChar);
        }
        
        return sb.toString();
    }

    public static String join(String[] array, String delimiter) {
        if (array == null || array.length == 0) return "";
        return String.join(delimiter, array);
    }

    public static String join(Collection<String> collection, String delimiter) {
        if (collection == null || collection.isEmpty()) return "";
        return String.join(delimiter, collection);
    }

    public static List<String> split(String str, String delimiter) {
        if (isEmpty(str)) return new ArrayList<>();
        return Arrays.asList(str.split(Pattern.quote(delimiter)));
    }

    public static String repeat(String str, int count) {
        if (str == null || count <= 0) return "";
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static int countOccurrences(String str, String substring) {
        if (isEmpty(str) || isEmpty(substring)) return 0;
        
        int count = 0;
        int index = 0;
        
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        
        return count;
    }

    public static String replaceAll(String str, String target, String replacement) {
        if (isEmpty(str)) return str;
        return str.replaceAll(Pattern.quote(target), replacement);
    }

    public static String escapeHtml(String str) {
        if (isEmpty(str)) return str;
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    public static String unescapeHtml(String str) {
        if (isEmpty(str)) return str;
        return str.replace("&amp;", "&")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&quot;", "\"")
                  .replace("&#39;", "'");
    }

    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }

    public static String generateRandomNumeric(int length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
}
