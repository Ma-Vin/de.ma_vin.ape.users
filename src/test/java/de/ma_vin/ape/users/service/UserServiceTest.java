package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class UserServiceTest {
    public static final Long USER_ID = 1L;
    public static final Long COMMON_GROUP_ID = 2L;
    public static final Long ADMIN_GROUP_ID = 3L;
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String ADMIN_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(ADMIN_GROUP_ID, AdminGroup.ID_PREFIX);

    private UserService cut;
    private AutoCloseable openMocks;

    @Mock
    private UserRepository userRepository;
    @Mock
    private User user;
    @Mock
    private UserDao userDao;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new UserService();
        cut.setUserRepository(userRepository);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Delete user")
    @Test
    public void testDeleteUser() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);

        cut.delete(user);

        verify(userRepository).delete(any());
    }

    @DisplayName("Check existence of user")
    @Test
    public void testUserExits() {
        when(userRepository.existsById(eq(USER_ID))).thenReturn(Boolean.TRUE);

        assertTrue(cut.userExits(USER_IDENTIFICATION), "The result should be true");
        verify(userRepository).existsById(eq(USER_ID));
    }

    @DisplayName("Find non existing user")
    @Test
    public void testFindUserNonExisting() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        Optional<User> result = cut.findUser(USER_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).findById(eq(USER_ID));
    }

    @DisplayName("Find non existing user")
    @Test
    public void testFindUser() {
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(any())).thenReturn(Optional.of(userDao));

        Optional<User> result = cut.findUser(USER_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");

        verify(userRepository).findById(eq(USER_ID));
    }

    @DisplayName("Find all users at common group")
    @Test
    public void testFindAllCommonGroups() {
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findByParentCommonGroup(any())).thenReturn(Collections.singletonList(userDao));

        List<User> result = cut.findAllUsersAtCommonGroup(COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(USER_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(userRepository).findByParentCommonGroup(any());
    }

    @DisplayName("Find all users at admin group")
    @Test
    public void testFindAllAdminGroups() {
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findByParentAdminGroup(any())).thenReturn(Collections.singletonList(userDao));

        List<User> result = cut.findAllUsersAtAdminGroup(ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(USER_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(userRepository).findByParentAdminGroup(any());
    }

    @DisplayName("Save user with admin group parent")
    @Test
    public void testSaveAdminGroup() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentAdminGroup(eq(USER_ID))).thenReturn(Optional.of(ADMIN_GROUP_ID));
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());

        Optional<User> result = cut.save(user);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");

        verify(userRepository).getIdOfParentAdminGroup(eq(USER_ID));
        verify(userRepository).findById(eq(USER_ID));
        verify(userRepository).save(any());
    }

    @DisplayName("Save user with admin group parent")
    @Test
    public void testSaveCommonGroup() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(ADMIN_GROUP_ID));

        Optional<User> result = cut.save(user);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");

        verify(userRepository).getIdOfParentCommonGroup(eq(USER_ID));
        verify(userRepository).findById(eq(USER_ID));
        verify(userRepository).save(any());
    }

    @DisplayName("Save user without parent")
    @Test
    public void testSaveNonParent() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());

        Optional<User> result = cut.save(user);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).getIdOfParentCommonGroup(eq(USER_ID));
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Save user without identification")
    @Test
    public void testSaveNoIdentification() {
        when(user.getIdentification()).thenReturn(null);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(ADMIN_GROUP_ID));

        Optional<User> result = cut.save(user);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository, never()).getIdOfParentAdminGroup(any());
        verify(userRepository, never()).getIdOfParentCommonGroup(any());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Save new user at admin group")
    @Test
    public void testSaveAtAdminGroupNew() {
        when(user.getIdentification()).thenReturn(null);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Parent at save should not be null");
            assertEquals(ADMIN_GROUP_ID, ((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Wrong parent at save");
            ((UserDao) a.getArgument(0)).setId(USER_ID);
            return a.getArgument(0);
        });

        Optional<User> result = cut.saveAtAdminGroup(user, ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository, never()).findById(any());
        verify(userRepository).save(any());
    }

    @DisplayName("Save existing user at admin group")
    @Test
    public void testSaveAtAdminGroupExisting() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Parent at save should not be null");
            assertEquals(ADMIN_GROUP_ID, ((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });

        Optional<User> result = cut.saveAtAdminGroup(user, ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
    }

    @DisplayName("Save non existing user at admin group")
    @Test
    public void testSaveAtAdminGroupNonExisting() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.empty());
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Parent at save should not be null");
            assertEquals(ADMIN_GROUP_ID, ((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });

        Optional<User> result = cut.saveAtAdminGroup(user, ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).findById(any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Save new user at common group")
    @Test
    public void testSaveAtCommonGroupNew() {
        when(user.getIdentification()).thenReturn(null);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            ((UserDao) a.getArgument(0)).setId(USER_ID);
            return a.getArgument(0);
        });

        Optional<User> result = cut.saveAtCommonGroup(user, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository, never()).findById(any());
        verify(userRepository).save(any());
    }

    @DisplayName("Save existing user at common group")
    @Test
    public void testSaveAtCommonGroupExisting() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });

        Optional<User> result = cut.saveAtCommonGroup(user, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
    }

    @DisplayName("Save non existing user at common group")
    @Test
    public void testSaveAtCommonGroupNonExisting() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.empty());
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });

        Optional<User> result = cut.saveAtCommonGroup(user, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).findById(any());
        verify(userRepository, never()).save(any());
    }
}
