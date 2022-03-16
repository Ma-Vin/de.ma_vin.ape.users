package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class AbstractChangeServiceTest {
    public static final String VALUE_NAME = "CommonGroup";

    @Mock
    private CommonGroupDao commonGroupDao;
    @Mock
    private CommonGroupDao storedCommonGroupDao;

    AbstractChangeService cut;
    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new CommonGroupChangeService();
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("check IIdentifiableDao")
    @Test
    public void testCheckDifferentIdentification() {
        when(commonGroupDao.getIdentification()).thenReturn("abc");
        when(storedCommonGroupDao.getIdentification()).thenReturn("123");

        assertEquals("CommonGroup: \"123\" -> \"abc\"", cut.checkDifferentIdentification(VALUE_NAME, commonGroupDao, storedCommonGroupDao), "Wrong difference");
    }

    @DisplayName("check IIdentifiableDao, change value to null")
    @Test
    public void testCheckDifferentIdentificationToNull() {
        when(storedCommonGroupDao.getIdentification()).thenReturn("123");

        assertEquals("CommonGroup: \"123\" -> \"null\"", cut.checkDifferentIdentification(VALUE_NAME, null, storedCommonGroupDao), "Wrong difference");
    }

    @DisplayName("check IIdentifiableDao, change value from null")
    @Test
    public void testCheckDifferentIdentificationFromNull() {
        when(commonGroupDao.getIdentification()).thenReturn("abc");

        assertEquals("CommonGroup: \"null\" -> \"abc\"", cut.checkDifferentIdentification(VALUE_NAME, commonGroupDao, null), "Wrong difference");
    }

    @DisplayName("check IIdentifiableDao, but both null")
    @Test
    public void testCheckDifferentIdentificationNull() {
        assertNull(cut.checkDifferentIdentification(VALUE_NAME, null, null), "null values should return null");
    }
    @DisplayName("check IIdentifiableDao, wrong type")
    @Test
    public void testCheckDifferentIdentificationWrongType() {
        assertNull(cut.checkDifferentIdentification(VALUE_NAME, commonGroupDao, "anything"), "wrong stored type should return null");
        assertNull(cut.checkDifferentIdentification(VALUE_NAME, "anything", storedCommonGroupDao), "wrong updated type should return null");
    }
}
