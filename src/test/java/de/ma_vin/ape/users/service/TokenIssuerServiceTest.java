package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.users.security.TokenIssuerService;
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
 * {@link de.ma_vin.ape.users.security.TokenIssuerService} is the class under test
 */
public class TokenIssuerServiceTest {

    private static final String SECRET = "DummySecret";
    private static final String ENCODING_ALGORITHM = "HS256";
    private static final Long TOKEN_EXPIRATION = 60L;
    private static final Long REFRESH_TOKEN_EXPIRATION = TOKEN_EXPIRATION + 60L;
    private static final String CLIENT_ID = "DummyClientId";
    private static final String USER_ID = IdGenerator.generateIdentification(1L, User.ID_PREFIX);
    private static final String USER_PWD = "DummyPwd";
    private static final String USER_ENCODED_PWD = "DummyPwdEncoded";

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
    }

    @DisplayName("The encoded token is invalid cause unable to decode")
    @Test
    public void testIsValidFailToDecode() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken() + "a";

        cut.getInMemoryTokens().put("abc", tokenInfo);

        assertFalse(cut.isValid(encodedToken), "The token should not be valid");
    }

    @DisplayName("The encoded token is invalid cause the token is not known")
    @Test
    public void testIsValidUnknown() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();

        cut.getInMemoryTokens().put("abcd", tokenInfo);

        assertFalse(cut.isValid(encodedToken), "The token should not be valid");
    }

    @DisplayName("The encoded token is invalid cause is different to the known one")
    @Test
    public void testIsValidDiffers() throws JwtGeneratingException {
        Payload otherPayload = new Payload("You", "You", null, LocalDateTime.of(2021, 7, 2, 0, 0)
                , null, LocalDateTime.of(2021, 7, 1, 0, 0), "abc");
        JsonWebToken otherToken = new JsonWebToken(header, otherPayload, signature);
        String encodedToken = otherToken.getEncodedToken();

        cut.getInMemoryTokens().put("abc", tokenInfo);

        assertFalse(cut.isValid(encodedToken), "The token should not be valid");
    }

    @DisplayName("The encoded token is valid")
    @Test
    public void testIsValid() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();

        cut.getInMemoryTokens().put("abc", tokenInfo);

        assertTrue(cut.isValid(encodedToken), "The token should be valid");
    }

    @DisplayName("Issue a new pair of token and refresh token with user and password")
    @Test
    public void testIssueToken() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(userDao));
        when(userDao.getPassword()).thenReturn(USER_ENCODED_PWD);
        when(encoder.encode(eq(USER_PWD))).thenReturn(USER_ENCODED_PWD);

        Optional<String[]> result = cut.issue(CLIENT_ID, USER_ID, USER_PWD);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(2, result.get().length, "Wrong number of entries");
        assertNotNull(result.get()[0], "The first token should not be null");
        assertFalse(result.get()[0].trim().isEmpty(), "The first token should not be empty");
        assertNotNull(result.get()[1], "The second token should not be null");
        assertFalse(result.get()[1].trim().isEmpty(), "The second token should not be empty");

        verify(userRepository).findById(eq(1L));
        verify(encoder).encode(eq(USER_PWD));
    }

    @DisplayName("Issue a new pair of token and refresh token with missing user")
    @Test
    public void testIssueTokenMissingUser() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());
        when(userDao.getPassword()).thenReturn(USER_ENCODED_PWD);
        when(encoder.encode(eq(USER_PWD))).thenReturn(USER_ENCODED_PWD);

        Optional<String[]> result = cut.issue(CLIENT_ID, USER_ID, USER_PWD);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).findById(eq(1L));
        verify(encoder, never()).encode(eq(USER_PWD));
    }

    @DisplayName("Issue a new pair of token and refresh token with wrong user password")
    @Test
    public void testIssueTokenWrongPassword() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(userDao));
        when(userDao.getPassword()).thenReturn(USER_ENCODED_PWD);
        when(encoder.encode(eq(USER_PWD))).thenReturn(USER_ENCODED_PWD + "_mod");

        Optional<String[]> result = cut.issue(CLIENT_ID, USER_ID, USER_PWD);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).findById(eq(1L));
        verify(encoder).encode(eq(USER_PWD));
    }

    @DisplayName("Refresh a valid token")
    @Test
    public void testRefresh() throws JwtGeneratingException {
        String encodedToken = token.getEncodedToken();
        String encodedRefreshToken = refreshToken.getEncodedToken();

        cut.getInMemoryTokens().put("abc", tokenInfo);

        Optional<String[]> result = cut.refresh(encodedRefreshToken);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(2, result.get().length, "Wrong number of entries");
        assertNotNull(result.get()[0], "The first token should not be null");
        assertFalse(result.get()[0].trim().isEmpty(), "The first token should not be empty");
        assertNotNull(result.get()[1], "The second token should not be null");
        assertFalse(result.get()[1].trim().isEmpty(), "The second token should not be empty");
        assertNotEquals(encodedToken, result.get()[0], "The token should be changed");
        assertNotEquals(encodedRefreshToken, result.get()[1], "The refresh token should be changed");
    }

    @DisplayName("Refresh a invalid token")
    @Test
    public void testRefreshInvalidToken() throws JwtGeneratingException {
        String encodedRefreshToken = refreshToken.getEncodedToken() + "_mod";

        cut.getInMemoryTokens().put("abc", tokenInfo);

        Optional<String[]> result = cut.refresh(encodedRefreshToken);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");
    }

    @DisplayName("Refresh a expired token")
    @Test
    public void testRefreshExpiredToken() throws JwtGeneratingException {
        String encodedRefreshToken = refreshToken.getEncodedToken();
        SystemProperties.getInstance().setTestingDateTime(refreshPayload.getExp().plus(30L, ChronoUnit.MINUTES));

        cut.getInMemoryTokens().put("abc", tokenInfo);

        Optional<String[]> result = cut.refresh(encodedRefreshToken);
        assertNotNull(result, "There should be a result");
        assertTrue(result.isEmpty(), "The result should be empty");
    }

    @DisplayName("Clear all tokens")
    @Test
    public void testClearAllTokens() {
        when(tokenInfo.getExpiresAtLeast()).thenReturn(LocalDateTime.of(2021, 7, 1, 0, 0), LocalDateTime.of(2021, 7, 2, 0, 0));
        cut.getInMemoryTokens().put("abc", tokenInfo);
        cut.getInMemoryTokens().put("def", tokenInfo);

        cut.clearAllTokens();

        assertTrue(cut.getInMemoryTokens().isEmpty(), "All tokens should be cleared");
    }

    @DisplayName("Clear all expired tokens")
    @Test
    public void testClearExpiredTokens() {
        when(tokenInfo.getExpiresAtLeast()).thenReturn(LocalDateTime.of(2021, 7, 1, 0, 0), LocalDateTime.of(2021, 7, 2, 0, 0));
        cut.getInMemoryTokens().put("abc", tokenInfo);
        cut.getInMemoryTokens().put("def", tokenInfo);

        cut.clearExpiredTokens();

        assertFalse(cut.getInMemoryTokens().isEmpty(), "In memory tokens should not be empty");
        assertTrue(cut.getInMemoryTokens().containsKey("def"), "The latest token should be still contained");
    }


    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
        SystemProperties.getInstance(null, null);
    }
}
