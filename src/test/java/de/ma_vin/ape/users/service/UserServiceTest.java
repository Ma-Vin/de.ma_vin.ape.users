package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.group.BaseGroupExt;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupToBaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupToUserDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.resource.UserResourceDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.resource.UserResource;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.*;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class UserServiceTest {
    public static final Long USER_ID = 1L;
    public static final Long COMMON_GROUP_ID = 2L;
    public static final Long ADMIN_GROUP_ID = 3L;
    public static final Long PRIVILEGE_GROUP_ID = 4L;
    public static final Long BASE_GROUP_ID = 5L;
    public static final Long USER_IMAGE_ID = 6L;
    public static final Long USER_SMALL_IMAGE_ID = 7L;
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);
    public static final String USER_PASSWORD = "1 Dummy Password!";
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String ADMIN_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(ADMIN_GROUP_ID, AdminGroup.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String USER_IMAGE_IDENTIFICATION = IdGenerator.generateIdentification(USER_IMAGE_ID, UserResource.ID_PREFIX);
    public static final String USER_SMALL_IMAGE_IDENTIFICATION = IdGenerator.generateIdentification(USER_SMALL_IMAGE_ID, UserResource.ID_PREFIX);

    private UserService cut;
    private AutoCloseable openMocks;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserResourceService userResourceService;
    @Mock
    private PrivilegeGroupRepository privilegeGroupRepository;
    @Mock
    private PrivilegeGroupToUserRepository privilegeGroupToUserRepository;
    @Mock
    private BaseGroupRepository baseGroupRepository;
    @Mock
    private BaseToBaseGroupRepository baseToBaseGroupRepository;
    @Mock
    private BaseGroupToUserRepository baseGroupToUserRepository;
    @Mock
    private BaseGroupService baseGroupService;
    @Mock
    private UserExt user;
    @Mock
    private UserResource image;
    @Mock
    private UserResource smallImage;
    @Mock
    private UserDao userDao;
    @Mock
    private UserResourceDao imageDao;
    @Mock
    private UserResourceDao smallImageDao;
    @Mock
    private PrivilegeGroupDao privilegeGroupDao;
    @Mock
    private BaseGroupDao baseGroupDao;
    @Mock
    private BaseGroupExt baseGroup;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new UserService();
        cut.setUserRepository(userRepository);
        cut.setPrivilegeGroupRepository(privilegeGroupRepository);
        cut.setPrivilegeGroupToUserRepository(privilegeGroupToUserRepository);
        cut.setBaseGroupRepository(baseGroupRepository);
        cut.setBaseToBaseGroupRepository(baseToBaseGroupRepository);
        cut.setBaseGroupToUserRepository(baseGroupToUserRepository);
        cut.setUserResourceService(userResourceService);
        cut.setBaseGroupService(baseGroupService);
        cut.setPasswordEncoder(passwordEncoder);

        initDefaultUserMock();
    }

    private void initDefaultUserMock() {
        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDao.getPassword()).thenReturn(USER_PASSWORD);

        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.getPassword()).thenReturn(USER_PASSWORD);

        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(userDao));
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Delete user")
    @Test
    public void testDeleteUser() {
        cut.delete(user);

        verify(userRepository).delete(any());
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
    }

    @DisplayName("Delete user with references")
    @Test
    public void testDeleteUserWithReferences() {
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        cut.delete(user);

        verify(userRepository).delete(any());
        verify(userResourceService, times(2)).delete(any(UserResource.class));
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

    @DisplayName("Find existing user")
    @Test
    public void testFindUser() {
        Optional<User> result = cut.findUser(USER_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");

        verify(userRepository).findById(eq(USER_ID));
    }

    @DisplayName("Find all users at common group")
    @Test
    public void testFindAllCommonGroups() {
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
        when(userRepository.findByParentAdminGroup(any())).thenReturn(Collections.singletonList(userDao));

        List<User> result = cut.findAllUsersAtAdminGroup(ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(USER_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(userRepository).findByParentAdminGroup(any());
    }

    @DisplayName("Find all direct users at base group")
    @Test
    public void testFindAllUsersAtBaseGroup() {
        Long otherUserId = USER_ID + 1L;
        String otherUserIdentification = IdGenerator.generateIdentification(otherUserId, User.ID_PREFIX);
        UserDao otherUserDao = mock(UserDao.class);
        User otherUser = mock(User.class);

        when(otherUserDao.getId()).thenReturn(otherUserId);
        when(otherUserDao.getIdentification()).thenReturn(otherUserIdentification);
        when(otherUser.getIdentification()).thenReturn(otherUserIdentification);

        BaseGroupToUserDao baseGroupToUserDao = mock(BaseGroupToUserDao.class);
        when(baseGroupToUserDao.getUser()).thenReturn(userDao).thenReturn(otherUserDao);
        when(baseGroupToUserRepository.findAllByBaseGroup(any())).thenReturn(Collections.singletonList(baseGroupToUserDao))
                .thenReturn(Collections.singletonList(baseGroupToUserDao));

        BaseGroupToBaseGroupDao baseGroupToBaseGroupDao = mock(BaseGroupToBaseGroupDao.class);
        when(baseGroupToBaseGroupDao.getSubBaseGroup()).thenReturn(baseGroupDao);
        when(baseToBaseGroupRepository.findAllByBaseGroup(any())).thenReturn(Collections.singletonList(baseGroupToBaseGroupDao))
                .thenReturn(Collections.emptyList());

        when(baseGroupService.findBaseGroupTree(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(baseGroup));
        when(baseGroup.getAllUsers()).thenReturn(Set.of(user, otherUser));

        List<User> result = cut.findAllUsersAtBaseGroup(BASE_GROUP_IDENTIFICATION, false);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of users at result");
        assertEquals(USER_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first user entry");

        verify(baseGroupToUserRepository).findAllByBaseGroup(any());
        verify(baseToBaseGroupRepository, never()).findAllByBaseGroup(any());
        verify(baseGroupService, never()).findBaseGroupTree(any());
    }

    @DisplayName("Find all users at base group")
    @Test
    public void testFindAllUsersAtBaseGroupDissolve() {
        Long otherUserId = USER_ID + 1L;
        String otherUserIdentification = IdGenerator.generateIdentification(otherUserId, User.ID_PREFIX);
        UserDao otherUserDao = mock(UserDao.class);
        User otherUser = mock(User.class);

        when(otherUserDao.getId()).thenReturn(otherUserId);
        when(otherUserDao.getIdentification()).thenReturn(otherUserIdentification);
        when(otherUser.getIdentification()).thenReturn(otherUserIdentification);

        BaseGroupToUserDao baseGroupToUserDao = mock(BaseGroupToUserDao.class);
        when(baseGroupToUserDao.getUser()).thenReturn(userDao).thenReturn(otherUserDao);
        when(baseGroupToUserRepository.findAllByBaseGroup(any())).thenReturn(Collections.singletonList(baseGroupToUserDao))
                .thenReturn(Collections.singletonList(baseGroupToUserDao));

        BaseGroupToBaseGroupDao baseGroupToBaseGroupDao = mock(BaseGroupToBaseGroupDao.class);
        when(baseGroupToBaseGroupDao.getSubBaseGroup()).thenReturn(baseGroupDao);
        when(baseToBaseGroupRepository.findAllByBaseGroup(any())).thenReturn(Collections.singletonList(baseGroupToBaseGroupDao))
                .thenReturn(Collections.emptyList());

        when(baseGroupService.findBaseGroupTree(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(baseGroup));
        when(baseGroup.getAllUsers()).thenReturn(Set.of(user, otherUser));

        List<User> result = cut.findAllUsersAtBaseGroup(BASE_GROUP_IDENTIFICATION, true);
        assertNotNull(result, "The result should not be null");
        assertEquals(2, result.size(), "Wrong number of users at result");
        assertTrue(result.stream().anyMatch(u -> u.getIdentification().equals(USER_IDENTIFICATION)), "The direct user is missing at result");
        assertTrue(result.stream().anyMatch(u -> u.getIdentification().equals(otherUserIdentification)), "The indirect user is missing at result");

        verify(baseGroupToUserRepository, never()).findAllByBaseGroup(any());
        verify(baseToBaseGroupRepository, never()).findAllByBaseGroup(any());
        verify(baseGroupService).findBaseGroupTree(any());
    }

    @DisplayName("Save user with admin group parent")
    @Test
    public void testSaveAdminGroup() {
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save user with admin group parent")
    @Test
    public void testSaveCommonGroup() {
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save user without parent")
    @Test
    public void testSaveNonParent() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());

        Optional<User> result = cut.save(user);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userRepository).getIdOfParentCommonGroup(eq(USER_ID));
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save user without identification")
    @Test
    public void testSaveNoIdentification() {
        when(user.getIdentification()).thenReturn(null);
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save new user at admin group")
    @Test
    public void testSaveAtAdminGroupNew() {
        when(user.getIdentification()).thenReturn(null);
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at admin group")
    @Test
    public void testSaveAtAdminGroupExisting() {
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at admin group with equal images")
    @Test
    public void testSaveAtAdminGroupWithEqualImages() {
        when(user.getImage()).thenReturn(image);
        when(user.getSmallImage()).thenReturn(smallImage);
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Parent at save should not be null");
            assertEquals(ADMIN_GROUP_ID, ((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });
        when(image.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImage.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(imageDao.getId()).thenReturn(USER_IMAGE_ID);
        when(smallImageDao.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(smallImageDao.getId()).thenReturn(USER_SMALL_IMAGE_ID);

        Optional<User> result = cut.saveAtAdminGroup(user, ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
        verify(userResourceService, never()).delete(eq(imageDao));
        verify(userResourceService, never()).delete(eq(smallImageDao));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at admin group with different images")
    @Test
    public void testSaveAtAdminGroupWithDifferentImages() {
        when(user.getImage()).thenReturn(image);
        when(user.getSmallImage()).thenReturn(smallImage);
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Parent at save should not be null");
            assertEquals(ADMIN_GROUP_ID, ((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });
        when(image.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImage.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getId()).thenReturn(USER_SMALL_IMAGE_ID);
        when(smallImageDao.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImageDao.getId()).thenReturn(USER_IMAGE_ID);

        Optional<User> result = cut.saveAtAdminGroup(user, ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
        verify(userResourceService).delete(eq(imageDao));
        verify(userResourceService).delete(eq(smallImageDao));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at admin group with new images")
    @Test
    public void testSaveAtAdminGroupWithNewImages() {
        when(user.getImage()).thenReturn(image);
        when(user.getSmallImage()).thenReturn(smallImage);
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Parent at save should not be null");
            assertEquals(ADMIN_GROUP_ID, ((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });
        when(image.getIdentification()).thenReturn(null);
        when(smallImage.getIdentification()).thenReturn(null);
        when(imageDao.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getId()).thenReturn(USER_SMALL_IMAGE_ID);
        when(smallImageDao.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImageDao.getId()).thenReturn(USER_IMAGE_ID);

        Optional<User> result = cut.saveAtAdminGroup(user, ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
        verify(userResourceService).delete(eq(imageDao));
        verify(userResourceService).delete(eq(smallImageDao));
        verify(userResourceService, times(2)).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at admin group with removed images")
    @Test
    public void testSaveAtAdminGroupWithRemovedImages() {
        when(user.getImage()).thenReturn(null);
        when(user.getSmallImage()).thenReturn(null);
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Parent at save should not be null");
            assertEquals(ADMIN_GROUP_ID, ((UserDao) a.getArgument(0)).getParentAdminGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });
        when(imageDao.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getId()).thenReturn(USER_SMALL_IMAGE_ID);
        when(smallImageDao.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImageDao.getId()).thenReturn(USER_IMAGE_ID);

        Optional<User> result = cut.saveAtAdminGroup(user, ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
        verify(userResourceService).delete(eq(imageDao));
        verify(userResourceService).delete(eq(smallImageDao));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save non existing user at admin group")
    @Test
    public void testSaveAtAdminGroupNonExisting() {
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save new user at common group")
    @Test
    public void testSaveAtCommonGroupNew() {
        when(user.getIdentification()).thenReturn(null);
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at common group")
    @Test
    public void testSaveAtCommonGroupExisting() {
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at common group with equal images")
    @Test
    public void testSaveAtCommonGroupWithEqualImages() {
        when(user.getImage()).thenReturn(image);
        when(user.getSmallImage()).thenReturn(smallImage);
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });
        when(image.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImage.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(imageDao.getId()).thenReturn(USER_IMAGE_ID);
        when(smallImageDao.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(smallImageDao.getId()).thenReturn(USER_SMALL_IMAGE_ID);

        Optional<User> result = cut.saveAtCommonGroup(user, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
        verify(userResourceService, never()).delete(eq(imageDao));
        verify(userResourceService, never()).delete(eq(smallImageDao));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at common group with different images")
    @Test
    public void testSaveAtCommonGroupWithDifferentImages() {
        when(user.getImage()).thenReturn(image);
        when(user.getSmallImage()).thenReturn(smallImage);
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });
        when(image.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImage.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getId()).thenReturn(USER_SMALL_IMAGE_ID);
        when(smallImageDao.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImageDao.getId()).thenReturn(USER_IMAGE_ID);

        Optional<User> result = cut.saveAtCommonGroup(user, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
        verify(userResourceService).delete(eq(imageDao));
        verify(userResourceService).delete(eq(smallImageDao));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at common group with new images")
    @Test
    public void testSaveAtCommonGroupWithNewImages() {
        when(user.getImage()).thenReturn(image);
        when(user.getSmallImage()).thenReturn(smallImage);
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });
        when(image.getIdentification()).thenReturn(null);
        when(smallImage.getIdentification()).thenReturn(null);
        when(imageDao.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getId()).thenReturn(USER_SMALL_IMAGE_ID);
        when(smallImageDao.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImageDao.getId()).thenReturn(USER_IMAGE_ID);

        Optional<User> result = cut.saveAtCommonGroup(user, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
        verify(userResourceService).delete(eq(imageDao));
        verify(userResourceService).delete(eq(smallImageDao));
        verify(userResourceService, times(2)).save(any(UserResourceDao.class));
    }

    @DisplayName("Save existing user at common group with removed images")
    @Test
    public void testSaveAtCommonGroupWithRemovedImages() {
        when(user.getImage()).thenReturn(null);
        when(user.getSmallImage()).thenReturn(null);
        when(userDao.getImage()).thenReturn(imageDao);
        when(userDao.getSmallImage()).thenReturn(smallImageDao);
        when(userRepository.save(any())).then(a -> {
            assertNotNull(((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((UserDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });
        when(imageDao.getIdentification()).thenReturn(USER_SMALL_IMAGE_IDENTIFICATION);
        when(imageDao.getId()).thenReturn(USER_SMALL_IMAGE_ID);
        when(smallImageDao.getIdentification()).thenReturn(USER_IMAGE_IDENTIFICATION);
        when(smallImageDao.getId()).thenReturn(USER_IMAGE_ID);

        Optional<User> result = cut.saveAtCommonGroup(user, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userRepository).findById(any());
        verify(userRepository).save(any());
        verify(userResourceService).delete(eq(imageDao));
        verify(userResourceService).delete(eq(smallImageDao));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Save non existing user at common group")
    @Test
    public void testSaveAtCommonGroupNonExisting() {
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
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Add user to privilege group")
    @Test
    public void testAddUserToPrivilegeGroup() {
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);

        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(privilegeGroupToUserRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION, Role.CONTRIBUTOR);

        assertTrue(added, "The user should be added to the privilege group");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(userRepository).findById(eq(USER_ID));
        verify(privilegeGroupToUserRepository).save(any());
        verify(userResourceService, never()).delete(any(UserResourceDao.class));
        verify(userResourceService, never()).save(any(UserResourceDao.class));
    }

    @DisplayName("Add user to non existing privilege group")
    @Test
    public void testAddUserToPrivilegeGroupMissingPrivilegeGroup() {
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.empty());
        when(privilegeGroupToUserRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION, Role.CONTRIBUTOR);

        assertFalse(added, "The user should not be added to the privilege group");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(userRepository, never()).findById(eq(USER_ID));
        verify(privilegeGroupToUserRepository, never()).save(any());
    }

    @DisplayName("Add non existing user to privilege group")
    @Test
    public void testAddUserToPrivilegeGroupMissingUser() {
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);

        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.empty());
        when(privilegeGroupToUserRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION, Role.CONTRIBUTOR);

        assertFalse(added, "The user should not be added to the privilege group");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(userRepository).findById(eq(USER_ID));
        verify(privilegeGroupToUserRepository, never()).save(any());
    }

    @DisplayName("Add user to privilege group without result at saving")
    @Test
    public void testAddUserToPrivilegeGroupNoSavingResult() {
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);

        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(privilegeGroupToUserRepository.save(any())).thenReturn(null);

        boolean added = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION, Role.CONTRIBUTOR);

        assertFalse(added, "The user should not be added to the privilege group");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(userRepository).findById(eq(USER_ID));
        verify(privilegeGroupToUserRepository).save(any());
    }

    @DisplayName("Add user to base group")
    @Test
    public void testAddUserToBaseGroup() {
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);

        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(baseGroupToUserRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addUserToBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertTrue(added, "The user should be added to the base group");

        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
        verify(userRepository).findById(eq(USER_ID));
        verify(baseGroupToUserRepository).save(any());
    }

    @DisplayName("Add user to non existing base group")
    @Test
    public void testAddUserToBaseGroupMissingBaseGroup() {
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.empty());
        when(baseGroupToUserRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addUserToBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertFalse(added, "The user should not be added to the base group");

        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
        verify(userRepository, never()).findById(eq(USER_ID));
        verify(baseGroupToUserRepository, never()).save(any());
    }

    @DisplayName("Add non existing user to base group")
    @Test
    public void testAddUserToBaseGroupMissingUser() {
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);

        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.empty());
        when(baseGroupToUserRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addUserToBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertFalse(added, "The user should not be added to the base group");

        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
        verify(userRepository).findById(eq(USER_ID));
        verify(baseGroupToUserRepository, never()).save(any());
    }

    @DisplayName("Add user to base group without result at saving")
    @Test
    public void testAddUserToBaseGroupNoSavingResult() {
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);

        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(baseGroupToUserRepository.save(any())).thenReturn(null);

        boolean added = cut.addUserToBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertFalse(added, "The user should not be added to the base group");

        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
        verify(userRepository).findById(eq(USER_ID));
        verify(baseGroupToUserRepository).save(any());
    }

    @DisplayName("Remove user from privilege group")
    @Test
    public void testRemoveUserFromPrivilegeGroup() {
        when(privilegeGroupToUserRepository.deleteByPrivilegeGroupAndUser(any(), any())).thenReturn(1L);

        boolean removed = cut.removeUserFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertTrue(removed, "The user should be removed from the privilege group");

        verify(privilegeGroupToUserRepository).deleteByPrivilegeGroupAndUser(any(), any());
    }

    @DisplayName("Remove user from privilege group, but not connection exists")
    @Test
    public void testRemoveUserFromPrivilegeGroupNonExisting() {
        when(privilegeGroupToUserRepository.deleteByPrivilegeGroupAndUser(any(), any())).thenReturn(0L);

        boolean removed = cut.removeUserFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertFalse(removed, "The user should not be removed from the privilege group");

        verify(privilegeGroupToUserRepository).deleteByPrivilegeGroupAndUser(any(), any());
    }

    @DisplayName("Remove user from privilege group, but non more than one connection exists")
    @Test
    public void testRemoveUserFromPrivilegeGroupNotUnique() {
        when(privilegeGroupToUserRepository.deleteByPrivilegeGroupAndUser(any(), any())).thenReturn(2L);

        boolean removed = cut.removeUserFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertFalse(removed, "The user should not be removed from the privilege group");

        verify(privilegeGroupToUserRepository).deleteByPrivilegeGroupAndUser(any(), any());
    }

    @DisplayName("Remove user from base group")
    @Test
    public void testRemoveUserFromBaseGroup() {
        when(baseGroupToUserRepository.deleteByBaseGroupAndUser(any(), any())).thenReturn(1L);

        boolean removed = cut.removeUserFromBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertTrue(removed, "The user should be removed from the base group");

        verify(baseGroupToUserRepository).deleteByBaseGroupAndUser(any(), any());
    }

    @DisplayName("Remove user from base group, but not connection exists")
    @Test
    public void testRemoveUserFromBaseGroupNonExisting() {
        when(baseGroupToUserRepository.deleteByBaseGroupAndUser(any(), any())).thenReturn(0L);

        boolean removed = cut.removeUserFromBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertFalse(removed, "The user should not be removed from the base group");

        verify(baseGroupToUserRepository).deleteByBaseGroupAndUser(any(), any());
    }

    @DisplayName("Remove user from base group, but non more than one connection exists")
    @Test
    public void testRemoveUserFromBaseGroupNotUnique() {
        when(baseGroupToUserRepository.deleteByBaseGroupAndUser(any(), any())).thenReturn(2L);

        boolean removed = cut.removeUserFromBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        assertFalse(removed, "The user should not be removed from the base group");

        verify(baseGroupToUserRepository).deleteByBaseGroupAndUser(any(), any());
    }

    @DisplayName("Set a password for an user")
    @Test
    public void testSetPassword() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq("ABCDabcd1_"), any())).thenReturn(Boolean.FALSE);
        assertTrue(cut.setPassword(USER_IDENTIFICATION, "ABCDabcd1_"), "The password should be set");

        verify(passwordEncoder).matches(eq("ABCDabcd1_"), any());
        verify(userRepository).save(any());
    }

    @DisplayName("Set a password for an user and old does not exists")
    @Test
    public void testSetPasswordOldNull() {
        when(userDao.getPassword()).thenReturn(null);
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq("ABCDabcd1_"), any())).thenReturn(Boolean.FALSE);
        assertTrue(cut.setPassword(USER_IDENTIFICATION, "ABCDabcd1_"), "The password should be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository).save(any());
    }

    @DisplayName("Set a password for an user, but same again")
    @Test
    public void testSetPasswordEqualsOldOne() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq(USER_PASSWORD), any())).thenReturn(Boolean.TRUE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, USER_PASSWORD), "The password should not be set");

        verify(passwordEncoder).matches(eq(USER_PASSWORD), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but to short")
    @Test
    public void testSetPasswordToShort() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq("ABCabcd1_"), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, "ABCabcd1_"), "The password should not be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but no upper character")
    @Test
    public void testSetPasswordNoUpperCharacter() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq("abcdabcd1_"), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, "abcdabcd1_"), "The password should not be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but no lower character")
    @Test
    public void testSetPasswordNoLowerCharacter() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq("ABCDABCD1_"), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, "ABCDABCD1_"), "The password should not be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but no number")
    @Test
    public void testSetPasswordNoNumber() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq("ABCDabcde_"), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, "ABCDabcde_"), "The password should not be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but no special sign")
    @Test
    public void testSetPasswordNoNSpecialSign() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq("ABCDabcd12"), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, "ABCDabcd12"), "The password should not be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but user not found")
    @Test
    public void testSetPasswordUserNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(eq("ABCDabcd1_"), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, "ABCDabcd1_"), "The password should not be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but user not saved")
    @Test
    public void testSetPasswordUserNotSaved() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(eq("ABCDabcd1_"), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, "ABCDabcd1_"), "The password should not be set");

        verify(passwordEncoder).matches(eq("ABCDabcd1_"), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but null")
    @Test
    public void testSetPasswordButNull() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(any(), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, null), "The password should not be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @DisplayName("Set a password for an user, but empty")
    @Test
    public void testSetPasswordButEmpty() {
        when(userRepository.save(any())).then(a -> a.getArgument(0));
        when(userRepository.getIdOfParentAdminGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(USER_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));
        when(passwordEncoder.matches(any(), any())).thenReturn(Boolean.FALSE);
        assertFalse(cut.setPassword(USER_IDENTIFICATION, ""), "The password should not be set");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }
}
