package de.ma_vin.ape.users.security.jwt;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Payload} is the class under test
 */
public class PayloadTest {

    private Payload cut;

    @BeforeEach
    public void setUp() {
        cut = new Payload("Me", "Me", null, LocalDateTime.of(2021,7,2,0,0), null
                , LocalDateTime.of(2021,7,1,0,0),"abc");
    }

    @DisplayName("Get the base64 url encoded token")
    @Test
    public void testGetJsonBase64UrlEncoded() {
        try {
            String result = cut.getJsonBase64UrlEncoded();
            assertNotNull(result, "There should be some result");
            assertEquals("eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOlsyMDIxLDcsMiwwLDBdLCJuYmYiOm51bGwsImlhdCI6WzIwMjEsNywxLDAsMF0sImp0aSI6ImFiYyJ9", result, "Wrong result");
        } catch (JwtGeneratingException e) {
            fail("Non expected JwtGeneratingException: " + e.getMessage());
        }
    }
}
