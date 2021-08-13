package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.users.security.jwt.JsonWebToken;
import de.ma_vin.ape.users.security.jwt.Payload;
import de.ma_vin.ape.utils.generators.IdGenerator;
import de.ma_vin.ape.utils.properties.SystemProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to provide operations on tokens
 */
@Service
@Data
@Log4j2
public class TokenIssuerService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder encoder;

    @Value("${tokenSecret}")
    private String secret;
    @Value("${tokenExpiresInSeconds}")
    private Long tokenExpiresInSeconds;
    @Value("${refreshTokenExpiresInSeconds}")
    private Long refreshTokenExpiresInSeconds;
    @Value("${encodingAlgorithm}")
    private String encodingAlgorithm;

    @Setter(AccessLevel.PRIVATE)
    private Map<String, TokenInfo> inMemoryTokens = new HashMap<>();

    /**
     * Clears all existing tokens
     */
    public void clearAllTokens() {
        inMemoryTokens.clear();
    }

    /**
     * Clears all expired tokens
     */
    public void clearExpiredTokens() {
        LocalDateTime now = SystemProperties.getSystemDateTime();
        Set<String> keysToRemove = inMemoryTokens.entrySet().stream()
                .filter(e -> e.getValue().getExpiresAtLeast() == null || e.getValue().getExpiresAtLeast().isBefore(now))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        log.debug("{} entries are expired", keysToRemove.size());
        keysToRemove.forEach(inMemoryTokens::remove);
    }

    /**
     * Refreshes a token
     *
     * @param encodedRefreshToken refresh token
     * @return new {@link Optional} of a pair token and refresh token
     */
    public Optional<TokenInfo> refresh(String encodedRefreshToken) {
        if (!isValidInternal(encodedRefreshToken, false, null)) {
            return Optional.empty();
        }

        Optional<JsonWebToken> refreshToken = JsonWebToken.decodeToken(encodedRefreshToken, secret);
        if (refreshToken.isEmpty()) {
            return Optional.empty();
        }
        TokenInfo tokenInfo = inMemoryTokens.get(refreshToken.get().getPayload().getJti());

        LocalDateTime now = SystemProperties.getSystemDateTime();
        if (refreshToken.get().getPayload().getExp().isBefore(now)) {
            log.warn("The token {} is expired already", encodedRefreshToken);
            return Optional.empty();
        }

        tokenInfo.getToken().getPayload().setIat(now);
        tokenInfo.getToken().getPayload().setNbf(now);
        tokenInfo.getToken().getPayload().setExp(now.plus(tokenExpiresInSeconds, ChronoUnit.SECONDS));

        tokenInfo.getRefreshToken().getPayload().setIat(now);
        tokenInfo.getRefreshToken().getPayload().setNbf(now);
        tokenInfo.getRefreshToken().getPayload().setExp(now.plus(refreshTokenExpiresInSeconds, ChronoUnit.SECONDS));

        tokenInfo.setExpiresAtLeast(tokenInfo.getRefreshToken().getPayload().getExp());

        return Optional.of(tokenInfo);
    }

    /**
     * Issues a new Token and corresponding refresh token
     *
     * @param issuerUrl url of the principal who is issuing
     * @param username  name of user whose is issued for
     * @param password  the password of the user
     * @return new {@link Optional} of a pair token and refresh token
     */
    public Optional<TokenInfo> issue(String issuerUrl, String username, String password) {
        return issue(issuerUrl, username, password, null);
    }

    /**
     * Issues a new Token and corresponding refresh token
     *
     * @param issuerUrl url of the principal who is issuing
     * @param username  name of user whose is issued for
     * @param password  the password of the user
     * @param scopes    the scope of the issued token
     * @return new {@link Optional} of a pair token and refresh token
     */
    public Optional<TokenInfo> issue(String issuerUrl, String username, String password, String scopes) {
        Optional<UserDao> user = userRepository.findById(IdGenerator.generateId(username, User.ID_PREFIX));
        if (user.isEmpty()) {
            log.warn("The user {} is unknown while issuing token", username);
            return Optional.empty();
        }
        if (!encoder.matches(new String(Base64.getUrlDecoder().decode(password)), user.get().getPassword())) {
            log.warn("Wrong password for user {} while issuing token", username);
            return Optional.empty();
        }
        return issueInternal(issuerUrl, username, scopes);
    }

    /**
     * Issues a new implicit Token and corresponding refresh token
     *
     * @param issuerUrl url of the principal who is issuing
     * @param username  name of user whose is issued for
     * @return new {@link Optional} of a pair token and refresh token
     */
    public Optional<TokenInfo> issueImplicit(String issuerUrl, String username) {
        return issueImplicit(issuerUrl, username, null);
    }

    /**
     * Issues a new implicit Token and corresponding refresh token
     *
     * @param issuerUrl url of the principal who is issuing
     * @param username  name of user whose is issued for
     * @param scopes    the scope of the issued token
     * @return new {@link Optional} of a pair token and refresh token
     */
    public Optional<TokenInfo> issueImplicit(String issuerUrl, String username, String scopes) {
        Optional<UserDao> user = userRepository.findById(IdGenerator.generateId(username, User.ID_PREFIX));
        if (user.isEmpty()) {
            log.warn("The user {} is unknown while issuing token", username);
            return Optional.empty();
        }
        return issueInternal(issuerUrl, username, scopes);
    }

    /**
     * Issues a new client Token and corresponding refresh token
     *
     * @param issuerUrl url of the principal who is issuing
     * @param clientId  id of the client
     * @return new {@link Optional} of a pair token and refresh token
     */
    public Optional<TokenInfo> issueClient(String issuerUrl, String clientId) {
        return issueClient(issuerUrl, clientId, null);
    }

    /**
     * Issues a new client Token and corresponding refresh token
     *
     * @param issuerUrl url of the principal who is issuing
     * @param clientId  id of the client
     * @param scopes    the scope of the issued token
     * @return new {@link Optional} of a pair token and refresh token
     */
    public Optional<TokenInfo> issueClient(String issuerUrl, String clientId, String scopes) {
        return issueInternal(issuerUrl, clientId, scopes);
    }

    /**
     * Issues a new Token and corresponding refresh token
     *
     * @param issuerUrl url of the principal who is issuing
     * @param username  name of user whose is issued for
     * @param scopes    the scopes of the issued token
     * @return new {@link Optional} of a pair token and refresh token
     */
    private Optional<TokenInfo> issueInternal(String issuerUrl, String username, String scopes) {
        LocalDateTime now = SystemProperties.getSystemDateTime();

        String uuid = UUID.randomUUID().toString();
        while (inMemoryTokens.containsKey(uuid)) {
            uuid = UUID.randomUUID().toString();
        }

        Payload payload = new Payload(issuerUrl, username, null, now.plus(tokenExpiresInSeconds, ChronoUnit.SECONDS), now, now, uuid);
        Payload refreshPayload = new Payload(issuerUrl, username, null, now.plus(refreshTokenExpiresInSeconds, ChronoUnit.SECONDS), now, now, uuid);

        TokenInfo tokenInfo = new TokenInfo(uuid, refreshPayload.getExp()
                , new JsonWebToken(encodingAlgorithm, secret, payload)
                , new JsonWebToken(encodingAlgorithm, secret, refreshPayload)
                , scopes);

        inMemoryTokens.put(uuid, tokenInfo);

        return Optional.of(tokenInfo);
    }

    /**
     * Checks whether is a given token is known and valid
     *
     * @param encodedToken token to check
     * @return {@code true} if the token is valid. Otherwise {@code false}
     */
    public boolean isValid(String encodedToken) {
        return isValid(encodedToken, null);
    }

    /**
     * Checks whether is a given token is known and valid
     *
     * @param encodedToken token to check
     * @param scope        the scope for which is validated
     * @return {@code true} if the token is valid. Otherwise {@code false}
     */
    public boolean isValid(String encodedToken, String scope) {
        return isValidInternal(encodedToken, true, scope);
    }

    /**
     * Checks whether is a given token or a refresh token is known and valid
     *
     * @param encodedToken token to check
     * @param isToken      {@code true} if to check a token. {@code false} if the its a refresh token
     * @param scope        the scope for which is validated
     * @return {@code true} if the token is valid. Otherwise {@code false}
     */
    private boolean isValidInternal(String encodedToken, boolean isToken, String scope) {
        return getTokenInfo(encodedToken, isToken, scope).isPresent();
    }

    /**
     * Determines the known token and checks if it is valid
     *
     * @param encodedToken the encoded token
     * @return Optional of the known and valid decoded token.
     */
    public Optional<JsonWebToken> getToken(String encodedToken) {
        return getTokenInfo(encodedToken, true, null).map(TokenInfo::getToken);
    }

    /**
     * Determines the known token info and checks if it is valid
     *
     * @param encodedToken token to check
     * @param isToken      {@code true} if to check a token. {@code false} if the its a refresh token
     * @param scope        the scope for which is validated
     * @return Optional of the known and valid token info.
     */
    private Optional<TokenInfo> getTokenInfo(String encodedToken, boolean isToken, String scope) {
        LocalDateTime now = SystemProperties.getSystemDateTime();
        String logText = isToken ? "token" : "refresh token";
        Optional<JsonWebToken> token = JsonWebToken.decodeToken(encodedToken, secret);
        if (token.isEmpty()) {
            log.error("The {} {} could not be decoded", logText, encodedToken);
            return Optional.empty();
        }
        if (!inMemoryTokens.containsKey(token.get().getPayload().getJti())) {
            log.error("The {} {} is unknown", logText, encodedToken);
            return Optional.empty();
        }
        TokenInfo tokenInfo = inMemoryTokens.get(token.get().getPayload().getJti());
        JsonWebToken referenceToken = isToken ? tokenInfo.getToken() : tokenInfo.getRefreshToken();
        if (!referenceToken.getPayload().equals(token.get().getPayload())) {
            log.error("The {} {} is different to the known one", logText, encodedToken);
            return Optional.empty();
        }
        if (!tokenInfo.containsScope(scope)) {
            log.error("The {} {} has the wrong scope {}", logText, encodedToken, scope);
            return Optional.empty();
        }
        if (referenceToken.getPayload().getExp().isBefore(now)) {
            log.error("The {} {} is expired", logText, encodedToken);
            return Optional.empty();
        }
        return Optional.of(tokenInfo);
    }

    @Data
    @AllArgsConstructor
    public static class TokenInfo {
        public static final String DEFAULT_DELIMITER_SPLIT = "\\|";
        public static final String DEFAULT_DELIMITER = "|";

        private String id;
        private LocalDateTime expiresAtLeast;
        private JsonWebToken token;
        private JsonWebToken refreshToken;
        private Set<String> scopes;

        public TokenInfo(String id, LocalDateTime expiresAtLeast, JsonWebToken token, JsonWebToken refreshToken, String scopesText) {
            this(id, expiresAtLeast, token, refreshToken, (Set<String>) null);
            setScopes(scopesText);
        }

        /**
         * Sets the scopes for the actual token
         *
         * @param scopesText scopes separated by a delimiter
         * @param delimiter  the delimiter
         */
        public void setScopes(String scopesText, String delimiter) {
            scopes = new TreeSet<>();
            if (scopesText == null) {
                return;
            }
            for (String s : scopesText.split(delimiter)) {
                scopes.add(s.toLowerCase());
            }
        }

        /**
         * Sets the scopes for the actual token
         *
         * @param scopesText scopes separated by a default delimiter
         */
        public void setScopes(String scopesText) {
            setScopes(scopesText, DEFAULT_DELIMITER_SPLIT);
        }

        /**
         * Checks whether the a scope is supported by the tokens
         *
         * @param scope scope to check
         * @return {@code true} if the scope is null or the scope is supported by the tokens. Otherwise {@code false}
         */
        public boolean containsScope(String scope) {
            return scope == null || scopes.contains(scope.toLowerCase());
        }

        /**
         * @return The concatenated scope. Separated by default delimiter
         */
        public String getScope() {
            if (scopes.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            scopes.forEach(s -> {
                sb.append(s);
                sb.append(DEFAULT_DELIMITER);
            });
            return sb.substring(0, sb.length() - DEFAULT_DELIMITER.length());
        }
    }
}
