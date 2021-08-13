package de.ma_vin.ape.users.security.jwt;

import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Payload} is the class under test
 */
public class PayloadTest {
    private LocalDateTime referenceDateTime = LocalDateTime.of(2021, 8, 13, 21, 29, 10);
    private Long referenceDateTimeSeconds = Payload.getLocalDateTimeToLong(referenceDateTime);

    private Payload cut;

    @BeforeEach
    public void setUp() {
        SystemProperties.setZoneId("Europe/Berlin");
        Payload.reInitZoneId();

        cut = new Payload("Me", "Me", null, LocalDateTime.of(2021, 7, 2, 0, 0), null
                , LocalDateTime.of(2021, 7, 1, 0, 0), "abc");
    }

    @AfterEach
    public void tearDown() {
        SystemProperties.setZoneId((ZoneId) null);
    }


    @DisplayName("Get the base64 url encoded token")
    @Test
    public void testGetJsonBase64UrlEncoded() {
        try {
            String result = cut.getJsonBase64UrlEncoded();
            assertNotNull(result, "There should be some result");
            assertEquals("eyJpc3MiOiJNZSIsInN1YiI6Ik1lIiwiYXVkIjpudWxsLCJleHAiOjE2MjUxNzY4MDAsIm5iZiI6bnVsbCwiaWF0IjoxNjI1MDkwNDAwLCJqdGkiOiJhYmMiLCJ0aW1lWm9uZSI6IkV1cm9wZS9CZXJsaW4ifQ", result, "Wrong result");
        } catch (JwtGeneratingException e) {
            fail("Non expected JwtGeneratingException: " + e.getMessage());
        }
    }

    @DisplayName("Set expiration time as local date time")
    @Test
    public void testSetExpAsLocalDateTime() {
        cut.setExpAsLocalDateTime(referenceDateTime);

        assertNotNull(cut.getExp(), "There should be a exp");
        assertEquals(referenceDateTimeSeconds, cut.getExp(), "Wrong exp");
    }

    @DisplayName("Set null expiration time as local date time")
    @Test
    public void testSetExpAsLocalDateTimeNull() {
        cut.setExpAsLocalDateTime(null);

        assertNull(cut.getExp(), "There should not be any exp");
    }

    @DisplayName("Set not before as local date time")
    @Test
    public void testSetNbfAsLocalDateTime() {
        cut.setNbfAsLocalDateTime(referenceDateTime);

        assertNotNull(cut.getNbf(), "There should be a nbf");
        assertEquals(referenceDateTimeSeconds, cut.getNbf(), "Wrong nbf");
    }

    @DisplayName("Set null not before as local date time")
    @Test
    public void testSetNbfAsLocalDateTimeNull() {
        cut.setNbfAsLocalDateTime(null);

        assertNull(cut.getNbf(), "There should not be any nbf");
    }

    @DisplayName("Set issued at as local date time")
    @Test
    public void testSetIatAsLocalDateTime() {
        cut.setIatAsLocalDateTime(referenceDateTime);

        assertNotNull(cut.getIat(), "There should be a iat");
        assertEquals(referenceDateTimeSeconds, cut.getIat(), "Wrong iat");
    }

    @DisplayName("Set null issued at as local date time")
    @Test
    public void testSetIatAsLocalDateTimeNull() {
        cut.setIatAsLocalDateTime(null);

        assertNull(cut.getIat(), "There should not be any iat");
    }

    @DisplayName("Get expiration time as local date time")
    @Test
    public void testGetExpAsLocalDateTime() {
        cut.setExp(referenceDateTimeSeconds);

        LocalDateTime result = cut.getExpAsLocalDateTime();
        assertNotNull(result, "There should be a exp");
        assertEquals(referenceDateTime, result, "Wrong exp");
    }

    @DisplayName("Get null expiration time as local date time")
    @Test
    public void testGetExpAsLocalDateTimeNull() {
        cut.setExp(null);

        assertNull(cut.getExpAsLocalDateTime(), "There should not be any exp");
    }

    @DisplayName("Get not before as local date time")
    @Test
    public void testGetNbfAsLocalDateTime() {
        cut.setNbf(referenceDateTimeSeconds);

        LocalDateTime result = cut.getNbfAsLocalDateTime();
        assertNotNull(result, "There should be a nbf");
        assertEquals(referenceDateTime, result, "Wrong nbf");
    }

    @DisplayName("Get null not before as local date time")
    @Test
    public void testGetNbfAsLocalDateTimeNull() {
        cut.setNbf(null);

        assertNull(cut.getNbfAsLocalDateTime(), "There should not be any nbf");
    }

    @DisplayName("Get issued at as local date time")
    @Test
    public void testGetIatAsLocalDateTime() {
        cut.setIat(referenceDateTimeSeconds);

        LocalDateTime result = cut.getIatAsLocalDateTime();
        assertNotNull(result, "There should be a iat");
        assertEquals(referenceDateTime, result, "Wrong iat");
    }

    @DisplayName("Get null issued at as local date time")
    @Test
    public void testGetIatAsLocalDateTimeNull() {
        cut.setIat(null);

        assertNull(cut.getIatAsLocalDateTime(), "There should not be any iat");
    }
}
