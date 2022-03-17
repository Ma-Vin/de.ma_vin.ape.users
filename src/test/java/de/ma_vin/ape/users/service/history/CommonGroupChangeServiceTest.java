package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.CommonGroupChangeDao;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.persistence.history.CommonGroupChangeRepository;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class CommonGroupChangeServiceTest {
    public static final Long COMMON_GROUP_ID = 1L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String PRINCIPAL_IDENTIFICATION = "UAA00001";

    @Mock
    private CommonGroupChangeRepository commonGroupChangeRepository;
    @Mock
    private CommonGroupDao commonGroupDao;
    @Mock
    private CommonGroupDao storedCommonGroupDao;


    private CommonGroupChangeService cut;
    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new CommonGroupChangeService();

        cut.setCommonGroupChangeRepository(commonGroupChangeRepository);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(storedCommonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Save a new common group")
    @Test
    public void testSaveCreation() {
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.CREATE, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing editor");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            return null;
        });
        cut.saveCreation(commonGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(commonGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified common group")
    @Test
    public void testSaveChange() {
        when(commonGroupDao.getGroupName()).thenReturn("abc");
        when(storedCommonGroupDao.getGroupName()).thenReturn("123");
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"123\" -> \"abc\"", ((CommonGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(commonGroupDao, storedCommonGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(commonGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified common group, change value to null")
    @Test
    public void testSaveChangeToNull() {
        when(storedCommonGroupDao.getGroupName()).thenReturn("123");
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"123\" -> \"null\"", ((CommonGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(commonGroupDao, storedCommonGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(commonGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified common group, change value from null")
    @Test
    public void testSaveChangeFromNull() {
        when(commonGroupDao.getGroupName()).thenReturn("abc");
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"null\" -> \"abc\"", ((CommonGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(commonGroupDao, storedCommonGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(commonGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified common group, but nothing changed")
    @Test
    public void testSaveChangeNoChanges() {
        when(commonGroupDao.getGroupName()).thenReturn("abc");
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.UNKNOWN, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNull(((CommonGroupChangeDao) a.getArgument(0)).getAction(), "The action should be null");
            return null;
        });

        cut.saveChange(commonGroupDao, commonGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(commonGroupChangeRepository).save(any());
    }

    @DisplayName("Delete a common group")
    @Test
    public void testDelete() {
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.DELETE, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals(COMMON_GROUP_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information");
            return null;
        });

        cut.delete(commonGroupDao, PRINCIPAL_IDENTIFICATION);

        verify(commonGroupChangeRepository).save(any());
        verify(commonGroupChangeRepository).markedAsDeleted(any(CommonGroupDao.class), eq(COMMON_GROUP_IDENTIFICATION));
    }
}
