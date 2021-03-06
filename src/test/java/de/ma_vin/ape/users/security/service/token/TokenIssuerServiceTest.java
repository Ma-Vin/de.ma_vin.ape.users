package de.ma_vin.ape.users.security.service.token;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.users.security.jwt.Header;
import de.ma_vin.ape.users.security.jwt.JsonWebToken;
import de.ma_vin.ape.users.security.jwt.Payload;
import de.ma_vin.ape.users.security.jwt.Signature;
import de.ma_vin.ape.utils.generators.IdGenerator;
import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link TokenIssuerService} is the class under test
 */
public class TokenIssuerServiceTest {

    private static final String SECRET = "DummySecret";
    private static final String ENCODING_ALGORITHM = "HS256";
    private static final Long TOKEN_EXPIRATION = 60L;
    private static final Long REFRESH_TOKEN_EXPIRATION = TOKEN_EXPIRATION + 60L;
    private static final String CLIENT_ID = "DummyClientId";
    private static final String CLIENT_URL = "http://localhost/oauth/token";
    private static final String USER_ID = IdGenerator.generateIdentification(1L, User.ID_PREFIX);
    private static final String USER_PWD = "DummyPwd";
    private static final String USER_URL_ENCODED_PWD = "RHVtbXlQd2Q=";
    private static final String USER_URL_DECODED_PWD = USER_PWD;

    private AutoCloseable openMocks;
    private TokenIssuerService cut;

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private TokenIssuerService.TokenInfo tokenInfo;
    @Mock
    private UserDao userDao;
    @Mock
    private ITokenStorageService tokenStorageService;

    private Header header;
    private Payload payload;
    private Payload refreshPayload;
    private Signature signature;
    private JsonWebToken token;
    private JsonWebToken refreshToken;

