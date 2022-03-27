package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.history.AdminGroupChange;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.domain.user.history.UserChange;
import de.ma_vin.ape.users.model.gen.dto.group.AdminGroupDto;
import de.ma_vin.ape.users.model.gen.dto.history.ChangeDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.users.model.gen.dto.user.part.UserPartDto;
import de.ma_vin.ape.users.service.AdminGroupService;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.users.service.history.AdminGroupChangeService;
import de.ma_vin.ape.users.service.history.UserChangeService;
import de.ma_vin.ape.utils.controller.response.Message;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import de.ma_vin.ape.utils.generators.IdGenerator;
import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link AdminController} is the class under test
 */
public class AdminControllerTest {
    public static final Long ADMIN_GROUP_ID = 1L;
    public static final Long USER_ID = 2L;
    public static final Long EDITOR_ID = 3L;
    public static final String ADMIN_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(ADMIN_GROUP_ID, AdminGroup.ID_PREFIX);
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);
    public static final String EDITOR_IDENTIFICATION = IdGenerator.generateIdentification(EDITOR_ID, User.ID_PREFIX);
    public static final String DEFAULT_USER_FIRST_NAME = "DummyFirstName";
    public static final String DEFAULT_USER_LAST_NAME = "DummyLastName";
    public static final String USER_PASSWORD = "1 Dummy Password!";
    public static final String PRINCIPAL_IDENTIFICATION = EDITOR_IDENTIFICATION;

    private AdminController cut;
    private AutoCloseable openMocks;

    @Mock
    private UserService userService;
    @Mock
    private AdminGroupService adminGroupService;
    @Mock
    private AdminGroupChangeService adminGroupChangeService;
    @Mock
    private UserChangeService userChangeService;
    @Mock
    private User user;
    @Mock
    private User editor;
    @Mock
    private UserDto userDto;
    @Mock
    private AdminGroup adminGroup;
    @Mock
    private AdminGroupDto adminGroupDto;
    @Mock
    private AdminGroupChange adminGroupChange;
    @Mock
    private UserChange userChange;
    @Mock
    private Principal principal;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2022, 3, 26, 20, 4, 0));

        cut = new AdminController();

        cut.setUserService(userService);
        cut.setAdminGroupService(adminGroupService);
        cut.setAdminGroupChangeService(adminGroupChangeService);
        cut.setUserChangeService(userChangeService);

        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.getFirstName()).thenReturn(DEFAULT_USER_FIRST_NAME);
        when(user.getLastName()).thenReturn(DEFAULT_USER_LAST_NAME);

        when(editor.getIdentification()).thenReturn(EDITOR_IDENTIFICATION);

        when(adminGroup.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);

        when(principal.getName()).thenReturn(PRINCIPAL_IDENTIFICATION);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Create an new global admin")
    @Test
    public void testCreateAdmin() {
        when(userService.saveAtAdminGroup(any(), eq(ADMIN_GROUP_IDENTIFICATION), eq(PRINCIPAL_IDENTIFICATION))).thenReturn(Optional.of(user));

        ResponseWrapper<UserDto> response = cut.createAdmin(principal, DEFAULT_USER_FIRST_NAME, DEFAULT_USER_LAST_NAME, ADMIN_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(USER_IDENTIFICATION, response.getResponse().getIdentification(), "Wrong identification at result");

        verify(userService).saveAtAdminGroup(any(), eq(ADMIN_GROUP_IDENTIFICATION), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Create an new global admin, but not saved")
    @Test
    public void testCreateAdminNotSaved() {
        when(userService.saveAtAdminGroup(any(), eq(ADMIN_GROUP_IDENTIFICATION), eq(PRINCIPAL_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<UserDto> response = cut.createAdmin(principal, DEFAULT_USER_FIRST_NAME, DEFAULT_USER_LAST_NAME, ADMIN_GROUP_IDENTIFICATION);

        checkError(response);

        verify(userService).saveAtAdminGroup(any(), eq(ADMIN_GROUP_IDENTIFICATION), eq(PRINCIPAL_IDENTIFICATION));
    }


    @DisplayName("Get an global admin")
    @Test
    public void testGetAdmin() {
        mockDefaultGetAdmin();

        ResponseWrapper<UserDto> response = cut.getAdmin(USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Get an non existing user")
    @Test
    public void testGetAdminNonExisting() {
        mockDefaultUpdateAdmin();
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<UserDto> response = cut.getAdmin(USER_IDENTIFICATION);

        checkError(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Get an non global admin")
    @Test
    public void testGetAdminNonGlobalAdmin() {
        mockDefaultGetAdmin();
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);

        ResponseWrapper<UserDto> response = cut.getAdmin(USER_IDENTIFICATION);

        checkError(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Set users password")
    @Test
    public void testSetAdminPassword() {
        when(userService.setPassword(eq(USER_IDENTIFICATION), eq(USER_PASSWORD), eq(Boolean.TRUE), eq(PRINCIPAL_IDENTIFICATION))).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.setAdminPassword(principal, USER_IDENTIFICATION, USER_PASSWORD);

        checkOk(response);

        assertTrue(response.getResponse(), "The result should be successful");
        verify(userService).setPassword(eq(USER_IDENTIFICATION), eq(USER_PASSWORD), eq(Boolean.TRUE), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Set users password, but failed")
    @Test
    public void testSetAdminPasswordFailed() {
        when(userService.setPassword(eq(USER_IDENTIFICATION), eq(USER_PASSWORD), eq(Boolean.TRUE), eq(PRINCIPAL_IDENTIFICATION))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.setAdminPassword(principal, USER_IDENTIFICATION, USER_PASSWORD);

        checkError(response);

        verify(userService).setPassword(eq(USER_IDENTIFICATION), eq(USER_PASSWORD), eq(Boolean.TRUE), eq(PRINCIPAL_IDENTIFICATION));
    }

    private void mockDefaultGetAdmin() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
    }

    @DisplayName("Update an global admin")
    @Test
    public void testUpdateAdmin() {
        mockDefaultUpdateAdmin();

        ResponseWrapper<UserDto> response = cut.updateAdmin(principal, userDto, USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(userService, never()).saveAtCommonGroup(any(), any(), any());
    }

    @DisplayName("Update an non global admin")
    @Test
    public void testUpdateAdminNonGlobal() {
        mockDefaultUpdateAdmin();

        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);

        ResponseWrapper<UserDto> response = cut.updateAdmin(principal, userDto, USER_IDENTIFICATION);

        checkError(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService, never()).save(any(), any());
        verify(userService, never()).saveAtCommonGroup(any(), any(), any());
    }

    @DisplayName("Update an non existing user")
    @Test
    public void testUpdateAdminNonExisting() {
        mockDefaultUpdateAdmin();

        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<UserDto> response = cut.updateAdmin(principal, userDto, USER_IDENTIFICATION);

        checkError(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService, never()).save(any(), any());
        verify(userService, never()).saveAtCommonGroup(any(), any(), any());
    }

    @DisplayName("Update an user without save return")
    @Test
    public void testUpdateAdminNoSaveReturn() {
        mockDefaultUpdateAdminWithoutSaving();

        when(userService.save(any(), any())).then(a -> Optional.empty());

        ResponseWrapper<UserDto> response = cut.updateAdmin(principal, userDto, USER_IDENTIFICATION);

        checkFatal(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(userService, never()).saveAtCommonGroup(any(), any(), any());
    }

    @DisplayName("Update an user with different identification as parameter")
    @Test
    public void testUpdateAdminDifferentIdentification() {
        mockDefaultUpdateAdminWithoutSaving();

        List<String> storedIdentification = new ArrayList<>();
        String otherIdentification = USER_IDENTIFICATION + "1";
        when(userService.save(any(), eq(PRINCIPAL_IDENTIFICATION))).then(a -> {
            storedIdentification.add(((User) a.getArgument(0)).getIdentification());
            return Optional.of(a.getArgument(0));
        });
        when(userService.findUser(eq(otherIdentification))).thenReturn(Optional.of(user));

        ResponseWrapper<UserDto> response = cut.updateAdmin(principal, userDto, otherIdentification);

        checkWarn(response, 1);

        assertEquals(1, storedIdentification.size(), "Wrong number of stored identifications");
        assertEquals(otherIdentification, storedIdentification.get(0), "Wrong stored identification");

        verify(userService).findUser(eq(otherIdentification));
        verify(userService, never()).findUser(eq(USER_IDENTIFICATION));
        verify(userService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(userService, never()).saveAtCommonGroup(any(), any(), any());
    }

    @DisplayName("Delete an user")
    @Test
    public void testDeleteAdmin() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
            when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(userService).delete(eq(user), eq(PRINCIPAL_IDENTIFICATION));

        ResponseWrapper<Boolean> response = cut.deleteAdmin(principal, USER_IDENTIFICATION);

        checkOk(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(userService).userExits(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Delete an non existing user")
    @Test
    public void testDeleteAdminNonExisting() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
        when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.deleteAdmin(principal, USER_IDENTIFICATION);

        checkWarn(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService, never()).delete(any(), any());
        verify(userService, never()).userExits(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Delete an non global admin")
    @Test
    public void testDeleteAdminNonGlobalAdmin() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.FALSE);
        doAnswer(a -> {
            when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
            when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(userService).delete(eq(user), eq(PRINCIPAL_IDENTIFICATION));

        ResponseWrapper<Boolean> response = cut.deleteAdmin(principal, USER_IDENTIFICATION);

        checkWarn(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService, never()).delete(any(), any());
        verify(userService, never()).userExits(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Delete an user but still existing afterwards")
    @Test
    public void testDeleteAdminExistingAfterDeletion() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(userService.userExits(eq(USER_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.empty());
            return null;
        }).when(userService).delete(eq(user), eq(PRINCIPAL_IDENTIFICATION));

        ResponseWrapper<Boolean> response = cut.deleteAdmin(principal, USER_IDENTIFICATION);

        checkFatal(response);

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(userService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(userService).userExits(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Count users at admin group")
    @Test
    public void testCountAdmins() {
        when(userService.countUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countAdmins(ADMIN_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of result elements");

        verify(userService).countUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get all users from admin group")
    @Test
    public void testGetAllAdmins() {
        mockDefaultGetAllAdmins();

        ResponseWrapper<List<UserDto>> response = cut.getAllAdmins(ADMIN_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService, never()).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all users from admin group with pages, but missing page")
    @Test
    public void testGetAllAdminsPageableMissingPage() {
        mockDefaultGetAllAdmins();

        ResponseWrapper<List<UserDto>> response = cut.getAllAdmins(ADMIN_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all users from admin group with pages, but missing size")
    @Test
    public void testGetAllAdminsPageableMissingSize() {
        mockDefaultGetAllAdmins();

        ResponseWrapper<List<UserDto>> response = cut.getAllAdmins(ADMIN_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all users from admin group with pages")
    @Test
    public void testGetAllAdminsPageable() {
        mockDefaultGetAllAdmins();

        ResponseWrapper<List<UserDto>> response = cut.getAllAdmins(ADMIN_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }


    @DisplayName("Get all user parts from admin group")
    @Test
    public void testGetAllAdminParts() {
        mockDefaultGetAllAdmins();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllAdminParts(ADMIN_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService, never()).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all user parts from admin group with pages, but missing page")
    @Test
    public void testGetAllAdminPartsPageableMissingPage() {
        mockDefaultGetAllAdmins();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllAdminParts(ADMIN_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all user parts from admin group with pages, but missing size")
    @Test
    public void testGetAllAdminPartsPageableMissingSize() {
        mockDefaultGetAllAdmins();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllAdminParts(ADMIN_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all user parts from admin group with pages")
    @Test
    public void testGetAllAdminPartsPageable() {
        mockDefaultGetAllAdmins();

        ResponseWrapper<List<UserPartDto>> response = cut.getAllAdminParts(ADMIN_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(USER_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(userService, never()).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultGetAllAdmins() {
        when(userService.findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(user));
        when(userService.findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION), any(), any())).thenReturn(Collections.singletonList(user));
    }

    private void mockDefaultUpdateAdmin() {
        mockDefaultUpdateAdminWithoutSaving();
        when(userService.save(any(), eq(PRINCIPAL_IDENTIFICATION))).then(a -> Optional.of(a.getArgument(0)));
    }

    private void mockDefaultUpdateAdminWithoutSaving() {
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userDto.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
    }


    @DisplayName("Get a admin group")
    @Test
    public void testGetAdminGroup() {
        when(adminGroupService.findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION))).thenReturn(Optional.of(adminGroup));

        ResponseWrapper<AdminGroupDto> response = cut.getAdminGroup(ADMIN_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(adminGroupService).findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a non existing admin group")
    @Test
    public void testGetAdminGroupNonExisting() {
        when(adminGroupService.findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<AdminGroupDto> response = cut.getAdminGroup(ADMIN_GROUP_IDENTIFICATION);

        checkError(response);

        verify(adminGroupService).findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
    }

    @DisplayName("Update a admin group")
    @Test
    public void testUpdateAdminGroup() {
        mockDefaultUpdateAdminGroup();

        ResponseWrapper<AdminGroupDto> response = cut.updateAdminGroup(principal, adminGroupDto, ADMIN_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(adminGroupService).findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(adminGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Update a non existing admin group")
    @Test
    public void testUpdateAdminGroupNonExisting() {
        mockDefaultUpdateAdminGroup();
        when(adminGroupService.findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<AdminGroupDto> response = cut.updateAdminGroup(principal, adminGroupDto, ADMIN_GROUP_IDENTIFICATION);

        checkError(response);

        verify(adminGroupService).findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(adminGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a admin group without save return")
    @Test
    public void testUpdateAdminGroupNoSaveReturn() {
        mockDefaultUpdateAdminGroupWithoutSaving();
        when(adminGroupService.save(any(), eq(PRINCIPAL_IDENTIFICATION))).then(a -> Optional.empty());

        ResponseWrapper<AdminGroupDto> response = cut.updateAdminGroup(principal, adminGroupDto, ADMIN_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(adminGroupService).findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(adminGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Update a admin group with different identification as parameter")
    @Test
    public void testUpdateAdminGroupDifferentIdentification() {
        List<String> storedIdentification = new ArrayList<>();
        String otherIdentification = ADMIN_GROUP_IDENTIFICATION + "1";
        when(adminGroup.getIdentification()).thenReturn(otherIdentification);
        when(adminGroupDto.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(adminGroupService.findAdminGroup(eq(otherIdentification))).thenReturn(Optional.of(adminGroup));
        when(adminGroupService.save(any(), eq(PRINCIPAL_IDENTIFICATION))).then(a -> {
            storedIdentification.add(((AdminGroup) a.getArgument(0)).getIdentification());
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<AdminGroupDto> response = cut.updateAdminGroup(principal, adminGroupDto, otherIdentification);


        checkWarn(response, 1);

        assertEquals(1, storedIdentification.size(), "Wrong number of stored identifications");
        assertEquals(otherIdentification, storedIdentification.get(0), "Wrong stored identification");

        verify(adminGroupService).findAdminGroup(eq(otherIdentification));
        verify(adminGroupService, never()).findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(adminGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    private void mockDefaultUpdateAdminGroupWithoutSaving() {
        when(adminGroupDto.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(adminGroupService.findAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION))).thenReturn(Optional.of(adminGroup));
    }

    private void mockDefaultUpdateAdminGroup() {
        mockDefaultUpdateAdminGroupWithoutSaving();
        when(adminGroupService.save(any(), any())).then(a -> Optional.of(a.getArgument(0)));
    }

    @DisplayName("Get history of an admin group")
    @Test
    public void testGetAdminGroupHistory() {
        when(adminGroupChangeService.loadChanges(eq(ADMIN_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(adminGroupChange));
        when(adminGroupChange.getAdminGroup()).thenReturn(adminGroup);
        when(adminGroupChange.getChangeType()).thenReturn(ChangeType.CREATE);
        when(adminGroupChange.getChangeTime()).thenReturn(SystemProperties.getSystemDateTime());
        when(adminGroupChange.getEditor()).thenReturn(editor);

        ResponseWrapper<List<ChangeDto>> response = cut.getAdminGroupHistory(ADMIN_GROUP_IDENTIFICATION);

        checkOk(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of changes");
        ChangeDto change = response.getResponse().get(0);
        assertEquals(ADMIN_GROUP_IDENTIFICATION, change.getSubjectIdentification(), "Wrong admin group id");
        assertEquals(ChangeType.CREATE, change.getChangeType(), "Wrong change typ");
        assertEquals(SystemProperties.getSystemDateTime(), change.getChangeTime(), "Wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, change.getEditor(), "Wrong editor id");

        verify(adminGroupChangeService).loadChanges(eq(ADMIN_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get empty history of an admin group")
    @Test
    public void testGetAdminGroupHistoryEmpty() {
        when(adminGroupChangeService.loadChanges(any())).thenReturn(Collections.emptyList());

        ResponseWrapper<List<ChangeDto>> response = cut.getAdminGroupHistory(ADMIN_GROUP_IDENTIFICATION);

        checkWarn(response);
        assertTrue(response.getMessages().stream()
                        .map(Message::getMessageText)
                        .anyMatch(String.format("No changes were found for admin group %s, but at least one creation should exist at history", ADMIN_GROUP_IDENTIFICATION)::equals)
                , "Missing warning message");

        verify(adminGroupChangeService).loadChanges(eq(ADMIN_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get history of an admin")
    @Test
    public void testGetAdminHistory() {
        when(userChangeService.loadChanges(eq(USER_IDENTIFICATION))).thenReturn(Collections.singletonList(userChange));
        when(userChange.getUser()).thenReturn(user);
        when(userChange.getChangeType()).thenReturn(ChangeType.CREATE);
        when(userChange.getChangeTime()).thenReturn(SystemProperties.getSystemDateTime());
        when(userChange.getEditor()).thenReturn(editor);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);

        ResponseWrapper<List<ChangeDto>> response = cut.getAdminHistory(USER_IDENTIFICATION);

        checkOk(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of changes");
        ChangeDto change = response.getResponse().get(0);
        assertEquals(USER_IDENTIFICATION, change.getSubjectIdentification(), "Wrong admin group id");
        assertEquals(ChangeType.CREATE, change.getChangeType(), "Wrong change typ");
        assertEquals(SystemProperties.getSystemDateTime(), change.getChangeTime(), "Wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, change.getEditor(), "Wrong editor id");

        verify(userChangeService).loadChanges(eq(USER_IDENTIFICATION));
        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Get history of an admin, but is not an admin")
    @Test
    public void testGetAdminHistoryNonAdmin() {
        when(userChangeService.loadChanges(eq(USER_IDENTIFICATION))).thenReturn(Collections.singletonList(userChange));
        when(userChange.getUser()).thenReturn(user);
        when(userChange.getChangeType()).thenReturn(ChangeType.CREATE);
        when(userChange.getChangeTime()).thenReturn(SystemProperties.getSystemDateTime());
        when(userChange.getEditor()).thenReturn(editor);
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);

        ResponseWrapper<List<ChangeDto>> response = cut.getAdminHistory(USER_IDENTIFICATION);

        checkError(response);
        assertTrue(response.getMessages().stream()
                        .map(Message::getMessageText)
                        .anyMatch(String.format("There cannot be any admin history for an non admin user %s", USER_IDENTIFICATION)::equals)
                , "Missing error message");

        verify(userChangeService, never()).loadChanges(eq(USER_IDENTIFICATION));
        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }

    @DisplayName("Get empty history of an admin")
    @Test
    public void testGetAdminHistoryEmpty() {
        when(userChangeService.loadChanges(any())).thenReturn(Collections.emptyList());
        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);


        ResponseWrapper<List<ChangeDto>> response = cut.getAdminHistory(USER_IDENTIFICATION);

        checkWarn(response);
        assertTrue(response.getMessages().stream()
                        .map(Message::getMessageText)
                        .anyMatch(String.format("No changes were found for admin %s, but at least one creation should exist at history", USER_IDENTIFICATION)::equals)
                , "Missing warning message");

        verify(userChangeService).loadChanges(eq(USER_IDENTIFICATION));
        verify(userService).findUser(eq(USER_IDENTIFICATION));
    }
}