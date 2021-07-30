package de.ma_vin.ape.users.security.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * See also https://datatracker.ietf.org/doc/html/rfc7519#section-4.1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payload {

    /**
     * (Issuer) Claim
     * <p>
     * The "iss" (issuer) claim identifies the principal that issued the JWT.
     * The processing of this claim is generally application specific.
     * The "iss" value is a case-sensitive string containing a StringOrURI value.
     * Use of this claim is OPTIONAL.
     */
    private String iss;

    /**
     * (Subject) Claim
     * <p>
     * The "sub" (subject) claim identifies the principal that is the subject of the JWT.
     * The claims in a JWT are normally statements about the subject.
     * The subject value MUST either be scoped to be  locally unique in the context of the issuer or be globally unique.
     * The processing of this claim is generally application specific.
     * The "sub" value is a case-sensitive string containing a StringOrURI value.
     * Use of this claim is OPTIONAL.
     */
    private String sub;

    /**
     * (Audience) Claim
     * <p>
     * The "aud" (audience) claim identifies the recipients that the JWT is intended for.
     * Each principal intended to process the JWT MUST identify itself with a value in the audience claim.
     * If the principal processing the claim does not identify itself with a value in the "aud" claim when this claim is present, then the JWT MUST be rejected.  In the general case, the "aud" value is an array of case-sensitive strings, each containing a StringOrURI value.  In the special case when the JWT has one audience, the "aud" value MAY be a
     * single case-sensitive string containing a StringOrURI value.
     * The interpretation of audience values is generally application specific.
     * Use of this claim is OPTIONAL.
     */
    private String aud;

    /**
     * (Expiration Time) Claim
     * <p>
     * The "exp" (expiration time) claim identifies the expiration time on or after which the JWT MUST NOT be accepted for processing.
     * The processing of the "exp" claim requires that the current date/time MUST be before the expiration date/time listed in the "exp" claim.
     * Implementers MAY provide for some small leeway, usually no more than  a few minutes, to account for clock skew.
     * Its value MUST be a number containing a NumericDate value.
     * Use of this claim is OPTIONAL.
     */
    private LocalDateTime exp;

    /**
     * (Not Before) Claim
     * <p>
     * The "nbf" (not before) claim identifies the time before which the JWT MUST NOT be accepted for processing.
     * The processing of the "nbf" claim requires that the current date/time MUST be after or equal to the not-before date/time listed in the "nbf" claim.
     * Implementers MAY provide for some small leeway, usually no more than a few minutes, to account for clock skew.
     * Its value MUST be a number containing a NumericDate value.
     * Use of this claim is OPTIONAL.
     */
    private LocalDateTime nbf;

    /**
     * (Issued At) Claim
     * <p>
     * The "iat" (issued at) claim identifies the time at which the JWT was issued.
     * This claim can be used to determine the age of the JWT.
     * Its value MUST be a number containing a NumericDate value.
     * Use of this claim is OPTIONAL.
     */
    private LocalDateTime iat;

    /**
     * (JWT ID) Claim
     * <p>
     * The "jti" (JWT ID) claim provides a unique identifier for the JWT.
     * The identifier value MUST be assigned in a manner that ensures that there is a negligible probability that the same value will be accidentally assigned to a different data object;
     * if the application uses multiple issuers, collisions MUST be prevented among values produced by different issuers as well.
     * The "jti" claim can be used to prevent the JWT from being replayed.
     * The "jti" value is a case-sensitive string.
     * Use of this claim is OPTIONAL.
     */
    private String jti;

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
        return Base64.getUrlEncoder().withoutPadding().encodeToString(getJson().getBytes(StandardCharsets.UTF_8));
    }
}
