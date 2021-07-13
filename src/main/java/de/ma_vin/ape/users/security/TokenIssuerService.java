package de.ma_vin.ape.users.security;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.users.security.jwt.JsonWebToken;
import de.ma_vin.ape.users.security.jwt.Payload;
import de.ma_vin.ape.utils.generators.IdGenerator;
import de.ma_vin.ape.utils.properties.SystemProperties;
import lombok.Data;
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
    public Optional<String[]> refresh(String encodedRefreshToken) {
        if (!isValid(encodedRefreshToken, false)) {
            return Optional.empty();
        }

        Optional<JsonWebToken> refreshToken = JsonWebToken.decodeToken(encodedRefreshToken, secret);
        if(refreshToken.isEmpty()){
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

        try {
            return Optional.of(new String[]{tokenInfo.getToken().getEncodedToken(), tokenInfo.getRefreshToken().getEncodedToken()});
        } catch (JwtGeneratingException e) {
            log.error("Could not create encode token while refreshing ");
            return Optional.empty();
        }
    }

    /**
     * Issues a new Token and corresponding refresh token
     *
     * @param clientId id of the client who is issuing
     * @param username name of user whose is issued for
     * @param password the password of the user
     * @return new {@link Optional} of a pair token and refresh token
     */
    public Optional<String[]> issue(String clientId, String username, String password) {
        Optional<UserDao> user = userRepository.findById(IdGenerator.generateId(username, User.ID_PREFIX));
        if (user.isEmpty()) {
            log.warn("The user {} is unknown while issuing token", username);
            return Optional.empty();
        }
        if (!user.get().getPassword().equals(encoder.encode(password))) {
            log.warn("Wrong password for user {} while issuing token", username);
            return Optional.empty();
        }
        return issue(clientId, username);
    }

    /**
     * Issues a new Token and corresponding refresh token
     *
     * @param clientId id of the client who is issuing
     * @param username name of user whose is issued for
     * @return new {@link Optional} of a pair token and refresh token
     */
    private Optional<String[]> issue(String clientId, String username) {
        LocalDateTime now = SystemProperties.getSystemDateTime();

        String uuid = UUID.randomUUID().toString();
        while (inMemoryTokens.containsKey(uuid)) {
            uuid = UUID.randomUUID().toString();
        }

        Payload payload = new Payload(clientId, username, null, now.plus(tokenExpiresInSeconds, ChronoUnit.SECONDS), now, now, uuid);
        Payload refreshPayload = new Payload(clientId, username, null, now.plus(refreshTokenExpiresInSeconds, ChronoUnit.SECONDS), now, now, uuid);

        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setId(uuid);
        tokenInfo.setExpiresAtLeast(refreshPayload.getExp());
        tokenInfo.setToken(new JsonWebToken(encodingAlgorithm, secret, payload));
        tokenInfo.setRefreshToken(new JsonWebToken(encodingAlgorithm, secret, refreshPayload));

        inMemoryTokens.put(uuid, tokenInfo);
        try {
            return Optional.of(new String[]{tokenInfo.getToken().getEncodedToken(), tokenInfo.getRefreshToken().getEncodedToken()});
        } catch (JwtGeneratingException e) {
            log.error("Could not issue new encoded token");
            inMemoryTokens.remove(uuid);
            return Optional.empty();
        }
    }

    /**
     * Checks whether is a given token is known and valid
     *
     * @param encodedToken token to check
     * @return {@code true} if the token is valid. Otherwise {@code false}
     */
    public boolean isValid(String encodedToken) {
        return isValid(encodedToken, true);
    }

    /**
     * Checks whether is a given token or a refresh token is known and valid
     *
     * @param encodedToken token to check
     * @param isToken      {@code true} if to check a token. {@code false} if the its a refresh token
     * @return {@code true} if the token is valid. Otherwise {@code false}
     */
    private boolean isValid(String encodedToken, boolean isToken) {
        String logText = isToken ? "token" : "refresh token";
        Optional<JsonWebToken> token = JsonWebToken.decodeToken(encodedToken, secret);
        if (token.isEmpty()) {
            log.error("The {} {} could not be decoded", logText, encodedToken);
            return false;
        }

        if (!inMemoryTokens.containsKey(token.get().getPayload().getJti())) {
            log.error("The {} {} is unknown", logText, encodedToken);
            return false;
        }

        TokenInfo tokenInfo = inMemoryTokens.get(token.get().getPayload().getJti());
        JsonWebToken referenceToken = isToken ? tokenInfo.getToken() : tokenInfo.getRefreshToken();
        if (!referenceToken.getPayload().equals(token.get().getPayload())) {
            log.error("The {} {} is different to the known one", logText, encodedToken);
            return false;
        }
        return true;
    }

    @Data
    public static class TokenInfo {
        private String id;
        private LocalDateTime expiresAtLeast;
        private JsonWebToken token;
        private JsonWebToken refreshToken;
    }
}
