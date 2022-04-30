package de.ma_vin.ape.users.security.service.token;

import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NonStoringTokenStorageService} is the class under test
 */
public class NonStoringTokenStorageServiceTest {
    NonStoringTokenStorageService cut;

    @BeforeEach
    public void setUp() {
        cut = new NonStoringTokenStorageService();
    }

    @DisplayName("The service should not be a storing one")
    @Test
    public void testIsStoringTokens() {
        assertFalse(cut.isStoringTokens(), "The service should not store tokens");
    }


    @DisplayName("The service should do nothing at clearing")
    @Test
    public void testClearAllTokens() {
        cut.clearAllTokens();
    }

    @DisplayName("The service should do nothing at clearing expired tokens")
    @Test
    public void testClearExpiredTokens() {
        cut.clearExpiredTokens(SystemProperties.getSystemDateTime());
    }

    @DisplayName("The service should not find anythinf")
    @Test
    public void testFindToken() {
        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("AnyUUID");

        assertNotNull(result, "There should be any result");
        assertTrue(result.isEmpty(), "The result should be empty");
    }

    @DisplayName("The service should not but anything")
    @Test
    public void testPutTokenInfo() {
        cut.putTokenInfo("anyUUID", null);
    }

    @DisplayName("Get an uuid")
    @Test
    public void testGetUuid() {
        String result = cut.getUuid();

        assertNotNull(result, "There should be any result");
        assertFalse(result.isEmpty(), "The result should not be empty");
    }
}