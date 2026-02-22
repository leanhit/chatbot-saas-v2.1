package com.chatbot.shared.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class EncryptionUtils {

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES_ECB_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    private static final int SALT_SIZE = 16;
    private static final int ITERATIONS = 65536;

    public static String generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    public static String encryptAES(String plaintext, String keyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            byte[] encrypted = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    public static String decryptAES(String encryptedBase64, String keyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);
            
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(encrypted, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            byte[] ciphertext = new byte[encrypted.length - IV_SIZE];
            System.arraycopy(encrypted, IV_SIZE, ciphertext, 0, ciphertext.length);
            
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    public static String encryptAESECB(String plaintext, String keyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            Cipher cipher = Cipher.getInstance(AES_ECB_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    public static String decryptAESECB(String encryptedBase64, String keyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            byte[] ciphertext = Base64.getDecoder().decode(encryptedBase64);
            
            Cipher cipher = Cipher.getInstance(AES_ECB_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    public static String encryptPassword(String password, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), ITERATIONS, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt password", e);
        }
    }

    public static boolean verifyPassword(String password, String salt, String encryptedPassword) {
        try {
            String computedHash = encryptPassword(password, salt);
            return computedHash.equals(encryptedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, length);
    }

    public static String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    public static String hashSHA256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash data", e);
        }
    }

    public static String hashSHA512(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash data", e);
        }
    }

    public static String hashMD5(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash data", e);
        }
    }

    public static boolean verifySHA256(String input, String hash) {
        return hashSHA256(input).equals(hash);
    }

    public static boolean verifySHA512(String input, String hash) {
        return hashSHA512(input).equals(hash);
    }

    public static boolean verifyMD5(String input, String hash) {
        return hashMD5(input).equals(hash);
    }

    public static String encodeBase64(String input) {
        if (StringUtils.isEmpty(input)) return null;
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeBase64(String encoded) {
        if (StringUtils.isEmpty(encoded)) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode base64", e);
        }
    }

    public static String encodeBase64Url(String input) {
        if (StringUtils.isEmpty(input)) return null;
        return Base64.getUrlEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeBase64Url(String encoded) {
        if (StringUtils.isEmpty(encoded)) return null;
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encoded);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode base64 URL", e);
        }
    }

    public static String xorEncrypt(String plaintext, String key) {
        if (StringUtils.isEmpty(plaintext) || StringUtils.isEmpty(key)) return plaintext;
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < plaintext.length(); i++) {
            result.append((char) (plaintext.charAt(i) ^ key.charAt(i % key.length())));
        }
        return result.toString();
    }

    public static String xorDecrypt(String encrypted, String key) {
        return xorEncrypt(encrypted, key);
    }

    public static String caesarCipher(String text, int shift) {
        if (StringUtils.isEmpty(text)) return text;
        
        StringBuilder result = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                result.append((char) ((ch - 'A' + shift + 26) % 26 + 'A'));
            } else if (Character.isLowerCase(ch)) {
                result.append((char) ((ch - 'a' + shift + 26) % 26 + 'a'));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public static String caesarDecrypt(String encrypted, int shift) {
        return caesarCipher(encrypted, -shift);
    }

    public static String reverseString(String input) {
        if (StringUtils.isEmpty(input)) return input;
        return new StringBuilder(input).reverse().toString();
    }

    public static String maskString(String input, int visibleChars) {
        if (StringUtils.isEmpty(input) || input.length() <= visibleChars) {
            return input;
        }
        
        String visible = input.substring(0, visibleChars);
        String masked = "*".repeat(input.length() - visibleChars);
        return visible + masked;
    }

    public static String maskEmail(String email) {
        if (StringUtils.isEmpty(email) || !email.contains("@")) return email;
        
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
        if (StringUtils.isEmpty(phone)) return phone;
        
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        if (cleanPhone.length() <= 4) return phone;
        
        return cleanPhone.substring(0, 4) + "*".repeat(cleanPhone.length() - 4);
    }

    public static String maskCreditCard(String cardNumber) {
        if (StringUtils.isEmpty(cardNumber)) return cardNumber;
        
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        if (cleanNumber.length() <= 4) return cardNumber;
        
        return cleanNumber.substring(0, 4) + "*".repeat(cleanNumber.length() - 8) + 
               cleanNumber.substring(cleanNumber.length() - 4);
    }

    public static boolean isBase64(String input) {
        if (StringUtils.isEmpty(input)) return false;
        try {
            Base64.getDecoder().decode(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isHex(String input) {
        if (StringUtils.isEmpty(input)) return false;
        return input.matches("^[0-9a-fA-F]+$");
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateApiKey() {
        return "sk-" + generateSecureToken().substring(0, 32);
    }

    public static boolean isValidApiKey(String apiKey) {
        return StringUtils.isNotEmpty(apiKey) && 
               apiKey.startsWith("sk-") && 
               apiKey.length() == 35;
    }
}
