package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.enums.GroupType;
import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.group.BaseGroupExt;
import de.ma_vin.ape.users.model.domain.group.PrivilegeGroupExt;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.service.BaseGroupService;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.users.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link UserPermissionService} is the class under test
 */
public class UserPermissionServiceTest {
    private final static String USER_IDENTIFICATION = "UAA0001";
    private final static String ADMIN_GROUP_IDENTIFICATION = "AGAA0001";
    private final static String COMMON_GROUP_IDENTIFICATION = "CGAA0001";
    private final static String BASE_GROUP_IDENTIFICATION = "BGAA0001";
    private final static String PRIVILEGE_GROUP_IDENTIFICATION = "PGAA0001";

    private UserPermissionService cut;
    private AutoCloseable openMocks;

    @Mock
    private UserService userService;
    @Mock
    private BaseGroupService baseGroupService;
    @Mock
    private PrivilegeGroupService privilegeGroupService;
    @Mock
    private UserExt user;
    @Mock
    private PrivilegeGroupExt privilegeGroup;
    @Mock
    private BaseGroupExt baseGroup;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new UserPermissionService();
        cut.setUserService(userService);
        cut.setBaseGroupService(baseGroupService);
        cut.setPrivilegeGroupService(privilegeGroupService);

        when(baseGroup.getCommonGroupId()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);

        when(privilegeGroup.getCommonGroupId()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);

        when(user.getCommonGroupId()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(user.isGlobalAdmin()).thenReturn(Boolean.FALSE);
        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(user.getRole()).thenReturn(Role.ADMIN);

        when(userService.findUser(eq(USER_IDENTIFICATION))).thenReturn(Optional.of(user));
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(baseGroup));
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Check if the username is a global admin")
    @Test
    public void testIsGlobalAdmin() {
        when(user.getCommonGroupId()).thenReturn(null);
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);

