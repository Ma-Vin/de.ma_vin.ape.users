package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.AdminGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.history.AdminGroupChange;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.history.AdminGroupChangeRepository;
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

public class AdminGroupChangeServiceTest {
    public static final Long ADMIN_GROUP_ID = 1L;
    public static final Long EDITOR_ID = 2L;
    public static final String ADMIN_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(ADMIN_GROUP_ID, AdminGroup.ID_PREFIX);
    public static final String EDITOR_IDENTIFICATION = IdGenerator.generateIdentification(EDITOR_ID, User.ID_PREFIX);
    public static final String PRINCIPAL_IDENTIFICATION = "UAA00001";

    @Mock
    private AdminGroupChangeRepository adminGroupChangeRepository;
    @Mock
    private AdminGroupDao adminGroupDao;
    @Mock
    private AdminGroupDao storedAdminGroupDao;
    @Mock
    private AdminGroupChangeDao adminGroupChangeDao;
    @Mock
    private UserDao editorDao;


    private AdminGroupChangeService cut;
    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new AdminGroupChangeService();

        cut.setAdminGroupChangeRepository(adminGroupChangeRepository);
        when(adminGroupDao.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(storedAdminGroupDao.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(editorDao.getIdentification()).thenReturn(EDITOR_IDENTIFICATION);
        when(adminGroupChangeDao.getAdminGroup()).thenReturn(adminGroupDao);

        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2022, 3, 21, 20, 31, 0));
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Save a new admin group")
    @Test
    public void testSaveCreation() {
        when(adminGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.CREATE, ((AdminGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNull(((AdminGroupChangeDao) a.getArgument(0)).getEditor(), "Editor should be null");
            return null;
        });
        cut.saveCreation(adminGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(adminGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified admin group")
    @Test
    public void testSaveChange() {
        when(adminGroupDao.getGroupName()).thenReturn("abc");
        when(storedAdminGroupDao.getGroupName()).thenReturn("123");
        when(adminGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((AdminGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((AdminGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"123\" -> \"abc\"", ((AdminGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(adminGroupDao, storedAdminGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(adminGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified admin group, change value to null")
    @Test
    public void testSaveChangeToNull() {
        when(storedAdminGroupDao.getGroupName()).thenReturn("123");
        when(adminGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((AdminGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((AdminGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"123\" -> \"null\"", ((AdminGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(adminGroupDao, storedAdminGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(adminGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified admin group, change value from null")
    @Test
    public void testSaveChangeFromNull() {
        when(adminGroupDao.getGroupName()).thenReturn("abc");
        when(adminGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((AdminGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((AdminGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"null\" -> \"abc\"", ((AdminGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(adminGroupDao, storedAdminGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(adminGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified admin group, but nothing changed")
    @Test
    public void testSaveChangeNoChanges() {
        when(adminGroupDao.getGroupName()).thenReturn("abc");
        when(adminGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.UNKNOWN, ((AdminGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((AdminGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNull(((AdminGroupChangeDao) a.getArgument(0)).getAction(), "The action should be null");
            return null;
        });

        cut.saveChange(adminGroupDao, adminGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(adminGroupChangeRepository).save(any());
    }

    @DisplayName("Delete a admin group")
    @Test
    public void testDelete() {
        when(adminGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.DELETE, ((AdminGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((AdminGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals(ADMIN_GROUP_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information");
            return null;
        });

        cut.delete(adminGroupDao, PRINCIPAL_IDENTIFICATION);

        verify(adminGroupChangeRepository).save(any());
        verify(adminGroupChangeRepository).markedAsDeleted(any(AdminGroupDao.class), eq(ADMIN_GROUP_IDENTIFICATION));
    }

    @DisplayName("load changes")
    @Test
    public void testLoadChanges() {
        when(adminGroupChangeDao.getChangeType()).thenReturn(ChangeType.CREATE);
        when(adminGroupChangeDao.getChangeTime()).thenReturn(SystemProperties.getSystemDateTime());
        when(adminGroupChangeDao.getEditor()).thenReturn(editorDao);
        when(adminGroupChangeRepository.findByAdminGroup(any())).then(a -> {
            assertEquals(ADMIN_GROUP_ID, ((AdminGroupDao) a.getArgument(0)).getId(), "Wrong id of admin group");
            return Collections.singletonList(adminGroupChangeDao);
        });

        List<AdminGroupChange> changes = cut.loadChanges(ADMIN_GROUP_IDENTIFICATION);

        assertNotNull(changes, "There should be a list of changes");
        assertEquals(1, changes.size(), "Wrong number of changes");
        assertEquals(ChangeType.CREATE, changes.get(0).getChangeType(), "Wrong change type");
        assertEquals(SystemProperties.getSystemDateTime(), changes.get(0).getChangeTime(), "Wrong change time");
        assertNotNull(changes.get(0).getEditor(), "Missing editor");
        assertEquals(EDITOR_IDENTIFICATION, changes.get(0).getEditor().getIdentification(), "Wrong editor identification");

        verify(adminGroupChangeRepository).findByAdminGroup(any());
    }
}
