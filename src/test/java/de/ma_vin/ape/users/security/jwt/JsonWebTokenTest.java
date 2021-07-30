package de.ma_vin.ape.users.security.jwt;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link JsonWebToken} is the class under test
 */
public class JsonWebTokenTest {
    private static final String SECRET = "SomeDummySecret";
    public static final String VALID_TOKEN = String.format("%s.%s.%s"
            , "eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6IkhTMjU2In0"
            , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9"
            , "doe9MklLVlTbn8HLnFWJWjBylOxgrGtQ9uD9IxQ0YYY");

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
        assertTrue(JsonWebToken.verify(VALID_TOKEN, SECRET), "The token should be valid");
    }

    @DisplayName("Decode a empty token")
    @Test
    public void testDecodeEmptyToken() {
        assertTrue(JsonWebToken.decodeToken("", SECRET).isEmpty(), "The token should not be present");
    }

    @DisplayName("Decode a token which has the wrong number of elements")
    @Test
    public void testDecodeWrongNumberOfElements() {
        assertTrue(JsonWebToken.decodeToken(
                String.format("%s.%s"
                        , "eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6IkhTMjU2In0="
                        , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9")
                , SECRET).isEmpty()
                , "The token should not be present");

    }

    @DisplayName("Decode a token which has a not parsable header element")
    @Test
    public void testDecodeNotParsableHeader() {
        assertTrue(JsonWebToken.decodeToken(
                String.format("%s.%s.%s"
                        , "abc"
                        , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9"
                        , "u2411rtXkLeOnjpOoyceBBcRY3UPcxwCa-8JqBGaiKw=")
                , SECRET).isEmpty()
                , "The token should not be present");
    }

    @DisplayName("Decode a token, but an exception occurs")
    @Test
    public void testDecodeException() {
        assertTrue(
                JsonWebToken.decodeToken(
                        String.format("%s.%s.%s"
                                , "eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6ImFiYyJ9"
                                , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9"
                                , "ahjJKEXAJso3AeHbfxxM70BQg-cqIWu_mo9Lr9nSu_M=")
                        , SECRET).isEmpty()
                , "The token should not be present");
    }

    @DisplayName("Decode a valid token")
    @Test
    public void testDecodeValidToken() {
        Header expectedHeader = new Header("JWT", null, "HS256");
        Payload expectedPayload = new Payload("Me", "Me", null, LocalDateTime.of(2021, 7, 2, 0, 0)
                , null, LocalDateTime.of(2021, 7, 1, 0, 0), "abc");

        Optional<JsonWebToken> result = JsonWebToken.decodeToken(
                String.format("%s.%s.%s"
                        , "eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6IkhTMjU2In0"
                        , "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9"
                        , "doe9MklLVlTbn8HLnFWJWjBylOxgrGtQ9uD9IxQ0YYY")
                , SECRET);
        assertTrue(result.isPresent(), "The token should be present");
        assertEquals(expectedHeader, result.get().getHeader(), "Wrong Header");
        assertEquals(expectedPayload, result.get().getPayload(), "Wrong Payload");
    }
}
