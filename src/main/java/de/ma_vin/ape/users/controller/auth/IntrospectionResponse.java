package de.ma_vin.ape.users.controller.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * See https://tools.ietf.org/html/rfc7662
 */
@Data
public class IntrospectionResponse {
    /**
     * REQUIRED.  Boolean indicator of whether or not the presented token
     * is currently active.  The specifics of a token's "active" state
     * will vary depending on the implementation of the authorization
     * server and the information it keeps about its tokens, but a "true"
     * value return for the "active" property will generally indicate
     * that a given token has been issued by this authorization server,
     * has not been revoked by the resource owner, and is within its
     * given time window of validity (e.g., after its issuance time and
     * before its expiration time).  See Section 4 for information on
     * implementation of such checks.
     */
    Boolean active;

    /**
     * OPTIONAL.  A JSON string containing a space-separated list of
     * scopes associated with this token, in the format described in
     * Section 3.3 of OAuth 2.0 [RFC6749].
     */
    String scope;


    /**
     * OPTIONAL.  Client identifier for the OAuth 2.0 client that
     * requested this token.
     */
    @JsonProperty("client_id")
    String clientId;

    /**
     * OPTIONAL.  Human-readable identifier for the resource owner who
     * authorized this token.
     */
    String username;

    /**
     * OPTIONAL.  Type of the token as defined in Section 5.1 of OAuth
     * 2.0 [RFC6749].
     */
    @JsonProperty("token_type")
    String tokenType;

    /**
     * OPTIONAL.  Integer timestamp, measured in the number of seconds
     * since January 1 1970 UTC, indicating when this token will expire,
     * as defined in JWT [RFC7519].
     */
    Long exp;

    /**
     * OPTIONAL.  Integer timestamp, measured in the number of seconds
     * since January 1 1970 UTC, indicating when this token was
     * originally issued, as defined in JWT [RFC7519].
     */
    Long iat;

    /**
     * OPTIONAL.  Integer timestamp, measured in the number of seconds
     * since January 1 1970 UTC, indicating when this token is not to be
     * used before, as defined in JWT [RFC7519].
     */
    Long nbf;


    /**
     * OPTIONAL.  Subject of the token, as defined in JWT [RFC7519].
     * Usually a machine-readable identifier of the resource owner who
     * authorized this token.
     */
    String sub;

    /**
     * OPTIONAL.  Service-specific string identifier or list of string
     * identifiers representing the intended audience for this token, as
     * defined in JWT [RFC7519].
     */
    String aud;

    /**
     * OPTIONAL.  String representing the issuer of this token, as
     * defined in JWT [RFC7519].
     */
    String iss;

    /**
     * OPTIONAL.  String identifier for the token, as defined in JWT
     * [RFC7519].
     */
    String jti;

}
