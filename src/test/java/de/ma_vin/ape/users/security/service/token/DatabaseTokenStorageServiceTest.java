package de.ma_vin.ape.users.security.service.token;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.users.model.gen.dao.security.TokenDao;
import de.ma_vin.ape.users.persistence.TokenRepository;
import de.ma_vin.ape.users.security.jwt.*;
import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class DatabaseTokenStorageServiceTest {
    private static final String SECRET = "SomeDummySecret";
    private static final String TOKEN_ID = "abc";
    private static final String SCOPE = "read";
    private static final String SUBJECT = "Me";

    @Mock
    private TokenIssuerService.TokenInfo tokenInfo;
    @Mock
    private TokenDao tokenDao;
    @Mock
    private TokenRepository tokenRepository;

    private AutoCloseable openMocks;
    private DatabaseTokenStorageService cut;

    private Header header;
    private Payload payload;
    private Payload refreshPayload;
    private Signature signature;
    private JsonWebToken token;
    private JsonWebToken refreshToken;

    @BeforeEach
    public void setUp() {
        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2022, 3, 3, 19, 0));

        openMocks = openMocks(this);

        cut = new DatabaseTokenStorageService();
        cut.setTokenRepository(tokenRepository);
        cut.setSecret(SECRET);

        header = new Header("JWT", null, "HS256");
        payload = new Payload(SUBJECT, SUBJECT, null, LocalDateTime.of(2021, 7, 2, 0, 0)
                , null, LocalDateTime.of(2021, 7, 1, 0, 0), TOKEN_ID);
        refreshPayload = new Payload(SUBJECT, SUBJECT, null, LocalDateTime.of(2021, 7, 3, 0, 0)
                , null, LocalDateTime.of(2021, 7, 1, 0, 0), TOKEN_ID);
        signature = new Signature(SECRET);
        token = new JsonWebToken(header, payload, signature);
        refreshToken = new JsonWebToken(header, refreshPayload, signature);

        when(tokenInfo.getToken()).thenReturn(token);
        when(tokenInfo.getRefreshToken()).thenReturn(refreshToken);
        when(tokenInfo.containsScope(any())).thenReturn(Boolean.TRUE);
        when(tokenInfo.getId()).thenReturn(TOKEN_ID);
        when(tokenInfo.getExpiresAtLeast()).thenReturn(refreshPayload.getExpAsLocalDateTime());
        when(tokenInfo.getScope()).thenReturn(SCOPE);
        when(tokenInfo.getScopes()).thenReturn(Collections.singleton(SCOPE));

        when(tokenDao.getUuid()).thenReturn(TOKEN_ID);
        when(tokenDao.getId()).thenReturn(1L);
        try {
            when(tokenDao.getToken()).thenReturn(token.getEncodedToken());
            when(tokenDao.getRefreshToken()).thenReturn(refreshToken.getEncodedToken());
        } catch (JwtGeneratingException e) {
            fail(e);
        }
        when(tokenDao.getScopes()).thenReturn(SCOPE);
        when(tokenDao.getValidFrom()).thenReturn(refreshToken.getPayload().getNbfAsLocalDateTime());
        when(tokenDao.getExpiresAtLeast()).thenReturn(refreshPayload.getExpAsLocalDateTime());
        when(tokenDao.getUserIdentification()).thenReturn(SUBJECT);
    }


    @DisplayName("Get an uuid")
    @Test
    public void testGetUuid() {
        String result = cut.getUuid();

        assertNotNull(result, "There should be any result");
        assertFalse(result.isEmpty(), "The result should not be empty");

        verify(tokenRepository).save(any());
    }

    @DisplayName("Get an uuid")
    @Test
    public void testGetUuidExisting() {
        when(tokenRepository.existsByUuid(any())).thenReturn(Boolean.TRUE, Boolean.FALSE);
        String result = cut.getUuid();

        assertNotNull(result, "There should be any result");
        assertFalse(result.isEmpty(), "The result should not be empty");

        verify(tokenRepository, times(2)).existsByUuid(any());
        verify(tokenRepository).save(any());
    }

    @DisplayName("Find non existing token info")
    @Test
    public void testFindTokenInfoNotExisting() {
        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("anyId");
        assertNotNull(result, "There should be any result");
        assertTrue(result.isEmpty(), "The result should be empty");
    }

    @DisplayName("Find token info")
    @Test
    public void testFindTokenInfo() {
        when(tokenRepository.findByUuid(eq("someId"))).thenReturn(Optional.of(tokenDao));

        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("someId");

        assertNotNull(result, "There should be any result");
        assertTrue(result.isPresent(), "The result should be present");
        TokenIssuerService.TokenInfo tokenInfo = result.get();
        assertEquals(token, tokenInfo.getToken(), "Wrong token");
        assertEquals(refreshToken, tokenInfo.getRefreshToken(), "Wrong refresh token");
        assertTrue(tokenInfo.containsScope(SCOPE), "scope should be contained");
        assertEquals(SCOPE, tokenInfo.getScope(), "Wrong token scope");
        assertEquals(TOKEN_ID, tokenInfo.getId(), "Wrong token id");
        assertEquals(refreshPayload.getExpAsLocalDateTime(), tokenInfo.getExpiresAtLeast(), "Wrong token expiration");
        assertTrue(tokenInfo.getScopes().contains(SCOPE), "Missing scope");
    }

    @DisplayName("Find token info, but without refresh token")
    @Test
    public void testFindTokenInfoWithoutRefreshToken() {
        when(tokenRepository.findByUuid(eq("someId"))).thenReturn(Optional.of(tokenDao));
        when(tokenDao.getRefreshToken()).thenReturn(null);

        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("someId");

        assertNotNull(result, "There should be any result");
        assertTrue(result.isPresent(), "The result should be present");
        TokenIssuerService.TokenInfo tokenInfo = result.get();
        assertEquals(token, tokenInfo.getToken(), "Wrong token");
        assertNull(tokenInfo.getRefreshToken(), "refresh token should not exists");
        assertTrue(tokenInfo.containsScope(SCOPE), "scope should be contained");
        assertEquals(SCOPE, tokenInfo.getScope(), "Wrong token scope");
        assertEquals(TOKEN_ID, tokenInfo.getId(), "Wrong token id");
        assertEquals(refreshPayload.getExpAsLocalDateTime(), tokenInfo.getExpiresAtLeast(), "Wrong token expiration");
        assertTrue(tokenInfo.getScopes().contains(SCOPE), "Missing scope");
    }

    @DisplayName("Find token info, but invalid token")
    @Test
    public void testFindTokenInfoInvalidToken() {
        when(tokenRepository.findByUuid(eq("someId"))).thenReturn(Optional.of(tokenDao));
        when(tokenDao.getToken()).thenReturn("anythingInvalid");

        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("someId");

        assertNotNull(result, "There should be any result");
        assertTrue(result.isEmpty(), "The result should be empty");
    }

    @DisplayName("Find token info, but invalid refresh token")
    @Test
    public void testFindTokenInfoInvalidRefreshToken() {
        when(tokenRepository.findByUuid(eq("someId"))).thenReturn(Optional.of(tokenDao));
        when(tokenDao.getRefreshToken()).thenReturn("anythingInvalid");

        Optional<TokenIssuerService.TokenInfo> result = cut.findToken("someId");

        assertNotNull(result, "There should be any result");
        assertTrue(result.isEmpty(), "The result should be empty");
    }

    @DisplayName("Put new token info")
    @Test
    public void testPutTokenNew() {
        when(tokenRepository.findByUuid(eq(TOKEN_ID))).thenReturn(Optional.empty());

        cut.putTokenInfo(TOKEN_ID, tokenInfo);

        verify(tokenRepository).findByUuid(eq(TOKEN_ID));
        verify(tokenRepository).save(any());
    }

    @DisplayName("Put existing token info")
    @Test
    public void testPutTokenExisting() {
        when(tokenRepository.findByUuid(eq(TOKEN_ID))).thenReturn(Optional.of(tokenDao));

        cut.putTokenInfo(TOKEN_ID, tokenInfo);

        verify(tokenRepository).findByUuid(eq(TOKEN_ID));
        verify(tokenRepository).save(any());
    }

    @DisplayName("Clear token info")
    @Test
    public void testClearToken() {
        cut.clearToken(TOKEN_ID);
        verify(tokenRepository).deleteByUuid(TOKEN_ID);
    }

    @DisplayName("Clear all token info")
    @Test
    public void testClearAllTokens() {
        cut.clearAllTokens();

        verify(tokenRepository).deleteAllInBatch();
    }

    @DisplayName("Clear expired token info")
    @Test
    public void testClearExpiredTokens() {
        cut.clearExpiredTokens(SystemProperties.getSystemDateTime());

        verify(tokenRepository).deleteExpired(eq(SystemProperties.getSystemDateTime()));
        verify(tokenRepository).deleteWithoutExpiration();
    }


    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }
}
