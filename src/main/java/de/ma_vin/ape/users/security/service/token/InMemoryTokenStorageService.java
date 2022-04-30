package de.ma_vin.ape.users.security.service.token;

import de.ma_vin.ape.users.security.service.token.TokenIssuerService.TokenInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Profile("memory-token")
@Service
@Log4j2
public class InMemoryTokenStorageService implements ITokenStorageService {

    private Map<String, TokenInfo> inMemoryTokens = new HashMap<>();

    @Override
    public boolean isStoringTokens() {
        return true;
    }

    @Override
    public void clearToken(String uuid) {
        inMemoryTokens.remove(uuid);
    }

    @Override
    public void clearAllTokens() {
        inMemoryTokens.clear();
    }

    @Override
    public void clearExpiredTokens(LocalDateTime actualDateTime) {
        Set<String> keysToRemove = inMemoryTokens.entrySet().stream()
                .filter(e -> e.getValue().getExpiresAtLeast() == null || e.getValue().getExpiresAtLeast().isBefore(actualDateTime))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        log.debug("{} entries are expired compared to {}", keysToRemove.size(), actualDateTime);
        keysToRemove.forEach(inMemoryTokens::remove);
    }

    @Override
    public Optional<TokenInfo> findToken(String uuid) {
        if (inMemoryTokens.containsKey(uuid)) {
            return Optional.of(inMemoryTokens.get(uuid));
        }
        log.debug("There was no token for uuid {}", uuid);
        return Optional.empty();
    }

    @Override
    public void putTokenInfo(String uuid, TokenInfo tokenInfo) {
        inMemoryTokens.put(uuid, tokenInfo);
    }

    @Override
    public String getUuid() {
        String uuid = UUID.randomUUID().toString();
        while (inMemoryTokens.containsKey(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }
}
