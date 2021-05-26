package de.ma_vin.ape.users.model.domain.group;

import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class PrivilegeGroupExtTest {
    private PrivilegeGroupExt cut;
    private AutoCloseable openMocks;

    @Mock
    private UserExt directAdminUser;
    @Mock
    private UserExt indirectAdminUser;
    @Mock
    private UserExt directManagerUser;
    @Mock
    private UserExt indirectManagerUser;
    @Mock
    private UserExt directContributorUser;
    @Mock
    private UserExt indirectContributorUser;
    @Mock
    private UserExt directVisitorUser;
    @Mock
    private UserExt indirectVisitorUser;
    @Mock
    private UserExt directBlockedUser;
    @Mock
    private UserExt indirectBlockedUser;

    @Mock
    private BaseGroupExt adminGroup;
    @Mock
    private BaseGroupExt managerGroup;
    @Mock
    private BaseGroupExt contributorGroup;
    @Mock
    private BaseGroupExt visitorGroup;
    @Mock
    private BaseGroupExt blockedGroup;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new PrivilegeGroupExt();
        cut.addAdmins(directAdminUser);
        cut.addManagers(directManagerUser);
        cut.addContributors(directContributorUser);
        cut.addVisitors(directVisitorUser);
        cut.addBlocks(directBlockedUser);
        cut.addAdminGroups(adminGroup);
        cut.addManagerGroups(managerGroup);
        cut.addContributorGroups(contributorGroup);
        cut.addVisitorGroups(visitorGroup);
        cut.addBlockGroups(blockedGroup);

        initUserMocks();
        initBaseGroupMocks();
    }

    private void initUserMocks() {
        when(directAdminUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(1L, UserExt.ID_PREFIX));
        when(indirectAdminUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(2L, UserExt.ID_PREFIX));
        when(directManagerUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(3L, UserExt.ID_PREFIX));
        when(indirectManagerUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(4L, UserExt.ID_PREFIX));
        when(directContributorUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(5L, UserExt.ID_PREFIX));
        when(indirectContributorUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(6L, UserExt.ID_PREFIX));
        when(directVisitorUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(7L, UserExt.ID_PREFIX));
        when(indirectVisitorUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(8L, UserExt.ID_PREFIX));
        when(directBlockedUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(9L, UserExt.ID_PREFIX));
        when(indirectBlockedUser.getIdentification()).thenReturn(IdGenerator.generateIdentification(10L, UserExt.ID_PREFIX));
    }

    private void initBaseGroupMocks() {
        when(adminGroup.getIdentification()).thenReturn(IdGenerator.generateIdentification(1L, BaseGroupExt.ID_PREFIX));
        when(managerGroup.getIdentification()).thenReturn(IdGenerator.generateIdentification(2L, BaseGroupExt.ID_PREFIX));
        when(contributorGroup.getIdentification()).thenReturn(IdGenerator.generateIdentification(3L, BaseGroupExt.ID_PREFIX));
        when(visitorGroup.getIdentification()).thenReturn(IdGenerator.generateIdentification(4L, BaseGroupExt.ID_PREFIX));
        when(blockedGroup.getIdentification()).thenReturn(IdGenerator.generateIdentification(5L, BaseGroupExt.ID_PREFIX));

        when(adminGroup.getAllUsers()).thenReturn(Collections.singleton(indirectAdminUser));
        when(managerGroup.getAllUsers()).thenReturn(Collections.singleton(indirectManagerUser));
        when(contributorGroup.getAllUsers()).thenReturn(Collections.singleton(indirectContributorUser));
        when(visitorGroup.getAllUsers()).thenReturn(Collections.singleton(indirectVisitorUser));
        when(blockedGroup.getAllUsers()).thenReturn(Collections.singleton(indirectBlockedUser));
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Get all admins at privilege group")
    @Test
    public void testGetAllAdmins() {
        Set<User> result = cut.getAllAdmins();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directAdminUser), "The direct admin should be contained");
        assertTrue(result.contains(indirectAdminUser), "The indirect admin should be contained");
    }

    @DisplayName("Get all managers at privilege group")
    @Test
    public void testGetAllManagers() {
        Set<User> result = cut.getAllManagers();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directManagerUser), "The direct manager should be contained");
        assertTrue(result.contains(indirectManagerUser), "The indirect manager should be contained");
    }

    @DisplayName("Get all managers at privilege group with higher role")
    @Test
    public void testGetAllManagersHigherRole() {
        when(blockedGroup.getAllUsers()).thenReturn(Collections.singleton(indirectManagerUser));

        Set<User> result = cut.getAllManagers();

        assertNotNull(result, "There should be a result");
        assertEquals(1, result.size(), "Wrong number of result");
        assertTrue(result.contains(directManagerUser), "The direct manager should be contained");
        assertFalse(result.contains(indirectManagerUser), "The indirect manager should not be contained");
    }

    @DisplayName("Get all managers at privilege group direct role is overriding")
    @Test
    public void testGetAllManagersDirectRoleOverrides() {
        when(blockedGroup.getAllUsers()).thenReturn(Collections.singleton(directManagerUser));

        Set<User> result = cut.getAllManagers();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directManagerUser), "The direct manager should be contained");
        assertTrue(result.contains(indirectManagerUser), "The indirect manager should be contained");
    }

    @DisplayName("Get all contributors at privilege group")
    @Test
    public void testGetAllContributors() {
        Set<User> result = cut.getAllContributors();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directContributorUser), "The direct contributor should be contained");
        assertTrue(result.contains(indirectContributorUser), "The indirect contributor should be contained");
    }

    @DisplayName("Get all contributors at privilege group with higher role")
    @Test
    public void testGetAllContributorsHigherRole() {
        when(managerGroup.getAllUsers()).thenReturn(Collections.singleton(indirectContributorUser));

        Set<User> result = cut.getAllContributors();

        assertNotNull(result, "There should be a result");
        assertEquals(1, result.size(), "Wrong number of result");
        assertTrue(result.contains(directContributorUser), "The direct contributor should be contained");
        assertFalse(result.contains(indirectContributorUser), "The indirect contributor should not be contained");
    }

    @DisplayName("Get all contributors at privilege group direct role is overriding")
    @Test
    public void testGetAllContributorsDirectRoleOverrides() {
        when(managerGroup.getAllUsers()).thenReturn(Collections.singleton(directContributorUser));

        Set<User> result = cut.getAllContributors();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directContributorUser), "The direct contributor should be contained");
        assertTrue(result.contains(indirectContributorUser), "The indirect contributor should be contained");
    }

    @DisplayName("Get all visitors at privilege group")
    @Test
    public void testGetAllVisitors() {
        Set<User> result = cut.getAllVisitors();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directVisitorUser), "The direct visitor should be contained");
        assertTrue(result.contains(indirectVisitorUser), "The indirect visitor should be contained");
    }

    @DisplayName("Get all visitors at privilege group with higher role")
    @Test
    public void testGetAllVisitorsHigherRole() {
        when(contributorGroup.getAllUsers()).thenReturn(Collections.singleton(indirectVisitorUser));

        Set<User> result = cut.getAllVisitors();

        assertNotNull(result, "There should be a result");
        assertEquals(1, result.size(), "Wrong number of result");
        assertTrue(result.contains(directVisitorUser), "The direct visitor should be contained");
        assertFalse(result.contains(indirectVisitorUser), "The indirect visitor should not be contained");
    }

    @DisplayName("Get all visitors at privilege group direct role is overriding")
    @Test
    public void testGetAllVisitorsDirectRoleOverrides() {
        when(contributorGroup.getAllUsers()).thenReturn(Collections.singleton(directVisitorUser));

        Set<User> result = cut.getAllVisitors();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directVisitorUser), "The direct visitor should be contained");
        assertTrue(result.contains(indirectVisitorUser), "The indirect visitor should be contained");
    }

    @DisplayName("Get all blocked users at privilege group")
    @Test
    public void testGetAllBlocks() {
        Set<User> result = cut.getAllBlocks();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directBlockedUser), "The direct blocked user should be contained");
        assertTrue(result.contains(indirectBlockedUser), "The indirect blocked user should be contained");
    }

    @DisplayName("Get all blocked users at privilege group with higher role")
    @Test
    public void testGetAllBlocksHigherRole() {
        when(adminGroup.getAllUsers()).thenReturn(Collections.singleton(indirectBlockedUser));

        Set<User> result = cut.getAllBlocks();

        assertNotNull(result, "There should be a result");
        assertEquals(1, result.size(), "Wrong number of result");
        assertTrue(result.contains(directBlockedUser), "The direct blocked user should be contained");
        assertFalse(result.contains(indirectBlockedUser), "The indirect blocked user should not be contained");
    }

    @DisplayName("Get all blocked users at privilege group direct role is overriding")
    @Test
    public void testGetAllBlocksDirectRoleOverrides() {
        when(adminGroup.getAllUsers()).thenReturn(Collections.singleton(directBlockedUser));

        Set<User> result = cut.getAllBlocks();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of result");
        assertTrue(result.contains(directBlockedUser), "The direct blocked user should be contained");
        assertTrue(result.contains(indirectBlockedUser), "The indirect blocked user should be contained");
    }
}
