package de.ma_vin.ape.users.security.jwt;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Header} is the class under test
 */
public class HeaderTest {
    private Header cut;

    @BeforeEach
    public void setUp() {
        cut = new Header("JWT", null, "HS256");
    }

    @DisplayName("Get the base64 url encoded token")
    @Test
    public void testGetJsonBase64UrlEncoded() {
        try {
            String result = cut.getJsonBase64UrlEncoded();
            assertNotNull(result, "There should be some result");
            assertEquals("eyJ0eXAiOiJKV1QiLCJjdHkiOm51bGwsImFsZyI6IkhTMjU2In0", result, "Wrong result");
        } catch (JwtGeneratingException e) {
            fail("Non expected JwtGeneratingException: " + e.getMessage());
        }
    }
}
