package de.ma_vin.ape.users.security.jwt;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link JsonWebToken} is the class under test
 */
public class JsonWebTokenTest {
    private static final String SECRET = "SomeDummySecret";

    private AutoCloseable openMocks;
    private JsonWebToken cut;

    @Mock
    private Header header;
    @Mock
    private Payload payload;
    @Mock
    private Signature signature;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);
        cut = new JsonWebToken(header, payload, signature);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Get a encoded token")
    @Test
    public void testGetEncodedToken() throws JwtGeneratingException {
        when(header.getJsonBase64UrlEncoded()).thenReturn("1234");
        when(payload.getJsonBase64UrlEncoded()).thenReturn("5678");
        when(signature.getJsonBase64UrlEncoded(any(), any())).thenReturn("90");

        String result = cut.getEncodedToken();
        assertNotNull(result, "There should be any result");
        assertEquals("1234.5678.90", result, "Wrong encoded token");
    }

    @DisplayName("Verify a empty token")
    @Test
    public void testVerifyEmptyToken() {
        assertFalse(JsonWebToken.verify("", SECRET), "The token should not be valid");
    }

    @DisplayName("Verify a token which has the wrong number of elements")
    @Test
    public void testVerifyWrongNumberOfElements() {
        assertFalse(JsonWebToken.verify("eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6IkhTMjU2In0=.eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9", SECRET), "The token should not be valid");
        assertFalse(JsonWebToken.verify(
                String.format("%s.%s"
                        , "eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6IkhTMjU2In0="
                        , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9")
                , SECRET)
                , "The token should not be valid");

    }

    @DisplayName("Verify a token which has a not parsable header element")
    @Test
    public void testVerifyNotParsableHeader() {
        assertFalse(JsonWebToken.verify(
                String.format("%s.%s.%s"
                        , "abc"
                        , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9"
                        , "u2411rtXkLeOnjpOoyceBBcRY3UPcxwCa-8JqBGaiKw=")
                , SECRET)
                , "The token should not be valid");
    }

    @DisplayName("Verify a token, but an exception occurs")
    @Test
    public void testVerifyException() {
        assertFalse(
                JsonWebToken.verify(
                        String.format("%s.%s.%s"
                                , "eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6ImFiYyJ9"
                                , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9"
                                , "ahjJKEXAJso3AeHbfxxM70BQg-cqIWu_mo9Lr9nSu_M=")
                        , SECRET)
                , "The token should not be valid");
    }

    @DisplayName("Verify a valid token")
    @Test
    public void testVerifyValidToken() {
        assertTrue(
                JsonWebToken.verify(
                        String.format("%s.%s.%s"
                                , "eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6IkhTMjU2In0="
                                , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9"
                                , "u2411rtXkLeOnjpOoyceBBcRY3UPcxwCa-8JqBGaiKw=")
                        , SECRET)
                , "The token should be valid");
    }
}
