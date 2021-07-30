package de.ma_vin.ape.users.security.jwt;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link Signature} is the class under test
 */
public class SignatureTest {
    private static final String SECRET = "SomeDummySecret";
    private static final String HEADER_BASE64_URL_ENCODED = "eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6IkhTMjU2In0";
    private static final String PAYLOAD_BASE64_URL_ENCODED = "eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9";
    private static final String EXPECTED_SIGNATURE_BASE64_URL_ENCODED = "doe9MklLVlTbn8HLnFWJWjBylOxgrGtQ9uD9IxQ0YYY";


    private AutoCloseable openMocks;
    private Signature cut;

    @Mock
    private Header header;
    @Mock
    private Payload payload;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);
        cut = new Signature(SECRET);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Get the base64 url encoded token from header and payload")
    @Test
    public void testGetJsonBase64UrlEncodedNonStatic() throws JwtGeneratingException {
        when(header.getAlg()).thenReturn("HS256");
        when(header.getJsonBase64UrlEncoded()).thenReturn(HEADER_BASE64_URL_ENCODED);
        when(payload.getJsonBase64UrlEncoded()).thenReturn(PAYLOAD_BASE64_URL_ENCODED);

        String result = cut.getJsonBase64UrlEncoded(header, payload);
        assertNotNull(result, "There should be any result");
        assertEquals(EXPECTED_SIGNATURE_BASE64_URL_ENCODED, result, "wrong result");
    }

    @DisplayName("Get the base64 url encoded token for HS256")
    @Test
    public void testGetJsonBase64UrlEncodedHS256() throws JwtGeneratingException {
        String result = Signature.getJsonBase64UrlEncoded(HEADER_BASE64_URL_ENCODED, PAYLOAD_BASE64_URL_ENCODED, SECRET, "HS256");
        assertNotNull(result, "There should be any result");
        assertEquals(EXPECTED_SIGNATURE_BASE64_URL_ENCODED, result, "wrong result");
    }

    @DisplayName("Get the base64 url encoded token is callable for all available algorithms")
    @Test
    public void testGetJsonBase64UrlEncodedForAllAlg() {
        Arrays.asList("MD5", "HS1", "HS224", "HS256", "HS384", "HS512", "HS512/224", "HS512/256")
                .forEach(alg -> {
                    try {
                        String result = Signature.getJsonBase64UrlEncoded(HEADER_BASE64_URL_ENCODED, PAYLOAD_BASE64_URL_ENCODED, SECRET, alg);
                        assertNotNull(result, "There should be any result");
                    } catch (JwtGeneratingException e) {
                        fail(String.format("Algorithm %s was not supported: %s", alg, e.getMessage()));
                    }
                });
    }
}
