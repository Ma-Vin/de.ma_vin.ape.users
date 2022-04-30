package de.ma_vin.ape.users.security.service.token;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


/**
 * Class which does not store the jwt, so only the verification of the jwt is left
 */
@Profile("jwt-verification-token")
@Service
@Log4j2
public class NonStoringTokenStorageService implements ITokenStorageService {

    @Override
    public boolean isStoringTokens() {
        return false;
    }

    @Override
    public void clearToken(String uuid) {
        log.debug("Nothing to clear since nothing was stored.");
    }

    @Override
    public void clearAllTokens() {
        log.debug("nothing to clear since nothing was stored");
    }

    @Override
    public void clearExpiredTokens(LocalDateTime actualDateTime) {
        log.debug("nothing to clear since nothing was stored");
    }

    @Override
    public Optional<TokenIssuerService.TokenInfo> findToken(String uuid) {
        log.debug("cannot find a token since nothing was stored");
        return Optional.empty();
    }

    @Override
    public void putTokenInfo(String uuid, TokenIssuerService.TokenInfo tokenInfo) {
        log.debug("cannot put a token since nothing will be stored");
    }

    @Override
    public String getUuid() {
        return UUID.randomUUID().toString();
    }
}
