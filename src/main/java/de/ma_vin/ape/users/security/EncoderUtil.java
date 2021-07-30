package de.ma_vin.ape.users.security;

import de.ma_vin.ape.users.exceptions.CryptException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncoderUtil {

    public static String encode(String message, String secret, String alg) throws CryptException {
        try {
            byte[] encrypted = switch (alg) {
                case "MD5" -> getHmac(message, "HmacMD5", secret);
                case "HS1" -> getHmac(message, "HmacSHA1", secret);
                case "HS224" -> getHmac(message, "HmacSHA224", secret);
                case "HS256" -> getHmac(message, "HmacSHA256", secret);
                case "HS384" -> getHmac(message, "HmacSHA384", secret);
                case "HS512" -> getHmac(message, "HmacSHA512", secret);
                case "HS512/224" -> getHmac(message, "HmacSHA512/224", secret);
                case "HS512/256" -> getHmac(message, "HmacSHA512/256", secret);
                default -> throw new CryptException("Unexpected code algorithm: " + alg);
            };
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new CryptException("Not able to generate Signature", e);
        }
    }

    private static byte[] getHmac(String message, String algorithmName, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithmName);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithmName);
        mac.init(secretKeySpec);
        return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }
}
