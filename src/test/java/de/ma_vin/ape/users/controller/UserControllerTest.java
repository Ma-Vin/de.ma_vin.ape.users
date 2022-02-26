package de.ma_vin.ape.users.controller;

import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.*;
import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.checkFatal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.group.PrivilegeGroupExt;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.dto.group.UserIdRoleDto;
import de.ma_vin.ape.users.model.gen.dto.group.UserRoleDto;
import de.ma_vin.ape.users.model.gen.dto.group.part.UserRolePartDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.users.model.gen.dto.user.part.UserPartDto;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.*;

public class UserControllerTest {

    public static final Long COMMON_GROUP_ID = 1L;
    public static final Long BASE_GROUP_ID = 2L;
    public static final Long PRIVILEGE_GROUP_ID = 3L;
    public static final Long USER_ID = 4L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, User.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);
    public static final String USER_PASSWORD = "1 Dummy Password!";

    private AutoCloseable openMocks;

    private UserController cut;

    @Mock
    private UserService userService;
    @Mock
    private PrivilegeGroupService privilegeGroupService;
    @Mock
    private User user;
    @Mock
    private UserDto userDto;
    @Mock
    private UserIdRoleDto userIdRoleDto;
    @Mock
    private PrivilegeGroupExt privilegeGroupExt;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new UserController();
        cut.setUserService(userService);
        cut.setPrivilegeGroupService(privilegeGroupService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Create an user")
    @Test
    public void testCreateUser() {
        when(userService.saveAtCommonGroup(any(), any())).thenAnswer(a -> {
            ((User) a.getArgument(0)).setIdentification(USER_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<UserDto> response = cut.createUser("FirstName", "LastName", COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(userService).saveAtCommonGroup(any(), eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Create an user but without save result")
    @Test
    public void testCreateUserWithoutResult() {
        when(userService.saveAtCommonGroup(any(), any())).thenAnswer(a -> Optional.empty());

        ResponseWrapper<UserDto> response = cut.createUser("FirstName", "LastName", COMMON_GROUP_IDENTIFICATION);

        checkError(response);

        verify(userService).saveAtCommonGroup(any(), eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete an user")
    @Test
    public void testDeleteUser() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
            when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(userService).delete(eq(user));

        ResponseWrapper<Boolean> response = cut.deleteUser(USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService).delete(any());
        verify(userService).userExits(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Delete an non existing user")
    @Test
    public void testDeleteUserNonExisting() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
        when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.deleteUser(USER_IDENTIFICATION);

        checkWarn(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService, never()).delete(any());
        verify(userService, never()).userExits(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Delete an global admin")
    @Test
    public void testDeleteUserGlobalAdmin() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.FALSE);
        doAnswer(a -> {
            when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
            when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(userService).delete(eq(user));

        ResponseWrapper<Boolean> response = cut.deleteUser(USER_IDENTIFICATION);

        checkWarn(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService, never()).delete(any());
        verify(userService, never()).userExits(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Delete an user but still existing afterwards")
    @Test
    public void testDeleteUserExistingAfterDeletion() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
            return null;
        }).when(userService).delete(eq(user));

        ResponseWrapper<Boolean> response = cut.deleteUser(USER_IDENTIFICATION);

        checkFatal(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService).delete(any());
        verify(userService).userExits(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Get an user")
    @Test
    public void testGetUser() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));

        ResponseWrapper<UserDto> response = cut.getUser(USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Get an non existing user")
    @Test
    public void testGetUserNonExisting() {
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<UserDto> response = cut.getUser(USER_IDENTIFICATION);

        checkError(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Get an global admin")
    @Test
    public void testGetUserGlobalAdmin() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));

        ResponseWrapper<UserDto> response = cut.getUser(USER_IDENTIFICATION);

        checkError(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Update an user")
    @Test
    public void testUpdateUser() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(user.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(userDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userDto.getRole()).thenReturn(Role.ADMIN);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.save(any())).then(a -> {
            assertEquals(Role.CONTRIBUTOR, ((User) a.getArgument(0)).getRole(), "The role should be the stored one");
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<UserDto> response = cut.updateUser(userDto, USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService).save(any());
        verify(userService, never()).saveAtCommonGroup(any(), any());
    }

    @DisplayName("Update an global admin")
    @Test
    public void testUpdateUserGlobalAdmin() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.save(any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<UserDto> response = cut.updateUser(userDto, USER_IDENTIFICATION);

        checkError(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService, never()).save(any());
        verify(userService, never()).saveAtCommonGroup(any(), any());
    }

    @DisplayName("Update an non existing user")
    @Test
    public void testUpdateUserNonExisting() {
        when(userDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
        when(userService.save(any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<UserDto> response = cut.updateUser(userDto, USER_IDENTIFICATION);

        checkError(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService, never()).save(any());
        verify(userService, never()).saveAtCommonGroup(any(), any());
    }

    @DisplayName("Update an user without save return")
    @Test
    public void testUpdateUserNoSaveReturn() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.save(any())).then(a -> Optional.empty());

        ResponseWrapper<UserDto> response = cut.updateUser(userDto, USER_IDENTIFICATION);

        checkFatal(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService).save(any());
        verify(userService, never()).saveAtCommonGroup(any(), any());
    }

    @DisplayName("Update an user with different identification as parameter")
    @Test
    public void testUpdateUserDifferentIdentification() {
        List<String> storedIdentification = new ArrayList<>();
        String otherIdentification = USER_IDENTIFICATION + "1";
        when(user.getIdentification()).thenReturn(otherIdentification);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findUser(eq(otherIdentification))).thenReturn(Optional.of(user));
        when(userService.save(any())).then(a -> {
            storedIdentification.add(((User) a.getArgument(0)).getIdentification());
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<UserDto> response = cut.updateUser(userDto, otherIdentification);

        checkWarn(response, 1);

        assertEquals(1, storedIdentification.size(), "Wrong number of stored identifications");
        assertEquals(otherIdentification, storedIdentification.get(0), "Wrong stored identification");

        verify(userService).findUser(eq(otherIdentification));
        verify(userService, never()).findUser(eq(USER_IDENTIFICATION));
        verify(userService).save(any());
        verify(userService, never()).saveAtCommonGroup(any(), any());
    }

    @DisplayName("Add user to privilege group")
    @Test
    public void testAddUserToPrivilegeGroup() {
        when(userIdRoleDto.getUserIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userIdRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, userIdRoleDto);

        checkOk(response);

        verify(userService).addUserToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Add global admin to privilege group")
    @Test
    public void testAddUserToPrivilegeGroupGlobalAdmin() {
        when(userIdRoleDto.getUserIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userIdRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, userIdRoleDto);

        checkError(response);

        verify(userService, never()).addUserToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Add non existing user to privilege group")
    @Test
    public void testAddUserToPrivilegeGroupNonExisting() {
        when(userIdRoleDto.getUserIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userIdRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
        when(userService.addUserToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, userIdRoleDto);

        checkWarn(response);

        verify(userService).addUserToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Add user to privilege group, but not successful")
    @Test
    public void testAddUserToPrivilegeGroupNotSuccessful() {
        when(userIdRoleDto.getUserIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userIdRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, userIdRoleDto);

        checkWarn(response, 1);

        verify(userService).addUserToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Remove user from privilege group")
    @Test
    public void testRemoveUserFromPrivilegeGroup() {
        when(userService.removeUserFromPrivilegeGroup(any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.removeUserFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).removeUserFromPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Remove user from privilege group, but not successful")
    @Test
    public void testRemoveUserFromPrivilegeGroupNotSuccessful() {
        when(userService.removeUserFromPrivilegeGroup(any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.removeUserFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        checkWarn(response, 1);

        verify(userService).removeUserFromPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Add user to base group")
    @Test
    public void testAddUserToBaseGroup() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToBaseGroup(any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.addUserToBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).addUserToBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Add global admin to base group")
    @Test
    public void testAddUserToBaseGroupGlobalAdmin() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToBaseGroup(any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.addUserToBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        checkError(response);

        verify(userService, never()).addUserToBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Add non existing user to base group")
    @Test
    public void testAddUserToBaseGroupNonExisting() {
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
        when(userService.addUserToBaseGroup(any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.addUserToBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        checkWarn(response);

        verify(userService).addUserToBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Add user to base group, but not successful")
    @Test
    public void testAddUserToBaseGroupNotSuccessful() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToBaseGroup(any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.addUserToBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        checkWarn(response, 1);

        verify(userService).addUserToBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Remove user from base group")
    @Test
    public void testRemoveUserFromBaseGroup() {
        when(userService.removeUserFromBaseGroup(any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.removeUserFromBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).removeUserFromBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Remove user from base group, but not successful")
    @Test
    public void testRemoveUserFromBaseGroupNotSuccessful() {
        when(userService.removeUserFromBaseGroup(any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.removeUserFromBaseGroup(BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);

        checkWarn(response, 1);

        verify(userService).removeUserFromBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION));
    }

    @DisplayName("Count users at common group")
    @Test
    public void testCountUsers() {
        when(userService.countUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countUsers(COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of result elements");

        verify(userService).countUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get all users from common group")
    @Test
    public void testGetAllUsers() {
        mockDefaultGetAllUsers();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsers(COMMON_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService, never()).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all users from common group with pages, but missing page")
    @Test
    public void testGetAllUsersPageableMissingPage() {
        mockDefaultGetAllUsers();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsers(COMMON_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all users from common group with pages, but missing size")
    @Test
    public void testGetAllUsersPageableMissingSize() {
        mockDefaultGetAllUsers();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsers(COMMON_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all users from common group with pages")
    @Test
    public void testGetAllUsersPageable() {
        mockDefaultGetAllUsers();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsers(COMMON_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }


    @DisplayName("Get all user parts from common group")
    @Test
    public void testGetAllUserParts() {
        mockDefaultGetAllUsers();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserParts(COMMON_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService, never()).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all user parts from common group with pages, but missing page")
    @Test
    public void testGetAllUserPartsPageableMissingPage() {
        mockDefaultGetAllUsers();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserParts(COMMON_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all user parts from common group with pages, but missing size")
    @Test
    public void testGetAllUserPartsPageableMissingSize() {
        mockDefaultGetAllUsers();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserParts(COMMON_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all user parts from common group with pages")
    @Test
    public void testGetAllUserPartsPageable() {
        mockDefaultGetAllUsers();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserParts(COMMON_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultGetAllUsers() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(user));
        when(userService.findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION), any(), any())).thenReturn(Collections.singletonList(user));
    }

    @DisplayName("Count users at base group")
    @Test
    public void testCountUsersAtBaseGroup() {
        when(userService.countUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countUsersAtBaseGroup(BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of result elements");

        verify(userService).countUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get all users from base group")
    @Test
    public void testGetAllUsersFromBaseGroup() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsersFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.TRUE, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all users from base group with pages, but missing page")
    @Test
    public void testGetAllUsersFromBaseGroupPageableMissingPage() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsersFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.FALSE, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all users from base group with pages, but missing size")
    @Test
    public void testGetAllUsersFromBaseGroupPageableMissingSize() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsersFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.FALSE, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all users from base group with pages")
    @Test
    public void testGetAllUsersFromBaseGroupPageable() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsersFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.FALSE, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all users from base group with pages, but dissolving subgroups")
    @Test
    public void testGetAllUsersFromBaseGroupPageableDissolving() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAllUsersFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.TRUE, 2, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all user parts from base group")
    @Test
    public void testGetAllUsersPartFromBaseGroup() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserPartsFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.TRUE, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all user parts from base group with pages, but missing page")
    @Test
    public void testGetAllUserPartsFromBaseGroupPageableMissingPage() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserPartsFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.FALSE, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all user parts from base group with pages, but missing size")
    @Test
    public void testGetAllUserPartsFromBaseGroupPageableMissingSize() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserPartsFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.FALSE, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all user parts from base group with pages")
    @Test
    public void testGetAllUserPartsFromBaseGroupPageable() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserPartsFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.FALSE, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all user parts from base group with pages, but dissolving subgroups")
    @Test
    public void testGetAllUserPartsFromBaseGroupPageableDissolving() {
        mockDefaultGetAllUsersFromBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllUserPartsFromBaseGroup(BASE_GROUP_IDENTIFICATION, Boolean.TRUE, 2, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean());
        verify(userService).findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultGetAllUsersFromBaseGroup() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), anyBoolean())).thenReturn(Collections.singletonList(user));
        when(userService.findAllUsersAtBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), any())).thenReturn(Collections.singletonList(user));
    }

    @DisplayName("Count available users for base group")
    @Test
    public void testCountAvailableUsersForBaseGroup() {
        when(userService.countAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countAvailableUsersForBaseGroup(BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of result elements");

        verify(userService).countAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
    }


    @DisplayName("Get all available users for base group")
    @Test
    public void testGetAvailableUsersForBaseGroup() {
        mockDefaultGetAvailableUsersForBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAvailableUsersForBaseGroup(BASE_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(userService, never()).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all available users for base group with pages, but missing page")
    @Test
    public void testGetAvailableUsersForBaseGroupPageableMissingPage() {
        mockDefaultGetAvailableUsersForBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAvailableUsersForBaseGroup(BASE_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available users for base group with pages, but missing size")
    @Test
    public void testGetAvailableUsersForBaseGroupPageableMissingSize() {
        mockDefaultGetAvailableUsersForBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAvailableUsersForBaseGroup(BASE_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all available users for base group with pages")
    @Test
    public void testGetAvailableUsersForBaseGroupPageable() {
        mockDefaultGetAvailableUsersForBaseGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAvailableUsersForBaseGroup(BASE_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available user parts for base group")
    @Test
    public void testGetAvailableUserPartsForBaseGroup() {
        mockDefaultGetAvailableUsersForBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAvailableUserPartsForBaseGroup(BASE_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(userService, never()).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all available user parts for base group with pages, but missing page")
    @Test
    public void testGetAvailableUserPartsForBaseGroupPageableMissingPage() {
        mockDefaultGetAvailableUsersForBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAvailableUserPartsForBaseGroup(BASE_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available user parts for base group with pages, but missing size")
    @Test
    public void testGetAvailableUserPartsForBaseGroupPageableMissingSize() {
        mockDefaultGetAvailableUsersForBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAvailableUserPartsForBaseGroup(BASE_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all available user parts for base group with pages")
    @Test
    public void testGetAvailableUserPartsForBaseGroupPageable() {
        mockDefaultGetAvailableUsersForBaseGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAvailableUserPartsForBaseGroup(BASE_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultGetAvailableUsersForBaseGroup() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(user));
        when(userService.findAllAvailableUsersForBaseGroup(eq(BASE_GROUP_IDENTIFICATION), any(), any())).thenReturn(Collections.singletonList(user));
    }

    @DisplayName("Count users at privilege group")
    @Test
    public void testCountUsersAtPrivilegeGroup() {
        when(userService.countUsersAtPrivilegeGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Role.CONTRIBUTOR))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countUsersAtPrivilegeGroup(BASE_GROUP_IDENTIFICATION, Role.CONTRIBUTOR);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of result elements");

        verify(userService).countUsersAtPrivilegeGroup(eq(BASE_GROUP_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Get all users from privilege group")
    @Test
    public void testGetAllUsersFromPrivilegeGroup() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRoleDto>> response = cut.getAllUsersFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.FALSE, Role.ADMIN, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any(), any());
    }

    @DisplayName("Get all users from privilege group with pages, but missing page")
    @Test
    public void testGetAllUsersFromPrivilegeGroupPageableMissingPage() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRoleDto>> response = cut.getAllUsersFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.FALSE, Role.ADMIN, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all users from privilege group with pages, but missing size")
    @Test
    public void testGetAllUsersFromPrivilegeGroupPageableMissingSize() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRoleDto>> response = cut.getAllUsersFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.FALSE, Role.ADMIN, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all users from privilege group with pages")
    @Test
    public void testGetAllUsersFromPrivilegeGroupPageable() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRoleDto>> response = cut.getAllUsersFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.FALSE, Role.ADMIN, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all users from privilege group with pages, but dissolving subgroups")
    @Test
    public void testGetAllUsersFromPrivilegeGroupPageableDissolving() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRoleDto>> response = cut.getAllUsersFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.TRUE, Role.ADMIN, 2, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }


    @DisplayName("Get all user parts from privilege group")
    @Test
    public void testGetAllUserPartsFromPrivilegeGroup() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRolePartDto>> response = cut.getAllUserPartsFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.FALSE, Role.ADMIN, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any(), any());
    }

    @DisplayName("Get all user parts from privilege group with pages, but missing page")
    @Test
    public void testGetAllUserPartsFromPrivilegeGroupPageableMissingPage() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRolePartDto>> response = cut.getAllUserPartsFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.FALSE, Role.ADMIN, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all user parts from privilege group with pages, but missing size")
    @Test
    public void testGetAllUserPartsFromPrivilegeGroupPageableMissingSize() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRolePartDto>> response = cut.getAllUserPartsFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.FALSE, Role.ADMIN, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all user parts from privilege group with pages")
    @Test
    public void testGetAllUserPartsFromPrivilegeGroupPageable() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRolePartDto>> response = cut.getAllUserPartsFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.FALSE, Role.ADMIN, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all user parts from privilege group with pages, but dissolving subgroups")
    @Test
    public void testGetAllUserPartsFromPrivilegeGroupPageableDissolving() {
        mockDefaultGetAllUsersFromPrivilegeGroup();

        ResponseWrapper<List<UserRolePartDto>> response = cut.getAllUserPartsFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Boolean.TRUE, Role.ADMIN, 2, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getUser().getIdentification(), "Wrong identification at first entry");
        assertEquals(Role.ADMIN, response.getResponse().get(0).getRole(), "Wrong role at first entry");

        verify(userService, never()).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean());
        verify(userService).findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultGetAllUsersFromPrivilegeGroup() {
        Map<Role, List<User>> rolesAndUsers = new EnumMap<>(Role.class);
        rolesAndUsers.put(Role.ADMIN, Collections.singletonList(user));

        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), anyBoolean())).thenReturn(rolesAndUsers);
        when(userService.findAllUsersAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any(), any())).thenReturn(rolesAndUsers);
    }

    @DisplayName("Count available users for privilege group")
    @Test
    public void testCountAvailableUsersForPrivilegeGroup() {
        when(userService.countAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countAvailableUsersForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of result elements");

        verify(userService).countAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get all available users for privilege group")
    @Test
    public void testGetAvailableUsersForPrivilegeGroup() {
        mockDefaultGetAvailableUsersForPrivilegeGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAvailableUsersForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(userService, never()).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all available users for privilege group with pages, but missing page")
    @Test
    public void testGetAvailableUsersForPrivilegeGroupPageableMissingPage() {
        mockDefaultGetAvailableUsersForPrivilegeGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAvailableUsersForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available users for privilege group with pages, but missing size")
    @Test
    public void testGetAvailableUsersForPrivilegeGroupPageableMissingSize() {
        mockDefaultGetAvailableUsersForPrivilegeGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAvailableUsersForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all available users for privilege group with pages")
    @Test
    public void testGetAvailableUsersForPrivilegeGroupPageable() {
        mockDefaultGetAvailableUsersForPrivilegeGroup();

        ResponseWrapper<List<UserDto>> response = cut.getAvailableUsersForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available user parts for privilege group")
    @Test
    public void testGetAvailableUserPartsForPrivilegeGroup() {
        mockDefaultGetAvailableUsersForPrivilegeGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAvailableUserPartsForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(userService, never()).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all available user parts for privilege group with pages, but missing page")
    @Test
    public void testGetAvailableUserPartsForPrivilegeGroupPageableMissingPage() {
        mockDefaultGetAvailableUsersForPrivilegeGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAvailableUserPartsForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available user parts for privilege group with pages, but missing size")
    @Test
    public void testGetAvailableUserPartsForPrivilegeGroupPageableMissingSize() {
        mockDefaultGetAvailableUsersForPrivilegeGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAvailableUserPartsForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all available user parts for privilege group with pages")
    @Test
    public void testGetAvailableUserPartsForPrivilegeGroupPageable() {
        mockDefaultGetAvailableUsersForPrivilegeGroup();

        ResponseWrapper<List<UserPartDto>> response = cut.getAvailableUserPartsForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(userService).findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultGetAvailableUsersForPrivilegeGroup() {
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(user));
        when(userService.findAllAvailableUsersForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any())).thenReturn(Collections.singletonList(user));
    }

    @DisplayName("Set users password")
    @Test
    public void testSetUserPassword() {
        when(userService.setPassword(eq(USER_IDENTIFICATION), eq(USER_PASSWORD), eq(Boolean.FALSE))).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.setUserPassword(USER_IDENTIFICATION, USER_PASSWORD);

        checkOk(response);

        assertTrue(response.getResponse(), "The result should be successful");
        verify(userService).setPassword(eq(USER_IDENTIFICATION), eq(USER_PASSWORD), eq(Boolean.FALSE));
    }

    @DisplayName("Set users password, but failed")
    @Test
    public void testSetUserPasswordFailed() {
        when(userService.setPassword(eq(USER_IDENTIFICATION), eq(USER_PASSWORD), eq(Boolean.FALSE))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.setUserPassword(USER_IDENTIFICATION, USER_PASSWORD);

        checkError(response);

        verify(userService).setPassword(eq(USER_IDENTIFICATION), eq(USER_PASSWORD), eq(Boolean.FALSE));
    }

    @DisplayName("Set users role")
    @Test
    public void testSetUserRole() {
        when(userService.setRole(eq(USER_IDENTIFICATION), eq(Role.ADMIN))).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.setUserRole(USER_IDENTIFICATION, Role.ADMIN);

        checkOk(response);

        assertTrue(response.getResponse(), "The result should be successful");
        verify(userService).setRole(eq(USER_IDENTIFICATION), eq(Role.ADMIN));
    }

    @DisplayName("Set users role, but failed")
    @Test
    public void testSetUserRoleFailed() {
        when(userService.setPassword(eq(USER_IDENTIFICATION), any(), eq(Boolean.FALSE))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.setUserRole(USER_IDENTIFICATION, Role.ADMIN);

        checkError(response);

        verify(userService).setRole(eq(USER_IDENTIFICATION), eq(Role.ADMIN));
    }
}
