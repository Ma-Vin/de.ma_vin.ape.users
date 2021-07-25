package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.utils.generators.IdGenerator;
import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link AuthorizeCodeService} is the class under test
 */
public class AuthorizeCodeServiceTest {

    private static final String SECRET = "DummySecret";
    private static final String ENCODING_ALGORITHM = "HS256";
    private static final String CLIENT_ID = "DummyClientId";
    private static final String USER_ID = IdGenerator.generateIdentification(1L, User.ID_PREFIX);
    private static final String SCOPE = "DummyScope";
    private static final String CODE = "DummyCode";
    private static final Long CODE_EXPIRATION = 600L;

    private AutoCloseable openMocks;
    private AuthorizeCodeService cut;

    private LocalDateTime expiresAt;

    @Mock
    private AuthorizeCodeService.CodeInfo codeInfo;

    @BeforeEach
    public void setUp() {
        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2021, 7, 1, 12, 0));
        expiresAt = SystemProperties.getSystemDateTime().plus(CODE_EXPIRATION, ChronoUnit.SECONDS);

        openMocks = openMocks(this);

        cut = new AuthorizeCodeService();
        cut.setSecret(SECRET);
        cut.setEncodingAlgorithm(ENCODING_ALGORITHM);
        cut.setAuthorizeCodeExpiresInSeconds(CODE_EXPIRATION);

        when(codeInfo.getCode()).thenReturn(CODE);
        when(codeInfo.getClientId()).thenReturn(CLIENT_ID);
        when(codeInfo.getUserId()).thenReturn(USER_ID);
        when(codeInfo.getScope()).thenReturn(SCOPE);
        when(codeInfo.getExpiresAt()).thenReturn(expiresAt);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("The code is valid")
    @Test
    public void testIsValid() {
        cut.getInMemoryCodes().add(codeInfo);

        boolean result = cut.isValid(CODE);
        assertTrue(result, "The result should be true");
    }

    @DisplayName("The code is expired and invalid")
    @Test
    public void testIsValidExpired() {
        cut.getInMemoryCodes().add(codeInfo);
        when(codeInfo.getExpiresAt()).thenReturn(SystemProperties.getSystemDateTime().minus(1L, ChronoUnit.SECONDS));

        boolean result = cut.isValid(CODE);
        assertFalse(result, "The result should be false");
    }

    @DisplayName("The code is unknown and invalid")
    @Test
    public void testIsValidUnknown() {
        boolean result = cut.isValid(CODE);
        assertFalse(result, "The result should be false");
    }

    @DisplayName("Clear all codes")
    @Test
    public void testClearAllCodes() {
        when(codeInfo.getExpiresAt()).thenReturn(LocalDateTime.of(2021, 7, 1, 0, 0));
        AuthorizeCodeService.CodeInfo secondCode = mock(AuthorizeCodeService.CodeInfo.class);
        when(secondCode.getCode()).thenReturn(CODE);
        when(secondCode.getClientId()).thenReturn(CLIENT_ID);
        when(secondCode.getUserId()).thenReturn(USER_ID);
        when(secondCode.getScope()).thenReturn(SCOPE);
        when(secondCode.getExpiresAt()).thenReturn(LocalDateTime.of(2021, 7, 2, 0, 0));

        cut.getInMemoryCodes().add(codeInfo);
        cut.getInMemoryCodes().add(secondCode);

        cut.clearAllCodes();

        assertTrue(cut.getInMemoryCodes().isEmpty(), "All codes should be cleared");
    }

    @DisplayName("Clear all expired codes")
    @Test
    public void testClearExpiredCodes() {
        when(codeInfo.getExpiresAt()).thenReturn(LocalDateTime.of(2021, 7, 1, 0, 0));
        AuthorizeCodeService.CodeInfo secondCode = mock(AuthorizeCodeService.CodeInfo.class);
        when(secondCode.getCode()).thenReturn(CODE);
        when(secondCode.getClientId()).thenReturn(CLIENT_ID);
        when(secondCode.getUserId()).thenReturn(USER_ID);
        when(secondCode.getScope()).thenReturn(SCOPE);
        when(secondCode.getExpiresAt()).thenReturn(LocalDateTime.of(2021, 7, 2, 0, 0));

        cut.getInMemoryCodes().add(codeInfo);
        cut.getInMemoryCodes().add(secondCode);

        cut.clearExpiredCodes();

        assertFalse(cut.getInMemoryCodes().isEmpty(), "In memory codes should not be empty");
        assertTrue(cut.getInMemoryCodes().contains(secondCode), "The latest code should be still contained");
    }

    @DisplayName("Issue authorization code")
    @Test
    public void testIssue() {
        Optional<String> result = cut.issue(USER_ID, CLIENT_ID, SCOPE);

        assertNotNull(result, "There should be a result");
        assertTrue(result.isPresent(), "The result should not be empty");
        assertEquals("kni7Pv2VDykuminJ23vqHCAsgqn-92YEiK8lPMCl4aM=", result.get(), "Wrong generated authorization code");
    }
}
