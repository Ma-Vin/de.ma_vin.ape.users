package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.CommonGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.PrivilegeGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.group.history.PrivilegeGroupChange;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.history.CommonGroupChangeRepository;
import de.ma_vin.ape.users.persistence.history.PrivilegeGroupChangeRepository;
import de.ma_vin.ape.utils.generators.IdGenerator;
import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class PrivilegeGroupChangeServiceTest {
    public static final Long PRIVILEGE_GROUP_ID = 1L;
    public static final Long COMMON_GROUP_ID = 2L;
    public static final Long EDITOR_ID = 3L;
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String EDITOR_IDENTIFICATION = IdGenerator.generateIdentification(EDITOR_ID, User.ID_PREFIX);
    public static final String PRINCIPAL_IDENTIFICATION = "UAA00001";

    @Mock
    private CommonGroupChangeRepository commonGroupChangeRepository;
    @Mock
    private PrivilegeGroupChangeRepository privilegeGroupChangeRepository;
    @Mock
    private PrivilegeGroupDao privilegeGroupDao;
    @Mock
    private PrivilegeGroupDao storedPrivilegeGroupDao;
    @Mock
    private CommonGroupDao commonGroupDao;
    @Mock
    private UserDao editorDao;
    @Mock
    private PrivilegeGroupChangeDao privilegeGroupChangeDao;


    private PrivilegeGroupChangeService cut;
    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new PrivilegeGroupChangeService();

        cut.setPrivilegeGroupChangeRepository(privilegeGroupChangeRepository);
        cut.setCommonGroupChangeRepository(commonGroupChangeRepository);

        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupDao.getParentCommonGroup()).thenReturn(commonGroupDao);
        when(storedPrivilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(editorDao.getIdentification()).thenReturn(EDITOR_IDENTIFICATION);
        when(privilegeGroupChangeDao.getPrivilegeGroup()).thenReturn(privilegeGroupDao);

        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2022, 3, 21, 20, 31, 0));
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Save a new privilege group")
    @Test
    public void testSaveCreation() {
        when(privilegeGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.CREATE, ((PrivilegeGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for privilege group change");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor(), "Missing editor for privilege group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for privilege group change");
            return null;
        });
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.ADD, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for common group change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing editor for common group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for common group change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getPrivilegeGroup(), "Missing privilege group for common group change");
            assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getPrivilegeGroup().getIdentification(), "Wrong privilege group for common group change");
            return null;
        });

        cut.saveCreation(privilegeGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(privilegeGroupChangeRepository).save(any());
        verify(commonGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified privilege group")
    @Test
    public void testSaveChange() {
        when(privilegeGroupDao.getGroupName()).thenReturn("abc");
        when(storedPrivilegeGroupDao.getGroupName()).thenReturn("123");
        when(privilegeGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((PrivilegeGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"123\" -> \"abc\"", ((PrivilegeGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(privilegeGroupDao, storedPrivilegeGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(privilegeGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified privilege group, change value to null")
    @Test
    public void testSaveChangeToNull() {
        when(storedPrivilegeGroupDao.getGroupName()).thenReturn("123");
        when(privilegeGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((PrivilegeGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"123\" -> \"null\"", ((PrivilegeGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(privilegeGroupDao, storedPrivilegeGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(privilegeGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified privilege group, change value from null")
    @Test
    public void testSaveChangeFromNull() {
        when(privilegeGroupDao.getGroupName()).thenReturn("abc");
        when(privilegeGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((PrivilegeGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"null\" -> \"abc\"", ((PrivilegeGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(privilegeGroupDao, storedPrivilegeGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(privilegeGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified privilege group, but nothing changed")
    @Test
    public void testSaveChangeNoChanges() {
        when(privilegeGroupDao.getGroupName()).thenReturn("abc");
        when(privilegeGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.UNKNOWN, ((PrivilegeGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getAction(), "The action should be null");
            return null;
        });

        cut.saveChange(privilegeGroupDao, privilegeGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(privilegeGroupChangeRepository).save(any());
    }

    @DisplayName("Delete a privilege group")
    @Test
    public void testDelete() {
        when(privilegeGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.DELETE, ((PrivilegeGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for privilege group change");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier for privilege group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for privilege group change");
            assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information for privilege group change");
            return null;
        });
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.REMOVE, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for privilege group change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier for privilege group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for privilege group change");
            assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information for privilege group change");
            return null;
        });

        cut.delete(privilegeGroupDao, PRINCIPAL_IDENTIFICATION);

        verify(privilegeGroupChangeRepository).save(any());
        verify(commonGroupChangeRepository).save(any());
        verify(privilegeGroupChangeRepository).markedAsDeleted(any(PrivilegeGroupDao.class), eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(commonGroupChangeRepository).markedAsDeleted(any(PrivilegeGroupDao.class), eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("load changes")
    @Test
    public void testLoadChanges() {
        when(privilegeGroupChangeDao.getChangeType()).thenReturn(ChangeType.CREATE);
        when(privilegeGroupChangeDao.getChangeTime()).thenReturn(SystemProperties.getSystemDateTime());
        when(privilegeGroupChangeDao.getEditor()).thenReturn(editorDao);
        when(privilegeGroupChangeRepository.findByPrivilegeGroup(any())).then(a -> {
            assertEquals(PRIVILEGE_GROUP_ID, ((PrivilegeGroupDao) a.getArgument(0)).getId(), "Wrong id of privilege group");
            return Collections.singletonList(privilegeGroupChangeDao);
        });

        List<PrivilegeGroupChange> changes = cut.loadChanges(PRIVILEGE_GROUP_IDENTIFICATION);

        assertNotNull(changes, "There should be a list of changes");
        assertEquals(1, changes.size(), "Wrong number of changes");
        assertEquals(ChangeType.CREATE, changes.get(0).getChangeType(), "Wrong change type");
        assertEquals(SystemProperties.getSystemDateTime(), changes.get(0).getChangeTime(), "Wrong change time");
        assertNotNull(changes.get(0).getEditor(), "Missing editor");
        assertEquals(EDITOR_IDENTIFICATION, changes.get(0).getEditor().getIdentification(), "Wrong editor identification");

        verify(privilegeGroupChangeRepository).findByPrivilegeGroup(any());
    }
}
