package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.AdminGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.CommonGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.dao.user.history.UserChangeDao;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.history.*;
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

public class UserChangeServiceTest {
    public static final Long USER_ID = 1L;
    public static final Long ADMIN_GROUP_ID = 1L;
    public static final Long COMMON_GROUP_ID = 2L;
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);
    public static final String ADMIN_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(ADMIN_GROUP_ID, AdminGroup.ID_PREFIX);
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String PRINCIPAL_IDENTIFICATION = "UAA00001";

    @Mock
    private AdminGroupChangeRepository adminGroupChangeRepository;
    @Mock
    private CommonGroupChangeRepository commonGroupChangeRepository;
    @Mock
    private BaseGroupChangeRepository baseGroupChangeRepository;
    @Mock
    private PrivilegeGroupChangeRepository privilegeGroupChangeRepository;
    @Mock
    private UserChangeRepository userChangeRepository;
    @Mock
    private UserDao userDao;
    @Mock
    private UserDao storedUserDao;
    @Mock
    private AdminGroupDao adminGroupDao;
    @Mock
    private CommonGroupDao commonGroupDao;


    private UserChangeService cut;
    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new UserChangeService();

        cut.setUserChangeRepository(userChangeRepository);
        cut.setAdminGroupChangeRepository(adminGroupChangeRepository);
        cut.setCommonGroupChangeRepository(commonGroupChangeRepository);
        cut.setBaseGroupChangeRepository(baseGroupChangeRepository);
        cut.setPrivilegeGroupChangeRepository(privilegeGroupChangeRepository);

        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(storedUserDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(adminGroupDao.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Save a new admin")
    @Test
    public void testSaveCreationAdmin() {
        when(userDao.getParentAdminGroup()).thenReturn(adminGroupDao);
        when(userChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.CREATE, ((UserChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for user change");
            assertNotNull(((UserChangeDao) a.getArgument(0)).getEditor(), "Missing editor for user change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for user change");
            return null;
        });
        when(adminGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.ADD, ((AdminGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for common group change");
            assertNotNull(((AdminGroupChangeDao) a.getArgument(0)).getEditor(), "Missing editor for common group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for common group change");
            assertNotNull(((AdminGroupChangeDao) a.getArgument(0)).getAdmin(), "Missing user for common group change");
            assertEquals(USER_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getAdmin().getIdentification(), "Wrong user for common group change");
            return null;
        });

        cut.saveCreation(userDao, PRINCIPAL_IDENTIFICATION);
        verify(userChangeRepository).save(any());
        verify(adminGroupChangeRepository).save(any());
        verify(commonGroupChangeRepository, never()).save(any());
    }

    @DisplayName("Save a new user")
    @Test
    public void testSaveCreationUser() {
        when(userDao.getParentCommonGroup()).thenReturn(commonGroupDao);
        when(userChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.CREATE, ((UserChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for user change");
            assertNotNull(((UserChangeDao) a.getArgument(0)).getEditor(), "Missing editor for user change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for user change");
            return null;
        });
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.ADD, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for common group change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing editor for common group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for common group change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getUser(), "Missing user for common group change");
            assertEquals(USER_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getUser().getIdentification(), "Wrong user for common group change");
            return null;
        });

        cut.saveCreation(userDao, PRINCIPAL_IDENTIFICATION);
        verify(userChangeRepository).save(any());
        verify(adminGroupChangeRepository, never()).save(any());
        verify(commonGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified user")
    @Test
    public void testSaveChange() {
        when(userDao.getParentCommonGroup()).thenReturn(commonGroupDao);
        when(userDao.getFirstName()).thenReturn("abc");
        when(storedUserDao.getFirstName()).thenReturn("123");
        when(userChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((UserChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((UserChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("FirstName: \"123\" -> \"abc\"", ((UserChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(userDao, storedUserDao, PRINCIPAL_IDENTIFICATION);
        verify(userChangeRepository).save(any());
    }

    @DisplayName("Save a modified user, change value to null")
    @Test
    public void testSaveChangeToNull() {
        when(userDao.getParentCommonGroup()).thenReturn(commonGroupDao);
        when(storedUserDao.getFirstName()).thenReturn("123");
        when(userChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((UserChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((UserChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("FirstName: \"123\" -> \"null\"", ((UserChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(userDao, storedUserDao, PRINCIPAL_IDENTIFICATION);
        verify(userChangeRepository).save(any());
    }

    @DisplayName("Save a modified user, change value from null")
    @Test
    public void testSaveChangeFromNull() {
        when(userDao.getParentCommonGroup()).thenReturn(commonGroupDao);
        when(userDao.getFirstName()).thenReturn("abc");
        when(userChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((UserChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((UserChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("FirstName: \"null\" -> \"abc\"", ((UserChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(userDao, storedUserDao, PRINCIPAL_IDENTIFICATION);
        verify(userChangeRepository).save(any());
    }

    @DisplayName("Save a modified user, but nothing changed")
    @Test
    public void testSaveChangeNoChanges() {
        when(userDao.getParentCommonGroup()).thenReturn(commonGroupDao);
        when(userDao.getFirstName()).thenReturn("abc");
        when(userChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.UNKNOWN, ((UserChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((UserChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNull(((UserChangeDao) a.getArgument(0)).getAction(), "The action should be null");
            return null;
        });

        cut.saveChange(userDao, userDao, PRINCIPAL_IDENTIFICATION);
        verify(userChangeRepository).save(any());
    }

    @DisplayName("Delete an admin")
    @Test
    public void testDeleteAdmin() {
        when(userDao.getParentAdminGroup()).thenReturn(adminGroupDao);
        when(userChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.DELETE, ((UserChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for user change");
            assertNotNull(((UserChangeDao) a.getArgument(0)).getEditor(), "Missing modifier for user change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for user change");
            assertEquals(USER_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information for user change");
            return null;
        });
        when(adminGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.REMOVE, ((AdminGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for user change");
            assertNotNull(((AdminGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier for user change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for user change");
            assertEquals(USER_IDENTIFICATION, ((AdminGroupChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information for user change");
            return null;
        });

        cut.delete(userDao, PRINCIPAL_IDENTIFICATION);

        verify(userChangeRepository).save(any());
        verify(adminGroupChangeRepository).save(any());
        verify(commonGroupChangeRepository, never()).save(any());
        verify(userChangeRepository).markedAsDeleted(any(UserDao.class), eq(USER_IDENTIFICATION));
        verify(adminGroupChangeRepository).markedAsDeleted(any(UserDao.class), eq(USER_IDENTIFICATION));
        verify(commonGroupChangeRepository).markedAsDeleted(any(UserDao.class), any());

        verify(adminGroupChangeRepository).markedEditorAsDeleted(any(), eq(USER_IDENTIFICATION));
        verify(commonGroupChangeRepository).markedEditorAsDeleted(any(), eq(USER_IDENTIFICATION));
        verify(baseGroupChangeRepository).markedEditorAsDeleted(any(), eq(USER_IDENTIFICATION));
        verify(privilegeGroupChangeRepository).markedEditorAsDeleted(any(), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Delete a user")
    @Test
    public void testDeleteUser() {
        when(userDao.getParentCommonGroup()).thenReturn(commonGroupDao);
        when(userChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.DELETE, ((UserChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for user change");
            assertNotNull(((UserChangeDao) a.getArgument(0)).getEditor(), "Missing modifier for user change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for user change");
            assertEquals(USER_IDENTIFICATION, ((UserChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information for user change");
            return null;
        });
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.REMOVE, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for user change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier for user change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for user change");
            assertEquals(USER_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information for user change");
            return null;
        });

        cut.delete(userDao, PRINCIPAL_IDENTIFICATION);

        verify(userChangeRepository).save(any());
        verify(adminGroupChangeRepository, never()).save(any());
        verify(commonGroupChangeRepository).save(any());
        verify(userChangeRepository).markedAsDeleted(any(UserDao.class), eq(USER_IDENTIFICATION));
        verify(adminGroupChangeRepository).markedAsDeleted(any(UserDao.class), any());
        verify(commonGroupChangeRepository).markedAsDeleted(any(UserDao.class), eq(USER_IDENTIFICATION));

        verify(adminGroupChangeRepository).markedEditorAsDeleted(any(), eq(USER_IDENTIFICATION));
        verify(commonGroupChangeRepository).markedEditorAsDeleted(any(), eq(USER_IDENTIFICATION));
        verify(baseGroupChangeRepository).markedEditorAsDeleted(any(), eq(USER_IDENTIFICATION));
        verify(privilegeGroupChangeRepository).markedEditorAsDeleted(any(), eq(USER_IDENTIFICATION));
    }
}