        assertTrue(cut.isGlobalAdmin(Optional.of(USER_IDENTIFICATION)), "The user should be a global admin");

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(user).isGlobalAdmin();
    }

    @DisplayName("Check if the username is a global admin, but is not")
    @Test
    public void testIsGlobalAdminButIsNot() {
        assertFalse(cut.isGlobalAdmin(Optional.of(USER_IDENTIFICATION)), "The user should not be a global admin");

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(user).isGlobalAdmin();
    }

    @DisplayName("Check if the username is a global admin, but is not an UserExt")
    @Test
    public void testIsGlobalAdminUsernameNotPresent() {
        when(user.getCommonGroupId()).thenReturn(null);
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);

        assertFalse(cut.isGlobalAdmin(Optional.empty()), "The user should be not a global admin");

        verify(userService, never()).findUser(eq(USER_IDENTIFICATION));
        verify(user, never()).isGlobalAdmin();
    }

    @DisplayName("Check if the username is a global admin, but user is not found")
    @Test
    public void testIsGlobalAdminUserNotFound() {
        when(user.getCommonGroupId()).thenReturn(null);
        when(user.isGlobalAdmin()).thenReturn(Boolean.TRUE);
        when(userService.findUser(any())).thenReturn(Optional.empty());

        assertFalse(cut.isGlobalAdmin(Optional.of(USER_IDENTIFICATION)), "The user should be not a global admin");

        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(user, never()).isGlobalAdmin();
    }

    @DisplayName("Check if the username is an admin")
    @Test
    public void testIsAdmin() {
        assertTrue(cut.isAdmin(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should be an admin");

        checkFindDefault();
    }

    @DisplayName("Check if the username is an admin, but is not")
    @Test
    public void testIsAdminButIsNot() {
        when(user.getRole()).thenReturn(Role.MANAGER);
        assertFalse(cut.isAdmin(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be an admin");

        checkFindDefault();
    }

    @DisplayName("Check if the username is an admin, but is not an UserExt")
    @Test
    public void testIsAdminUsernameNotPresent() {
        assertFalse(cut.isAdmin(Optional.empty(), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be an admin");

        checkFindNone();
    }

    @DisplayName("Check if the username is an admin, but user is not found")
    @Test
    public void testIsAdminUserNotFound() {
        when(userService.findUser(any())).thenReturn(Optional.empty());

        assertFalse(cut.isAdmin(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be an admin");

        checkFindDefault();
    }

    @DisplayName("Check if the username is an admin, but wrong common group as parent")
    @Test
    public void testIsAdminWrongCommonId() {
        assertFalse(cut.isAdmin(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION + "_1", GroupType.COMMON)
                , "The user should not be an admin");

        checkFindDefault();
    }

    @DisplayName("Check if the username is an admin, check from based on base group")
    @Test
    public void testIsAdminBaseGroup() {
        assertTrue(cut.isAdmin(Optional.of(USER_IDENTIFICATION), BASE_GROUP_IDENTIFICATION, GroupType.BASE)
                , "The user should be an admin");

        checkFindBaseGroup();
    }

    @DisplayName("Check if the username is an admin, check from based on base group, but not found")
    @Test
    public void testIsAdminBaseGroupButNotFound() {
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        assertFalse(cut.isAdmin(Optional.of(USER_IDENTIFICATION), BASE_GROUP_IDENTIFICATION, GroupType.BASE)
                , "The user should not be an admin");

        checkFindBaseGroup();
    }

    @DisplayName("Check if the username is an admin, check from based on privilege group")
    @Test
    public void testIsAdminPrivilegeGroup() {
        assertTrue(cut.isAdmin(Optional.of(USER_IDENTIFICATION), PRIVILEGE_GROUP_IDENTIFICATION, GroupType.PRIVILEGE)
                , "The user should be an admin");

        checkFindPrivilegeGroup();
    }

    @DisplayName("Check if the username is an admin, check from based on privilege group, but not found")
    @Test
    public void testIsAdminPrivilegeGroupButNotFound() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        assertFalse(cut.isAdmin(Optional.of(USER_IDENTIFICATION), PRIVILEGE_GROUP_IDENTIFICATION, GroupType.PRIVILEGE)
                , "The user should not be an admin");

        checkFindPrivilegeGroup();
    }

    @DisplayName("Check if the username is an admin, check from based on admin group")
    @Test
    public void testIsAdminAdminGroup() {
        assertFalse(cut.isAdmin(Optional.of(USER_IDENTIFICATION), ADMIN_GROUP_IDENTIFICATION, GroupType.ADMIN)
                , "The user should not be an admin");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a manager")
    @Test
    public void testIsManager() {
        assertTrue(cut.isManager(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should be a manager");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a manager, but is lower level")
    @Test
    public void testIsManagerButHasLowerLevel() {
        when(user.getRole()).thenReturn(Role.CONTRIBUTOR);
        assertFalse(cut.isManager(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a manager");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a manager, but is higher level")
    @Test
    public void testIsManagerButHasHigherLevel() {
        when(user.getRole()).thenReturn(Role.ADMIN);
        assertTrue(cut.isManager(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should be a manager");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a manager, but is not an UserExt")
    @Test
    public void testIsManagerUsernameNotPresent() {
        assertFalse(cut.isManager(Optional.empty(), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a manager");

        checkFindNone();
    }

    @DisplayName("Check if the username is a manager, but user is not found")
    @Test
    public void testIsManagerUserNotFound() {
        when(userService.findUser(any())).thenReturn(Optional.empty());

        assertFalse(cut.isManager(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a manager");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a manager, but wrong common group as parent")
    @Test
    public void testIsManagerWrongCommonId() {
        assertFalse(cut.isManager(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION + "_1", GroupType.COMMON)
                , "The user should not be a manager");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a manager, check from based on base group")
    @Test
    public void testIsManagerBaseGroup() {
        assertTrue(cut.isManager(Optional.of(USER_IDENTIFICATION), BASE_GROUP_IDENTIFICATION, GroupType.BASE)
                , "The user should be a manager");

        checkFindBaseGroup();
    }

    @DisplayName("Check if the username is a manager, check from based on base group, but not found")
    @Test
    public void testIsManagerBaseGroupButNotFound() {
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        assertFalse(cut.isManager(Optional.of(USER_IDENTIFICATION), BASE_GROUP_IDENTIFICATION, GroupType.BASE)
                , "The user should not be a manager");

        checkFindBaseGroup();
    }

    @DisplayName("Check if the username is a manager, check from based on privilege group")
    @Test
    public void testIsManagerPrivilegeGroup() {
        assertTrue(cut.isManager(Optional.of(USER_IDENTIFICATION), PRIVILEGE_GROUP_IDENTIFICATION, GroupType.PRIVILEGE)
                , "The user should be a manager");

        checkFindPrivilegeGroup();
    }

    @DisplayName("Check if the username is a manager, check from based on privilege group, but not found")
    @Test
    public void testIsManagerPrivilegeGroupButNotFound() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        assertFalse(cut.isManager(Optional.of(USER_IDENTIFICATION), PRIVILEGE_GROUP_IDENTIFICATION, GroupType.PRIVILEGE)
                , "The user should not be a manager");

        checkFindPrivilegeGroup();
    }

    @DisplayName("Check if the username is a manager, check from based on admin group")
    @Test
    public void testIsManagerAdminGroup() {
        assertFalse(cut.isManager(Optional.of(USER_IDENTIFICATION), ADMIN_GROUP_IDENTIFICATION, GroupType.ADMIN)
                , "The user should not be a manager");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a contributor")
    @Test
    public void testIsContributor() {
        assertTrue(cut.isContributor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should be a contributor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a contributor, but is lower level")
    @Test
    public void testIsContributorButHasLowerLevel() {
        when(user.getRole()).thenReturn(Role.VISITOR);
        assertFalse(cut.isContributor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a contributor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a contributor, but is higher level")
    @Test
    public void testIsContributorButHasHigherLevel() {
        when(user.getRole()).thenReturn(Role.MANAGER);
        assertTrue(cut.isContributor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should be a contributor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a contributor, but is not an UserExt")
    @Test
    public void testIsContributorUsernameNotPresent() {
        assertFalse(cut.isContributor(Optional.empty(), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a contributor");

        checkFindNone();
    }

    @DisplayName("Check if the username is a contributor, but user is not found")
    @Test
    public void testIsContributorUserNotFound() {
        when(userService.findUser(any())).thenReturn(Optional.empty());

        assertFalse(cut.isContributor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a contributor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a contributor, but wrong common group as parent")
    @Test
    public void testIsContributorWrongCommonId() {
        assertFalse(cut.isContributor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION + "_1", GroupType.COMMON)
                , "The user should not be a contributor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a contributor, check from based on base group")
    @Test
    public void testIsContributorBaseGroup() {
        assertTrue(cut.isContributor(Optional.of(USER_IDENTIFICATION), BASE_GROUP_IDENTIFICATION, GroupType.BASE)
                , "The user should be a contributor");

        checkFindBaseGroup();
    }

    @DisplayName("Check if the username is a contributor, check from based on base group, but not found")
    @Test
    public void testIsContributorBaseGroupButNotFound() {
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        assertFalse(cut.isContributor(Optional.of(USER_IDENTIFICATION), BASE_GROUP_IDENTIFICATION, GroupType.BASE)
                , "The user should not be a contributor");

        checkFindBaseGroup();
    }

    @DisplayName("Check if the username is a contributor, check from based on privilege group")
    @Test
    public void testIsContributorPrivilegeGroup() {
        assertTrue(cut.isContributor(Optional.of(USER_IDENTIFICATION), PRIVILEGE_GROUP_IDENTIFICATION, GroupType.PRIVILEGE)
                , "The user should be a contributor");

        checkFindPrivilegeGroup();
    }

    @DisplayName("Check if the username is a contributor, check from based on privilege group, but not found")
    @Test
    public void testIsContributorPrivilegeGroupButNotFound() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        assertFalse(cut.isContributor(Optional.of(USER_IDENTIFICATION), PRIVILEGE_GROUP_IDENTIFICATION, GroupType.PRIVILEGE)
                , "The user should not be a contributor");

        checkFindPrivilegeGroup();
    }

    @DisplayName("Check if the username is a contributor, check from based on admin group")
    @Test
    public void testIsContributorAdminGroup() {
        assertFalse(cut.isContributor(Optional.of(USER_IDENTIFICATION), ADMIN_GROUP_IDENTIFICATION, GroupType.ADMIN)
                , "The user should not be a contributor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a visitor")
    @Test
    public void testIsVisitor() {
        assertTrue(cut.isVisitor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should be a visitor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a visitor, but is lower level")
    @Test
    public void testIsVisitorButHasLowerLevel() {
        when(user.getRole()).thenReturn(Role.BLOCKED);
        assertFalse(cut.isVisitor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a visitor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a visitor, but is higher level")
    @Test
    public void testIsVisitorButHasHigherLevel() {
        when(user.getRole()).thenReturn(Role.CONTRIBUTOR);
        assertTrue(cut.isVisitor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should be a visitor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a visitor, but is not an UserExt")
    @Test
    public void testIsVisitorUsernameNotPresent() {
        assertFalse(cut.isVisitor(Optional.empty(), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a visitor");

        checkFindNone();
    }

    @DisplayName("Check if the username is a visitor, but user is not found")
    @Test
    public void testIsVisitorUserNotFound() {
        when(userService.findUser(any())).thenReturn(Optional.empty());

        assertFalse(cut.isVisitor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION, GroupType.COMMON)
                , "The user should not be a visitor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a visitor, but wrong common group as parent")
    @Test
    public void testIsVisitorWrongCommonId() {
        assertFalse(cut.isVisitor(Optional.of(USER_IDENTIFICATION), COMMON_GROUP_IDENTIFICATION + "_1", GroupType.COMMON)
                , "The user should not be a visitor");

        checkFindDefault();
    }

    @DisplayName("Check if the username is a visitor, check from based on base group")
    @Test
    public void testIsVisitorBaseGroup() {
        assertTrue(cut.isVisitor(Optional.of(USER_IDENTIFICATION), BASE_GROUP_IDENTIFICATION, GroupType.BASE)
                , "The user should be a visitor");

        checkFindBaseGroup();
    }

    @DisplayName("Check if the username is a visitor, check from based on base group, but not found")
    @Test
    public void testIsVisitorBaseGroupButNotFound() {
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        assertFalse(cut.isVisitor(Optional.of(USER_IDENTIFICATION), BASE_GROUP_IDENTIFICATION, GroupType.BASE)
                , "The user should not be a visitor");

        checkFindBaseGroup();
    }

    @DisplayName("Check if the username is a visitor, check from based on privilege group")
    @Test
    public void testIsVisitorPrivilegeGroup() {
        assertTrue(cut.isVisitor(Optional.of(USER_IDENTIFICATION), PRIVILEGE_GROUP_IDENTIFICATION, GroupType.PRIVILEGE)
                , "The user should be a visitor");

        checkFindPrivilegeGroup();
    }

    @DisplayName("Check if the username is a visitor, check from based on privilege group, but not found")
    @Test
    public void testIsVisitorPrivilegeGroupButNotFound() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        assertFalse(cut.isVisitor(Optional.of(USER_IDENTIFICATION), PRIVILEGE_GROUP_IDENTIFICATION, GroupType.PRIVILEGE)
                , "The user should not be a visitor");

        checkFindPrivilegeGroup();
    }

    @DisplayName("Check if the username is a visitor, check from based on admin group")
    @Test
    public void testIsVisitorAdminGroup() {
        assertFalse(cut.isVisitor(Optional.of(USER_IDENTIFICATION), ADMIN_GROUP_IDENTIFICATION, GroupType.ADMIN)
                , "The user should not be a visitor");

        checkFindDefault();
    }

    private void checkFindDefault() {
        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(baseGroupService, never()).findBaseGroup(any());
        verify(privilegeGroupService, never()).findPrivilegeGroup(any());
    }

    private void checkFindNone() {
        verify(userService, never()).findUser(eq(USER_IDENTIFICATION));
        verify(baseGroupService, never()).findBaseGroup(any());
        verify(privilegeGroupService, never()).findPrivilegeGroup(any());
    }

    private void checkFindBaseGroup() {
        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(baseGroupService).findBaseGroup(any());
        verify(privilegeGroupService, never()).findPrivilegeGroup(any());
    }

    private void checkFindPrivilegeGroup() {
        verify(userService).findUser(eq(USER_IDENTIFICATION));
        verify(baseGroupService, never()).findBaseGroup(any());
        verify(privilegeGroupService).findPrivilegeGroup(any());
    }
}
