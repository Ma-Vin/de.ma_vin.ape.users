package de.ma_vin.ape.users.controller.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response of token requests
 * See also <a>https://datatracker.ietf.org/doc/html/rfc6749#section-5.1</a>
 */
@Data
public class TokenResponse {
    /**
     * REQUIRED.  The access token issued by the authorization server.
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * REQUIRED.  The type of the token issued as described in Section 7.1.  Value is case insensitive.
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * RECOMMENDED.  The lifetime in seconds of the access token.
     * For example, the value "3600" denotes that the access token will expire in one hour from the time the response was generated.
     * If omitted, the authorization server SHOULD provide the expiration time via other means or document the default value.
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * OPTIONAL.  The refresh token, which can be used to obtain new access tokens using the same authorization grant as described in Section 6..
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * OPTIONAL, if identical to the scope requested by the client;
     * otherwise, REQUIRED.  The scope of the access token as  described by Section 3.3.
     */
    @JsonProperty("scope")
    private String scope;
}
