package de.ma_vin.ape.users.security.jwt;

import de.ma_vin.ape.users.exceptions.CryptException;
import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.users.security.EncoderUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Signature {
    private String secret;

    public String getJsonBase64UrlEncoded(Header header, Payload payload) throws JwtGeneratingException {
        return getJsonBase64UrlEncoded(header.getJsonBase64UrlEncoded(), payload.getJsonBase64UrlEncoded(), secret, header.getAlg());
    }

    public static String getJsonBase64UrlEncoded(String headerBase64UrlEncoded, String payloadBase64UrlEncoded, String secret, String alg)
            throws JwtGeneratingException {
        String message = String.format("%s.%s", headerBase64UrlEncoded, payloadBase64UrlEncoded);
        try {
            return EncoderUtil.encode(message,secret,alg);
        } catch (CryptException e) {
            throw new JwtGeneratingException("Not able to generate Signature", e);
        }
    }
}
