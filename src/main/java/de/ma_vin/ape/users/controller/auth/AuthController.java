package de.ma_vin.ape.users.controller.auth;

import de.ma_vin.ape.users.exceptions.AuthTokenException;
import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.users.properties.AuthClients;
import de.ma_vin.ape.users.security.jwt.JsonWebToken;
import de.ma_vin.ape.users.security.jwt.Payload;
import de.ma_vin.ape.users.security.service.AuthorizeCodeService;
import de.ma_vin.ape.users.security.service.TokenIssuerService;
import de.ma_vin.ape.utils.properties.SystemProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


/**
 * See also <a>https://datatracker.ietf.org/doc/html/rfc6749</a>
 */
@RestController
@RequestMapping(path = "oauth")
@Data
public class AuthController {

    public static final String TOKEN_TYPE = "bearer";

    @Autowired
    AuthClients authClients;

    @Autowired
    private TokenIssuerService tokenIssuerService;
    @Autowired
    private AuthorizeCodeService authorizeCodeService;

    @GetMapping("/authorize")
    public @ResponseBody
    Object authorize(Principal principal, HttpServletRequest request, HttpServletResponse response
            , @RequestParam("response_type") String responseType
            , @RequestParam(name = "client_id") String clientId
            , @RequestParam(name = "redirect_uri", required = false) String redirectUri
            , @RequestParam(required = false) String scope
            , @RequestParam(required = false) String state) {

        return switch (ResponseType.getByTypeName(responseType)) {
            case CODE -> handleAuthorizeCode(response, principal.getName(), clientId, redirectUri, scope, state);
            case TOKEN -> handleAuthorizeToken(request, response, principal.getName(), clientId, redirectUri, scope);
            default -> throw new AuthTokenException("Not supported response type:" + responseType, HttpServletResponse.SC_NOT_IMPLEMENTED);
        };
    }

    @SuppressWarnings("java:S107")
    @PostMapping("/token")
    public @ResponseBody
    TokenResponse token(HttpServletRequest request, HttpServletResponse response
            , @RequestParam(name = "grant_type") String grantType
            , @RequestParam(required = false) String code
            , @RequestParam(name = "refresh_token", required = false) String refreshToken
            , @RequestParam(name = "redirect_uri", required = false) String redirectUri
            , @RequestParam(name = "client_id") String clientId
            , @RequestParam(required = false) String username
            , @RequestParam(required = false) String password
            , @RequestParam(required = false) String scope) {

        return switch (GrantType.getByTypeName(grantType)) {
            case AUTHORIZATION_CODE -> handleTokenAuthorizationCode(request, response, code, redirectUri, clientId);
            case PASSWORD -> handleTokenPassword(request, username, password, clientId, scope);
            case CLIENT_CREDENTIALS -> handleTokenClient(request, clientId, scope);
            case REFRESH_TOKEN -> handleTokenRefreshToken(refreshToken, clientId);
            default -> throw new AuthTokenException("Not supported grant type:" + grantType, HttpServletResponse.SC_NOT_IMPLEMENTED);
        };
    }

    @PostMapping("/introspection")
    public @ResponseBody
    IntrospectionResponse introspection(@RequestParam String token, @RequestParam(required = false, name = "token_type_hint") String tokenTypeHint) {
        Optional<JsonWebToken> decodedToken = tokenIssuerService.getToken(token);
        if (decodedToken.isEmpty()) {
            throw new AuthTokenException("The token is not known or not valid anymore", HttpServletResponse.SC_UNAUTHORIZED);

        }
        Payload payload = decodedToken.get().getPayload();

        IntrospectionResponse response = new IntrospectionResponse();
        response.setActive(Boolean.TRUE);
        response.setTokenType(decodedToken.get().getHeader().getTyp());
        response.setUsername(payload.getSub());
        response.setSub(payload.getSub());
        response.setAud(payload.getAud());
        response.setExp(getLocalDateTimeToLong(payload.getExp()));
        response.setIat(getLocalDateTimeToLong(payload.getIat()));
        response.setNbf(getLocalDateTimeToLong(payload.getNbf()));
        response.setIss(payload.getIss());
        response.setJti(payload.getJti());

        return response;
    }