    @BeforeEach
    public void setUp() {
        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2021, 7, 1, 12, 0));

        openMocks = openMocks(this);

        cut = new TokenIssuerService();
        cut.setUserRepository(userRepository);
        cut.setTokenStorageService(tokenStorageService);
        cut.setEncoder(encoder);
        cut.setSecret(SECRET);
        cut.setTokenExpiresInSeconds(TOKEN_EXPIRATION);
        cut.setRefreshTokenExpiresInSeconds(REFRESH_TOKEN_EXPIRATION);
        cut.setEncodingAlgorithm(ENCODING_ALGORITHM);

        header = new Header("JWT", null, "HS256");
        payload = new Payload("Me", "Me", null, LocalDateTime.of(2021, 7, 2, 0, 0)
                , null, LocalDateTime.of(2021, 7, 1, 0, 0), "abc");
        refreshPayload = new Payload("Me", "Me", null, LocalDateTime.of(2021, 7, 3, 0, 0)
                , null, LocalDateTime.of(2021, 7, 1, 0, 0), "abc");
        signature = new Signature(SECRET);
        token = new JsonWebToken(header, payload, signature);
        refreshToken = new JsonWebToken(header, refreshPayload, signature);
        when(tokenInfo.getToken()).thenReturn(token);
        when(tokenInfo.getRefreshToken()).thenReturn(refreshToken);
        when(tokenInfo.containsScope(any())).thenReturn(Boolean.TRUE);

        when(tokenStorageService.findToken(any())).thenReturn(Optional.empty());
        when(tokenStorageService.findToken(eq("abc"))).thenReturn(Optional.of(tokenInfo));
        when(tokenStorageService.getUuid()).thenReturn("abcd");
        when(tokenStorageService.isStoringTokens()).thenReturn(Boolean.TRUE);
    }

    @DisplayName("The encoded token is invalid cause unable to decode")
    @Test
    public void testIsValidFailToDecode() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken() + "a";

        assertFalse(cut.isValid(encodedToken), "The token should not be valid");
    }

    @DisplayName("The encoded token is invalid cause the token is not known")
    @Test
    public void testIsValidUnknown() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();

        when(tokenStorageService.findToken(any())).thenReturn(Optional.empty());

        assertFalse(cut.isValid(encodedToken), "The token should not be valid");
    }

    @DisplayName("The encoded token is invalid cause the token is expired")
    @Test
    public void testIsValidExpired() throws JwtGeneratingException {
        SystemProperties.getInstance().setTestingDateTime(payload.getExpAsLocalDateTime().plus(1, ChronoUnit.SECONDS));
        String encodedToken = token.getEncodedToken();

        assertFalse(cut.isValid(encodedToken), "The token should not be valid");
    }

    @DisplayName("The encoded token is invalid cause is different to the known one")
    @Test
    public void testIsValidDiffers() throws JwtGeneratingException {
        Payload otherPayload = new Payload("You", "You", null, LocalDateTime.of(2021, 7, 2, 0, 0)
                , null, LocalDateTime.of(2021, 7, 1, 0, 0), "abc");
        JsonWebToken otherToken = new JsonWebToken(header, otherPayload, signature);
        String encodedToken = otherToken.getEncodedToken();

        assertFalse(cut.isValid(encodedToken), "The token should not be valid");
    }

    @DisplayName("The encoded token is valid")
    @Test
    public void testIsValid() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();

        assertTrue(cut.isValid(encodedToken), "The token should be valid");
    }

    @DisplayName("The encoded token is valid with scope")
    @Test
    public void testIsValidWithScope() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();

        assertTrue(cut.isValid(encodedToken, "read"), "The token should be valid");
    }

    @DisplayName("The encoded token is valid and not store but is not needed to")
    @Test
    public void testIsValidNotStoredButNotNeeded() throws JwtGeneratingException {
        when(tokenStorageService.isStoringTokens()).thenReturn(Boolean.FALSE);
        when(tokenStorageService.findToken(any())).thenReturn(Optional.empty());

        String encodedToken = token.getEncodedToken();

        assertTrue(cut.isValid(encodedToken), "The token should be valid");
        verify(tokenStorageService).isStoringTokens();
    }

    @DisplayName("The encoded token could not be determined cause unable to decode")
    @Test
    public void testGetTokenFailToDecode() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken() + "a";

        Optional<JsonWebToken> result = cut.getToken(encodedToken);
        assertNotNull(result, "There should token should be any result");
        assertTrue(result.isEmpty(), "There should not be any token at the result");

        verify(tokenStorageService, never()).findToken(any());
    }

    @DisplayName("The encoded token could not be determined cause the token is not known")
    @Test
    public void testGetTokenUnknown() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();

        when(tokenStorageService.findToken(any())).thenReturn(Optional.empty());

        Optional<JsonWebToken> result = cut.getToken(encodedToken);
        assertNotNull(result, "There should token should be any result");
        assertTrue(result.isEmpty(), "There should not be any token at the result");

        verify(tokenStorageService).findToken(any());
    }

    @DisplayName("The encoded token could not be determined cause the token is expired")
    @Test
    public void testGetTokenExpired() throws JwtGeneratingException {
        SystemProperties.getInstance().setTestingDateTime(payload.getExpAsLocalDateTime().plus(1, ChronoUnit.SECONDS));
        String encodedToken = token.getEncodedToken();

        Optional<JsonWebToken> result = cut.getToken(encodedToken);
        assertNotNull(result, "There should token should be any result");
        assertTrue(result.isEmpty(), "There should not be any token at the result");

        verify(tokenStorageService).findToken(any());
    }

    @DisplayName("The encoded token could not be determined cause is different to the known one")
    @Test
    public void testGetTokenDiffers() throws JwtGeneratingException {
        Payload otherPayload = new Payload("You", "You", null, LocalDateTime.of(2021, 7, 2, 0, 0)
                , null, LocalDateTime.of(2021, 7, 1, 0, 0), "abc");
        JsonWebToken otherToken = new JsonWebToken(header, otherPayload, signature);
        String encodedToken = otherToken.getEncodedToken();

        Optional<JsonWebToken> result = cut.getToken(encodedToken);
        assertNotNull(result, "There should token should be any result");
        assertTrue(result.isEmpty(), "There should not be any token at the result");

        verify(tokenStorageService).findToken(any());
    }

    @DisplayName("The encoded token could be determined")
    @Test
    public void testGetToken() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();

        Optional<JsonWebToken> result = cut.getToken(encodedToken);
        assertNotNull(result, "There should token should be any result");
        assertTrue(result.isPresent(), "There should be a token at the result");
        assertEquals(token, result.get(), "Wrong token");

        verify(tokenStorageService).findToken(any());
    }

    @DisplayName("Issue a new pair of token and refresh token with user and password")
    @Test
    public void testIssueToken() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(userDao));
        when(userDao.getPassword()).thenReturn(USER_PWD);
        when(encoder.matches(eq(USER_URL_DECODED_PWD), eq(USER_PWD))).thenReturn(Boolean.TRUE);

        Optional<TokenIssuerService.TokenInfo> result = cut.issue(CLIENT_URL, USER_ID, USER_URL_ENCODED_PWD);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertNotNull(result.get().getToken(), "The token should not be null");
        assertNotNull(result.get().getRefreshToken(), "The refresh token should not be null");

        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq(USER_URL_DECODED_PWD), eq(USER_PWD));
        verify(tokenStorageService).getUuid();
        verify(tokenStorageService).putTokenInfo(any(), any());
    }

    @DisplayName("Issue a new pair of token and refresh token with user, password and scope")
    @Test
    public void testIssueTokenWithScope() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(userDao));
        when(userDao.getPassword()).thenReturn(USER_PWD);
        when(encoder.matches(eq(USER_URL_DECODED_PWD), eq(USER_PWD))).thenReturn(Boolean.TRUE);

        Optional<TokenIssuerService.TokenInfo> result = cut.issue(CLIENT_URL, USER_ID, USER_URL_ENCODED_PWD, "read|Write");
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertNotNull(result.get().getToken(), "The token should not be null");
        assertNotNull(result.get().getRefreshToken(), "The refresh token should not be null");

        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq(USER_URL_DECODED_PWD), eq(USER_PWD));
        verify(tokenStorageService).getUuid();
        verify(tokenStorageService).putTokenInfo(any(), any());
    }

    @DisplayName("Issue a new pair of token and refresh token with missing user")
    @Test
    public void testIssueTokenMissingUser() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());
        when(userDao.getPassword()).thenReturn(USER_PWD);
        when(encoder.matches(eq(USER_URL_DECODED_PWD), eq(USER_PWD))).thenReturn(Boolean.TRUE);

        Optional<TokenIssuerService.TokenInfo> result = cut.issue(CLIENT_URL, USER_ID, USER_URL_ENCODED_PWD);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).findById(eq(1L));
        verify(encoder, never()).matches(eq(USER_URL_DECODED_PWD), eq(USER_PWD));
        verify(tokenStorageService, never()).getUuid();
        verify(tokenStorageService, never()).putTokenInfo(any(), any());
    }

    @DisplayName("Issue a new pair of token and refresh token with wrong user password")
    @Test
    public void testIssueTokenWrongPassword() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(userDao));
        when(userDao.getPassword()).thenReturn(USER_PWD);
        when(encoder.matches(eq(USER_URL_DECODED_PWD), eq(USER_PWD))).thenReturn(Boolean.FALSE);

        Optional<TokenIssuerService.TokenInfo> result = cut.issue(CLIENT_URL, USER_ID, USER_URL_ENCODED_PWD);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq(USER_URL_DECODED_PWD), eq(USER_PWD));
        verify(tokenStorageService, never()).getUuid();
        verify(tokenStorageService, never()).putTokenInfo(any(), any());
    }

    @DisplayName("Issue a new pair of implicit token and refresh token with user and password")
    @Test
    public void testIssueImplicitToken() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(userDao));
        when(userDao.getPassword()).thenReturn(USER_URL_ENCODED_PWD);
        when(encoder.encode(eq(USER_PWD))).thenReturn(USER_URL_ENCODED_PWD);

        Optional<TokenIssuerService.TokenInfo> result = cut.issueImplicit(CLIENT_URL, USER_ID);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertNotNull(result.get().getToken(), "The token should not be null");
        assertNotNull(result.get().getRefreshToken(), "The refresh token should not be null");

        verify(userRepository).findById(eq(1L));
        verify(encoder, never()).encode(eq(USER_PWD));
        verify(tokenStorageService).getUuid();
        verify(tokenStorageService).putTokenInfo(any(), any());
    }

    @DisplayName("Issue a new pair of implicit token and refresh token with user, password and scope")
    @Test
    public void testIssueImplicitTokenWithScope() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(userDao));
        when(userDao.getPassword()).thenReturn(USER_URL_ENCODED_PWD);
        when(encoder.encode(eq(USER_PWD))).thenReturn(USER_URL_ENCODED_PWD);

        Optional<TokenIssuerService.TokenInfo> result = cut.issueImplicit(CLIENT_URL, USER_ID, "read|Write");
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertNotNull(result.get().getToken(), "The token should not be null");
        assertNotNull(result.get().getRefreshToken(), "The refresh token should not be null");

        verify(userRepository).findById(eq(1L));
        verify(encoder, never()).encode(eq(USER_PWD));
        verify(tokenStorageService).getUuid();
        verify(tokenStorageService).putTokenInfo(any(), any());
    }

    @DisplayName("Issue a new pair of implicit token and refresh token with missing user")
    @Test
    public void testIssueImplicitTokenMissingUser() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());
        when(userDao.getPassword()).thenReturn(USER_URL_ENCODED_PWD);
        when(encoder.encode(eq(USER_PWD))).thenReturn(USER_URL_ENCODED_PWD);

        Optional<TokenIssuerService.TokenInfo> result = cut.issueImplicit(CLIENT_URL, USER_ID, USER_PWD);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).findById(eq(1L));
        verify(encoder, never()).encode(eq(USER_PWD));
        verify(tokenStorageService, never()).getUuid();
        verify(tokenStorageService, never()).putTokenInfo(any(), any());
    }

    @DisplayName("Issue a new pair of client token and refresh token with user and password")
    @Test
    public void testIssueClientToken() {
        Optional<TokenIssuerService.TokenInfo> result = cut.issueClient(CLIENT_URL, CLIENT_ID);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertNotNull(result.get().getToken(), "The token should not be null");
        assertNotNull(result.get().getRefreshToken(), "The refresh token should not be null");

        verify(tokenStorageService).getUuid();
        verify(tokenStorageService).putTokenInfo(any(), any());
    }

    @DisplayName("Issue a new pair of client token and refresh token with user, password and scope")
    @Test
    public void testIssueClientTokenWithScope() {
        Optional<TokenIssuerService.TokenInfo> result = cut.issueClient(CLIENT_URL, CLIENT_ID, "read|Write");
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertNotNull(result.get().getToken(), "The token should not be null");
        assertNotNull(result.get().getRefreshToken(), "The refresh token should not be null");

        verify(tokenStorageService).getUuid();
        verify(tokenStorageService).putTokenInfo(any(), any());
    }

    @DisplayName("Refresh a valid token")
    @Test
    public void testRefresh() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();
        String encodedRefreshToken = refreshToken.getEncodedToken();

        Optional<TokenIssuerService.TokenInfo> result = cut.refresh(encodedRefreshToken);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertNotNull(result.get().getToken(), "The token should not be null");
        assertNotNull(result.get().getRefreshToken(), "The refresh token should not be null");
        assertNotEquals(encodedToken, result.get().getToken().getEncodedToken(), "The token should be changed");
        assertNotEquals(encodedRefreshToken, result.get().getRefreshToken().getEncodedToken(), "The refresh token should be changed");

        verify(tokenStorageService, times(2)).findToken(any());
        verify(tokenStorageService, times(2)).isStoringTokens();
        verify(tokenStorageService).getUuid();
        verify(tokenStorageService).putTokenInfo(any(), any());
    }

    @DisplayName("Refresh a invalid token")
    @Test
    public void testRefreshInvalidToken() throws JwtGeneratingException {
        String encodedRefreshToken = refreshToken.getEncodedToken() + "_mod";

        Optional<TokenIssuerService.TokenInfo> result = cut.refresh(encodedRefreshToken);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(tokenStorageService, never()).findToken(any());
        verify(tokenStorageService, never()).isStoringTokens();
        verify(tokenStorageService, never()).getUuid();
        verify(tokenStorageService, never()).putTokenInfo(any(), any());
    }

    @DisplayName("Refresh a expired token")
    @Test
    public void testRefreshExpiredToken() throws JwtGeneratingException {
        String encodedRefreshToken = refreshToken.getEncodedToken();
        SystemProperties.getInstance().setTestingDateTime(refreshPayload.getExpAsLocalDateTime().plus(30L, ChronoUnit.MINUTES));

        Optional<TokenIssuerService.TokenInfo> result = cut.refresh(encodedRefreshToken);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(tokenStorageService).findToken(any());
        verify(tokenStorageService).isStoringTokens();
        verify(tokenStorageService, never()).getUuid();
        verify(tokenStorageService, never()).putTokenInfo(any(), any());
    }

    @DisplayName("Refresh a not stored token at not storing service")
    @Test
    public void testRefreshNonStoredTokenButNotNeeded() throws JwtGeneratingException {
        when(tokenStorageService.isStoringTokens()).thenReturn(Boolean.FALSE);
        when(tokenStorageService.findToken(any())).thenReturn(Optional.empty());
        String encodedToken = token.getEncodedToken();
        String encodedRefreshToken = refreshToken.getEncodedToken();

        Optional<TokenIssuerService.TokenInfo> result = cut.refresh(encodedRefreshToken);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertNotNull(result.get().getToken(), "The token should not be null");
        assertNotNull(result.get().getRefreshToken(), "The refresh token should not be null");
        assertNotEquals(encodedToken, result.get().getToken().getEncodedToken(), "The token should be changed");
        assertNotEquals(encodedRefreshToken, result.get().getRefreshToken().getEncodedToken(), "The refresh token should be changed");

        verify(tokenStorageService, never()).findToken(any());
        verify(tokenStorageService, times(3)).isStoringTokens();
        verify(tokenStorageService, times(3)).getUuid();
        verify(tokenStorageService).putTokenInfo(any(), any());
    }

    @DisplayName("Clear all tokens")
    @Test
    public void testClearAllTokens() {
        cut.clearAllTokens();

        verify(tokenStorageService).clearAllTokens();
    }

    @DisplayName("Clear all expired tokens")
    @Test
    public void testClearExpiredTokens() {
        cut.clearExpiredTokens();

        verify(tokenStorageService).clearExpiredTokens(eq(SystemProperties.getSystemDateTime()));
    }


    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
        SystemProperties.getInstance(null, null);
    }
}
