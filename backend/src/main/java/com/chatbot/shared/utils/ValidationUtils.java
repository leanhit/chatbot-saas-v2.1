package com.chatbot.shared.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"
    );

    private static final Set<String> ALLOWED_COUNTRIES = new HashSet<>(Arrays.asList(
            "US", "CA", "GB", "AU", "DE", "FR", "IT", "ES", "NL", "BE", "CH", "AT", "SE", "NO", "DK", "FI"
    ));

    private static final Set<String> ALLOWED_CURRENCIES = new HashSet<>(Arrays.asList(
            "USD", "EUR", "GBP", "CAD", "AUD", "CHF", "JPY", "CNY", "INR", "SGD", "HKD", "NZD"
    ));

    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }

        public void addErrors(List<String> errors) {
            this.valid = false;
            this.errors.addAll(errors);
        }

        public String getErrorsAsString() {
            return String.join(", ", errors);
        }
    }

    public static boolean isValidEmail(String email) {
        return StringUtils.isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (StringUtils.isEmpty(phone)) return false;
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    public static boolean isValidUsername(String username) {
        return StringUtils.isNotEmpty(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        return StringUtils.isNotEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidUUID(String uuid) {
        return StringUtils.isNotEmpty(uuid) && UUID_PATTERN.matcher(uuid).matches();
    }

    public static boolean isValidUrl(String url) {
        if (StringUtils.isEmpty(url)) return false;
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidCountryCode(String countryCode) {
        return StringUtils.isNotEmpty(countryCode) && 
               ALLOWED_COUNTRIES.contains(countryCode.toUpperCase());
    }

    public static boolean isValidCurrencyCode(String currencyCode) {
        return StringUtils.isNotEmpty(currencyCode) && 
               ALLOWED_CURRENCIES.contains(currencyCode.toUpperCase());
    }

    public static boolean isValidPostalCode(String postalCode, String countryCode) {
        if (StringUtils.isEmpty(postalCode) || StringUtils.isEmpty(countryCode)) {
            return false;
        }

        switch (countryCode.toUpperCase()) {
            case "US":
                return postalCode.matches("^\\d{5}(-\\d{4})?$");
            case "CA":
                return postalCode.matches("^[A-Za-z]\\d[A-Za-z] \\d[A-Za-z]\\d$");
            case "GB":
                return postalCode.matches("^[A-Za-z]{1,2}\\d[A-Za-z\\d]? \\d[A-Za-z]{2}$");
            case "DE":
                return postalCode.matches("^\\d{5}$");
            case "FR":
                return postalCode.matches("^\\d{5}$");
            case "IT":
                return postalCode.matches("^\\d{5}$");
            case "ES":
                return postalCode.matches("^\\d{5}$");
            case "NL":
                return postalCode.matches("^\\d{4}\\s?[A-Za-z]{2}$");
            case "BE":
                return postalCode.matches("^\\d{4}$");
            case "CH":
                return postalCode.matches("^\\d{4}$");
            case "AT":
                return postalCode.matches("^\\d{4}$");
            case "SE":
                return postalCode.matches("^\\d{3}\\s?\\d{2}$");
            case "NO":
                return postalCode.matches("^\\d{4}$");
            case "DK":
                return postalCode.matches("^\\d{4}$");
            case "FI":
                return postalCode.matches("^\\d{5}$");
            case "AU":
                return postalCode.matches("^\\d{4}$");
            case "NZ":
                return postalCode.matches("^\\d{4}$");
            default:
                return postalCode.length() >= 3 && postalCode.length() <= 10;
        }
    }

    public static boolean isAdult(LocalDate birthDate) {
        if (birthDate == null) return false;
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }

    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null || min == null || max == null) return false;
        return value.doubleValue() >= min.doubleValue() && value.doubleValue() <= max.doubleValue();
    }

    public static boolean isLengthInRange(String str, int minLength, int maxLength) {
        if (str == null) return false;
        return str.length() >= minLength && str.length() <= maxLength;
    }

    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    public static boolean isNonNegative(Number number) {
        return number != null && number.doubleValue() >= 0;
    }

    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public static boolean isTodayOrFuture(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    public static boolean isTodayOrPast(LocalDate date) {
        return date != null && !date.isAfter(LocalDate.now());
    }

    public static boolean isValidCreditCard(String cardNumber) {
        if (StringUtils.isEmpty(cardNumber)) return false;
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        
        if (!cleanNumber.matches("^\\d{13,19}$")) return false;
        
        return luhnCheck(cleanNumber);
    }

    private static boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }

    public static boolean isValidIBAN(String iban) {
        if (StringUtils.isEmpty(iban)) return false;
        
        String normalizedIban = iban.replaceAll("[\\s]", "").toUpperCase();
        
        if (!normalizedIban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$")) {
            return false;
        }
        
        String rearranged = normalizedIban.substring(4) + normalizedIban.substring(0, 4);
        String numeric = "";
        
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric += String.valueOf(c - 'A' + 10);
            } else {
                numeric += c;
            }
        }
        
        try {
            java.math.BigInteger bigInt = new java.math.BigInteger(numeric);
            return bigInt.mod(java.math.BigInteger.valueOf(97)).intValue() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public static ValidationResult validateUserRegistration(String username, String email, String password, String firstName, String lastName) {
        ValidationResult result = new ValidationResult();
        
        if (!isValidUsername(username)) {
            result.addError("Username must be 3-20 characters long and contain only letters, numbers, and underscores");
        }
        
        if (!isValidEmail(email)) {
            result.addError("Invalid email format");
        }
        
        if (!isValidPassword(password)) {
            result.addError("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
        
        if (StringUtils.isEmpty(firstName) || firstName.length() < 2) {
            result.addError("First name must be at least 2 characters long");
        }
        
        if (StringUtils.isEmpty(lastName) || lastName.length() < 2) {
            result.addError("Last name must be at least 2 characters long");
        }
        
        return result;
    }

    public static ValidationResult validateTenantCreation(String name, String domain, String country) {
        ValidationResult result = new ValidationResult();
        
        if (StringUtils.isEmpty(name) || name.length() < 2) {
            result.addError("Tenant name must be at least 2 characters long");
        }
        
        if (StringUtils.isNotEmpty(domain) && !isValidUrl("https://" + domain)) {
            result.addError("Invalid domain format");
        }
        
        if (StringUtils.isEmpty(country) || !isValidCountryCode(country)) {
            result.addError("Invalid country code");
        }
        
        return result;
    }

    public static ValidationResult validatePaymentAmount(BigDecimal amount, String currency) {
        ValidationResult result = new ValidationResult();
        
        if (amount == null || !isPositive(amount)) {
            result.addError("Amount must be a positive number");
        }
        
        if (amount.scale() > 2) {
            result.addError("Amount cannot have more than 2 decimal places");
        }
        
        if (StringUtils.isNotEmpty(currency) && !isValidCurrencyCode(currency)) {
            result.addError("Invalid currency code");
        }
        
        return result;
    }

    public static boolean isValidJson(String json) {
        if (StringUtils.isEmpty(json)) return false;
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidXml(String xml) {
        if (StringUtils.isEmpty(xml)) return false;
        try {
            javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(xml.getBytes()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidBase64(String base64) {
        if (StringUtils.isEmpty(base64)) return false;
        try {
            java.util.Base64.getDecoder().decode(base64);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidHexString(String hex) {
        return StringUtils.isNotEmpty(hex) && hex.matches("^[0-9a-fA-F]+$");
    }

    public static boolean isValidBinaryString(String binary) {
        return StringUtils.isNotEmpty(binary) && binary.matches("^[01]+$");
    }

    public static boolean isValidIpAddress(String ip) {
        if (StringUtils.isEmpty(ip)) return false;
        
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
        
        return ip.matches(ipv4Pattern) || ip.matches(ipv6Pattern);
    }

    public static boolean isValidMacAddress(String mac) {
        if (StringUtils.isEmpty(mac)) return false;
        return mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

    public static boolean isValidColorHex(String color) {
        if (StringUtils.isEmpty(color)) return false;
        return color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }

    public static boolean isValidSlug(String slug) {
        if (StringUtils.isEmpty(slug)) return false;
        return slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }

    public static boolean isValidVersion(String version) {
        if (StringUtils.isEmpty(version)) return false;
        return version.matches("^\\d+\\.\\d+(\\.\\d+)?(-[a-zA-Z0-9]+)?$");
    }

    public static boolean isValidSemanticVersion(String version) {
        if (StringUtils.isEmpty(version)) return false;
        return version.matches("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    }
}
