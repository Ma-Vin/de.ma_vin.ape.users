package de.ma_vin.ape.users.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
@AllArgsConstructor
@Log4j2
public class JsonWebToken {
    private static ObjectMapper mapper = getObjectMapper();

    private Header header;
    private Payload payload;
    private Signature signature;

    /**
     * Determines the base64 url encoded token for the given header, body and secret (at signature)
     *
     * @return the token
     * @throws JwtGeneratingException
     */
    public String getEncodedToken() throws JwtGeneratingException {
        return String.format("%s.%s.%s"
                , header.getJsonBase64UrlEncoded()
                , payload.getJsonBase64UrlEncoded()
                , signature.getJsonBase64UrlEncoded(header, payload));
    }

    /**
     * Checks whether a given base64 url encoded token is valid against its signature and given secret
     *
     * @param encodedToken the token to check
     * @param secret       Secret to use for encoding
     * @return {@code true} if the token is valid with respect to the given secret. Otherwise {@code false}
     */
    public static boolean verify(String encodedToken, String secret) {
        if (encodedToken.trim().isEmpty()) {
            return false;
        }
        String[] split = encodedToken.split("\\.");
        if (split.length != 3) {
            return false;
        }

        try {
            Header header = getObjectMapper().readValue(Base64.getDecoder().decode(split[0].getBytes(StandardCharsets.UTF_8)), Header.class);
            return split[2].equals(Signature.getJsonBase64UrlEncoded(split[0], split[1], secret, header.getAlg()));
        } catch (JwtGeneratingException | IOException e) {
            log.error("Could not verify token {}: {}", encodedToken, e.getMessage());
            return false;
        }
    }

    /**
     * @return Mapper to transform objects to their json representation and other way round
     */
    public static ObjectMapper getObjectMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
        }

        return mapper;
    }
}
