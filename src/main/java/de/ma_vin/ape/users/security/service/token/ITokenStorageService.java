package de.ma_vin.ape.users.security.service.token;

import de.ma_vin.ape.users.security.service.token.TokenIssuerService.TokenInfo;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ITokenStorageService {

    /**
     * Clears a token
     *
     * @param uuid the id of the token to delete
     */
    void clearToken(String uuid);

    /**
     * Clears all known tokens
     */
    void clearAllTokens();

    /**
     * Clears all expired tokens
     *
     * @param actualDateTime the reference date time
     */
    void clearExpiredTokens(LocalDateTime actualDateTime);

    /**
     * Determines the token info to a given uuid
     *
     * @param uuid the uuid of the search token
     * @return the token info
     */
    Optional<TokenInfo> findToken(String uuid);

    /**
     * puts the token info
     *
     * @param uuid      the id where to put at
     * @param tokenInfo the token info t oput
     */
    void putTokenInfo(String uuid, TokenInfo tokenInfo);

    /**
     * Creates a new uuid
     *
     * @return the generated
     */
    String getUuid();
}