    private Long getLocalDateTimeToLong(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toEpochSecond() : null;
    }

    /**
     * Authorization Code Grant
     *
     * @param response    http response where to set a status if an error occurs or a redirection
     * @param userId      id of the user for whom a code is generated (not part of common specification)
     * @param clientId    REQUIRED. The client identifier as described in Section 2.2.
     * @param redirectUri OPTIONAL. As described in Section 3.1.2.
     * @param scope       OPTIONAL. The scope of the access request as described by Section 3.3.
     * @param state       RECOMMENDED. An opaque value used by the client to maintain state between the request and callback.
     *                    The authorization server includes this value when redirecting the user-agent back to the client.
     *                    The parameter SHOULD be used for preventing cross-site request forgery as described in Section 10.12.
     * @return Response with a new code and the give state
     */
    private AuthorizationResponse handleAuthorizeCode(HttpServletResponse response, String userId, String clientId, String redirectUri, String scope, String state) {
        AuthorizationResponse authorizationResponse = new AuthorizationResponse();
        authorizationResponse.setState(state);
        Optional<String> code = authorizeCodeService.issue(userId, clientId, scope);
        if (code.isPresent()) {
            authorizationResponse.setCode(code.get());
            redirect(response, redirectUri, clientId);
            return authorizationResponse;
        }
        throw new AuthTokenException(String.format("Could not provide authenticate code for user %s for client %s", userId, clientId), HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Adds the redirection uri to the http response
     *
     * @param response    The http response
     * @param redirectUri Redirection uri which should be add to the response
     * @param clientId    The client identifier
     * @throws AuthTokenException if not able to send redirect
     */
    private void redirect(HttpServletResponse response, String redirectUri, String clientId) {
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            return;
        }
        if (authClients.getClients().stream()
                .filter(c -> c.getClientId().equals(clientId))
                .flatMap(c -> c.getRedirects().stream())
                .noneMatch(r -> redirectUri.equalsIgnoreCase(r.getRedirectStart()))) {
            throw new AuthTokenException(String.format("The redirection uri %s is not allowed for client %s", redirectUri, clientId)
                    , HttpServletResponse.SC_FORBIDDEN);
        }
        try {
            response.sendRedirect(redirectUri);
        } catch (IOException e) {
            throw new AuthTokenException(String.format("Could not redirect authenticate code to %s for client %s", redirectUri, clientId)
                    , HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Implicit Grant
     *
     * @param request     http request
     * @param response    http response where to set a status if an error occurs or a redirection
     * @param username    id of the user for whom a code is generated (not part of common specification)
     * @param clientId    REQUIRED. The client identifier as described in Section 2.2.
     * @param redirectUri OPTIONAL.  As described in Section 3.1.2.
     * @param scope       OPTIONAL. The scope of the access request as described by Section 3.3.
     * @return a token and refresh token
     */
    private TokenResponse handleAuthorizeToken(HttpServletRequest request, HttpServletResponse response, String username, String clientId, String redirectUri, String scope) {
        Optional<TokenIssuerService.TokenInfo> tokenPair = tokenIssuerService.issueImplicit(request.getRequestURL().toString(), username, scope);
        TokenResponse result = createTokenResponse(tokenPair, username, clientId);
        redirect(response, redirectUri, clientId);
        return result;
    }

    /**
     * Authorization Code Grant
     *
     * @param response    http response where to set a status if an error occurs or a redirection
     * @param code        REQUIRED. The authorization code received from the authorization server.
     * @param redirectUri REQUIRED, if the "redirect_uri" parameter was included in the authorization request as described in Section 4.1.1,
     *                    and their values MUST be identical.
     * @param clientId    REQUIRED, if the client is not authenticating with the authorization server as described in Section 3.2.1.
     * @return a token and refresh token
     */
    private TokenResponse handleTokenAuthorizationCode(HttpServletRequest request, HttpServletResponse response, String code, String redirectUri, String clientId) {
        if (!authorizeCodeService.isValid(code)) {
            throw new AuthTokenException(String.format("The authenticate code %s is not valid for client %s", code, clientId)
                    , HttpServletResponse.SC_UNAUTHORIZED);
        }
        AuthorizeCodeService.CodeInfo codeInfo = authorizeCodeService.getCodeInfo(code)
                .orElseThrow(() -> new AuthTokenException(String.format("The authenticate code %s is not valid for client %s", code, clientId)
                        , HttpServletResponse.SC_UNAUTHORIZED));

        Optional<TokenIssuerService.TokenInfo> tokenPair = tokenIssuerService.issueImplicit(request.getRequestURL().toString(), codeInfo.getUserId(), codeInfo.getScope());

        TokenResponse tokenResponse = createTokenResponse(tokenPair, codeInfo.getUserId(), clientId);
        redirect(response, redirectUri, clientId);
        return tokenResponse;
    }

    /**
     * Resource Owner Password Credentials Grant
     *
     * @param username REQUIRED. The resource owner username.
     * @param password REQUIRED. The resource owner password.
     * @param clientId Id of the client which request the token
     * @param scope    OPTIONAL. The scope of the access request as described by Section 3.3.
     * @return a token and refresh token
     */
    private TokenResponse handleTokenPassword(HttpServletRequest request, String username, String password, String clientId, String scope) {
        Optional<TokenIssuerService.TokenInfo> tokenPair = tokenIssuerService.issue(request.getRequestURL().toString(), username, password, scope);
        return createTokenResponse(tokenPair, username, clientId);
    }

    /**
     * Client Credentials Grant
     *
     * @param clientId Id of the client which request the token
     * @param scope    OPTIONAL. The scope of the access request as described by Section 3.3.
     * @return A new pair of tokens
     */
    private TokenResponse handleTokenClient(HttpServletRequest request, String clientId, String scope) {
        Optional<TokenIssuerService.TokenInfo> tokenPair = tokenIssuerService.issueClient(request.getRequestURL().toString(), clientId, scope);
        return createTokenResponse(tokenPair, clientId, clientId);
    }

    /**
     * Refreshing an Access Token
     *
     * @param refreshToken REQUIRED.  The refresh token issued to the client.
     * @param clientId     Id of the client which request the refresh token
     * @return A new pair of tokens
     */
    private TokenResponse handleTokenRefreshToken(String refreshToken, String clientId) {
        Optional<TokenIssuerService.TokenInfo> tokenPair = tokenIssuerService.refresh(refreshToken);
        return createTokenResponse(tokenPair, tokenPair.map(tp -> tp.getToken().getPayload().getSub()).orElse("Unknown"), clientId);
    }

    /**
     * Creates a token response
     *
     * @param tokenPair Token info containing a pair of token and refresh token
     * @param username  name of the user who is subject of the token. Might be a client
     * @param clientId  the client who is requesting
     * @return a token response
     */
    private TokenResponse createTokenResponse(Optional<TokenIssuerService.TokenInfo> tokenPair, String username, String clientId) {
        if (tokenPair.isEmpty()) {
            throw new AuthTokenException(String.format("Could not authenticate user %s for client %s", username, clientId), HttpServletResponse.SC_UNAUTHORIZED);
        }
        TokenResponse response = new TokenResponse();
        try {
            response.setAccessToken(tokenPair.get().getToken().getEncodedToken());
            response.setRefreshToken(tokenPair.get().getRefreshToken().getEncodedToken());
            response.setTokenType(TOKEN_TYPE);
            response.setScope(tokenPair.get().getScope());
            response.setExpiresIn(Long.valueOf(ChronoUnit.SECONDS.between(SystemProperties.getSystemDateTime(), tokenPair.get().getToken().getPayload().getExp())));
        } catch (JwtGeneratingException e) {
            throw new AuthTokenException(String.format("Could not authenticate user %s for client %s, because of jwt generating failures", username, clientId), e
                    , HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
