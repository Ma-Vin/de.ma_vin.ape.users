package de.ma_vin.ape.users.security.service.token;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.users.model.gen.dao.security.TokenDao;
import de.ma_vin.ape.users.persistence.TokenRepository;
import de.ma_vin.ape.users.security.jwt.JsonWebToken;
import de.ma_vin.ape.users.security.service.token.TokenIssuerService.TokenInfo;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Profile("database-token")
@Service
@Log4j2
@Data
public class DatabaseTokenStorageService implements ITokenStorageService {
    @Value("${tokenSecret}")
    private String secret;
    @Autowired
    private TokenRepository tokenRepository;


    @Override
    public boolean isStoringTokens() {
        return true;
    }

    @Override
    public void clearToken(String uuid) {
        tokenRepository.deleteByUuid(uuid);
    }

    @Override
    public void clearAllTokens() {
        tokenRepository.deleteAllInBatch();
    }

    @Override
    public void clearExpiredTokens(LocalDateTime actualDateTime) {
        long numDeleted = tokenRepository.deleteExpired(actualDateTime);
        long numDeletedWithoutExpiration = tokenRepository.deleteWithoutExpiration();
        log.debug("{} entries are expired compared to {}", numDeleted + numDeletedWithoutExpiration, actualDateTime);
    }

    @Override
    public Optional<TokenInfo> findToken(String uuid) {
        return tokenRepository.findByUuid(uuid).map(this::createTokenInfo);
    }

    @Override
    public void putTokenInfo(String uuid, TokenInfo tokenInfo) {
        TokenDao toStore = createTokenDao(tokenInfo);
        tokenRepository.findByUuid(uuid).ifPresentOrElse(
                t -> {
                    toStore.setId(t.getId());
                    tokenRepository.save(toStore);
                }
                , () -> tokenRepository.save(toStore)
        );
    }

    @Override
    public String getUuid() {
        String uuid = UUID.randomUUID().toString();
        while (tokenRepository.existsByUuid(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        TokenDao placeholder = new TokenDao();
        placeholder.setUuid(uuid);
        tokenRepository.save(placeholder);
        return uuid;
    }

    /**
     * Converts a given {@link TokenDao} to a {@link TokenInfo}
     *
     * @param tokenDao the dao to convert
     * @return the token info. {@code null} if the dao could not be converted
     */
    private TokenInfo createTokenInfo(TokenDao tokenDao) {
        Optional<JsonWebToken> tokenOpt = JsonWebToken.decodeToken(tokenDao.getToken(), secret);
        JsonWebToken refreshToken = getRefreshToken(tokenDao);
        if (tokenOpt.isEmpty() || (tokenDao.getRefreshToken() != null && refreshToken == null)) {
            return null;
        }
        return new TokenInfo(tokenDao.getUuid(), tokenDao.getExpiresAtLeast(), tokenOpt.get(), refreshToken, tokenDao.getScopes());
    }

    private JsonWebToken getRefreshToken(TokenDao tokenDao) {
        if (tokenDao.getRefreshToken() == null) {
            return null;
        }
        Optional<JsonWebToken> refreshToken = JsonWebToken.decodeToken(tokenDao.getRefreshToken(), secret);

        return refreshToken.isPresent() ? refreshToken.get() : null;
    }

    /**
     * Converts a given {@link TokenInfo} to a {@link TokenDao}
     *
     * @param tokenInfo the token info to convert
     * @return the dao. {@code null} if the dao could not be converted
     */
    private TokenDao createTokenDao(TokenInfo tokenInfo) {
        try {
            TokenDao result = new TokenDao();
            result.setUuid(tokenInfo.getId());
            result.setToken(tokenInfo.getToken().getEncodedToken());
            result.setRefreshToken(tokenInfo.getRefreshToken() != null ? tokenInfo.getRefreshToken().getEncodedToken() : null);
            result.setScopes(tokenInfo.getScope());
            result.setValidFrom(tokenInfo.getToken().getPayload().getNbfAsLocalDateTime());
            result.setExpiresAtLeast(tokenInfo.getExpiresAtLeast());
            result.setUserIdentification(tokenInfo.getToken().getPayload().getSub());
            return result;
        } catch (JwtGeneratingException e) {
            log.error("could not convert tokenInfo to tokenDao", e);
        }
        return null;
    }
}
