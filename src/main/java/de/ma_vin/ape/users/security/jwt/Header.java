package de.ma_vin.ape.users.security.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * See also https://datatracker.ietf.org/doc/html/rfc7519#section-5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Header {

    /**
     * (Type) Header Parameter
     * <p>
     * The "typ" (type) Header Parameter defined by [JWS] and [JWE] is used by JWT applications to declare the media type [IANA.MediaTypes] of this complete JWT.
     * This is intended for use by the JWT application when values that are not JWTs could also be present in an application data structure that can contain a JWT object;
     * the application can use this value to disambiguate among the different kinds of objects that might be present.
     * It will typically not be used by applications when it is already known that the object is a JWT.
     * This parameter is ignored by JWT implementations;
     * any processing of this parameter is performed by the JWT application.
     * If present, it is RECOMMENDED that its value be "JWT" to indicate that this object is a JWT.
     * While media type names are not case sensitive, it is RECOMMENDED that "JWT" always be spelled using uppercase characters for compatibility with legacy implementations.
     * Use of this Header Parameter is OPTIONAL.
     */
    private String typ;

    /**
     * (Content Type) Header Parameter
     * <p>
     * The "cty" (content type) Header Parameter defined by [JWS] and [JWE] is used by this specification to convey structural information about the JWT.
     * In the normal case in which nested signing or encryption operations are not employed, the use of this Header Parameter is NOT RECOMMENDED.
     * In the case that nested signing or encryption is employed, this Header Parameter MUST be present;
     * in this case, the value MUST be "JWT", to indicate that a Nested JWT is carried in this JWT.
     * While media type names are not case sensitive, it is RECOMMENDED that "JWT" always be spelled using uppercase characters for compatibility with legacy implementations.
     * See Appendix A.2 for an example of a Nested JWT.
     */
    private String cty;

    /**
     * Message authentication code algorithm.
     * The issuer can freely set an algorithm to verify the signature on the token. However, some supported algorithms are insecure.
     */
    private String alg;

    @JsonIgnore
    private String getJson() throws JwtGeneratingException {
        try {
            return JsonWebToken.getObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new JwtGeneratingException("Could not create Json from: " + toString(), e);
        }
    }

    @JsonIgnore
    public String getJsonBase64UrlEncoded() throws JwtGeneratingException {
        return Base64.getUrlEncoder().encodeToString(getJson().getBytes(StandardCharsets.UTF_8));
    }
}
