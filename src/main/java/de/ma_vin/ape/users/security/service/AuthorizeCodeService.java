package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.exceptions.CryptException;
import de.ma_vin.ape.users.security.EncoderUtil;
import de.ma_vin.ape.utils.properties.SystemProperties;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to provide operations on authorization codes
 */
@Service
@Data
@Log4j2
public class AuthorizeCodeService {

    @Value("${authorizeCodeExpiresInSeconds}")
    private Long authorizeCodeExpiresInSeconds;
    @Value("${tokenSecret}")
    private String secret;
    @Value("${encodingAlgorithm}")
    private String encodingAlgorithm;

    @Setter(AccessLevel.PRIVATE)
    private Set<CodeInfo> inMemoryCodes = new HashSet<>();

    /**
     * Clears all existing codes
     */
    public void clearAllCodes() {
        inMemoryCodes.clear();
    }

    /**
     * Clears all expired codes
     */
    public void clearExpiredCodes() {
        LocalDateTime now = SystemProperties.getSystemDateTime();
        Set<CodeInfo> codesToRemove = inMemoryCodes.stream()
                .filter(c -> c.getExpiresAt().isBefore(now))
                .collect(Collectors.toSet());
        log.debug("{} entries are expired", codesToRemove.size());
        inMemoryCodes.removeAll(codesToRemove);
    }

    /**
     * Issues a new code
     *
     * @param userId   identification of the user who authorize the code
     * @param clientId identification of the client who was asking for
     * @param scope    optional scope of the code
     * @return Optional of a new generated code
     */
    public Optional<String> issue(String userId, String clientId, String scope) {
        LocalDateTime expiresAt = SystemProperties.getSystemDateTime().plus(authorizeCodeExpiresInSeconds, ChronoUnit.SECONDS);
        try {
            String message = String.format("%s %s %s %s", userId, clientId, scope != null ? scope : "null", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(expiresAt));
            String code = EncoderUtil.encode(message, secret, encodingAlgorithm);
            inMemoryCodes.add(new CodeInfo(code, userId, clientId, scope, expiresAt));
            return Optional.of(code);
        } catch (CryptException e) {
            log.error("Could not create authorization code for user {}, client {} and scope {}", userId, clientId, scope != null ? scope : "null");
            return Optional.empty();
        }
    }

    /**
     * Determines the {@link CodeInfo} of a given code
     *
     * @param code Code whose info is ask for
     * @return Optional of the {@link CodeInfo}. {@link Optional#empty()} if there is no search result.
     */
    public Optional<CodeInfo> getCodeInfo(String code) {
        return inMemoryCodes.stream().filter(c -> c.getCode().equals(code)).findFirst();
    }

    /**
     * Checks whether is a given code is known and valid
     *
     * @param code authorization codes to check
     * @return {@code true} if the token is valid. Otherwise {@code false}
     */
    public boolean isValid(String code) {
        Optional<CodeInfo> codeInfo = getCodeInfo(code);
        return codeInfo.isPresent() && codeInfo.get().getExpiresAt().isAfter(SystemProperties.getSystemDateTime());
    }

    @Data
    @AllArgsConstructor
    public static class CodeInfo {
        String code;
        String userId;
        String clientId;
        String scope;
        LocalDateTime expiresAt;
    }
}
