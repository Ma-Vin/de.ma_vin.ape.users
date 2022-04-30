package de.ma_vin.ape.users.security.service.token;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

public class InMemoryTokenStorageServiceTest {

    @Mock
    private TokenIssuerService.TokenInfo tokenInfo;
    @Mock
    private TokenIssuerService.TokenInfo secondTokenInfo;
    @Mock
    private TokenIssuerService.TokenInfo thirdTokenInfo;

    private AutoCloseable openMocks;
    private InMemoryTokenStorageService cut;

    @BeforeEach
    public void setUp() {
        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2022, 3, 3, 19, 0));

        openMocks = openMocks(this);

        cut = new InMemoryTokenStorageService();
    }


    @DisplayName("Get an uuid")
    @Test
    public void testGetUuid() {
        String result = cut.getUuid();

        assertNotNull(result, "There should be any result");
        assertFalse(result.isEmpty(), "The result should not be empty");
    }

    @DisplayName("Find non existing token info")
    @Test
    public void testFindTokenInfoNotExisting() {
        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("anyId");
        assertNotNull(result, "There should be any result");
        assertTrue(result.isEmpty(), "The result should be empty");
    }

    @DisplayName("Put and find token info")
    @Test
    public void testPutAndFindTokenInfo() {
        cut.putTokenInfo("someId", tokenInfo);
        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("someId");
        assertNotNull(result, "There should be any result");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(tokenInfo, result.get(), "Wrong token info");
    }

    @DisplayName("Clear token info")
    @Test
    public void testClearToken() {
        cut.putTokenInfo("someId", tokenInfo);
        cut.clearToken("someId");
        assertTrue(cut.findToken("someId").isEmpty(), "The result should be empty");
    }

    @DisplayName("Clear all token info")
    @Test
    public void testClearAllTokens() {
        cut.putTokenInfo("someId", tokenInfo);
        cut.clearAllTokens();
        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("someId");
        assertNotNull(result, "There should be any result");
        assertTrue(result.isEmpty(), "The result should be empty");
    }

    @DisplayName("Clear expired token info")
    @Test
    public void testClearExpiredTokens() {
        when(tokenInfo.getExpiresAtLeast()).thenReturn(SystemProperties.getSystemDateTime().plusHours(1));
        cut.putTokenInfo("firstId", tokenInfo);
        when(secondTokenInfo.getExpiresAtLeast()).thenReturn(SystemProperties.getSystemDateTime().minusHours(1));
        cut.putTokenInfo("secondId", secondTokenInfo);
        cut.putTokenInfo("thirdId", thirdTokenInfo);

        cut.clearExpiredTokens(SystemProperties.getSystemDateTime());

        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("firstId");
        assertNotNull(result, "There should be any result for first id");
        assertTrue(result.isPresent(), "The result should be present for first id");
        assertEquals(tokenInfo, result.get(), "Wrong token info for first id");

        result = cut.findToken("secondId");
        assertNotNull(result, "There should be any result for second id");
        assertTrue(result.isEmpty(), "The result should be empty for second id");

        result = cut.findToken("thirdId");
        assertNotNull(result, "There should be any result for third id");
        assertTrue(result.isEmpty(), "The result should be empty for third id");
    }

    @DisplayName("The service should be a storing one")
    @Test
    public void testIsStoringTokens() {
        assertTrue(cut.isStoringTokens(), "The service should store tokens");
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }
}
