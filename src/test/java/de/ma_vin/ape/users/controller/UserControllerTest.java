package de.ma_vin.ape.users.controller;

import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.*;
import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.checkFatal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.dto.group.UserRoleDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserControllerTest {

    public static final Long COMMON_GROUP_ID = 1L;
    public static final Long BASE_GROUP_ID = 2L;
    public static final Long PRIVILEGE_GROUP_ID = 3L;
    public static final Long USER_ID = 4L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, User.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);

    private AutoCloseable openMocks;

    private UserController cut;

    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private UserDto userDto;
    @Mock
    private UserRoleDto userRoleDto;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new UserController();
        cut.setUserService(userService);
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
        when(userDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.save(any())).then(a -> Optional.of(a.getArgument(0)));

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
        when(userRoleDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, userRoleDto);

        checkOk(response);

        verify(userService).addUserToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Add global admin to privilege group")
    @Test
    public void testAddUserToPrivilegeGroupGlobalAdmin() {
        when(userRoleDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, userRoleDto);

        checkError(response);

        verify(userService, never()).addUserToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Add non existing user to privilege group")
    @Test
    public void testAddUserToPrivilegeGroupNonExisting() {
        when(userRoleDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
        when(userService.addUserToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, userRoleDto);

        checkWarn(response);

        verify(userService).addUserToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(USER_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Add user to privilege group, but not successful")
    @Test
    public void testAddUserToPrivilegeGroupNotSuccessful() {
        when(userRoleDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.addUserToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.addUserToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, userRoleDto);

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
}
