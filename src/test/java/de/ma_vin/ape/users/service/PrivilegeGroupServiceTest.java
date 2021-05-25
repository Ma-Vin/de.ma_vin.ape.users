package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dao.group.*;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.PrivilegeGroupRepository;
import de.ma_vin.ape.users.persistence.PrivilegeGroupToUserRepository;
import de.ma_vin.ape.users.persistence.PrivilegeToBaseGroupRepository;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class PrivilegeGroupServiceTest {
    public static final Long PRIVILEGE_GROUP_ID = 1L;
    public static final Long COMMON_GROUP_ID = 2L;
    public static final Long BASE_GROUP_ID = 3L;
    public static final Long USER_ID = 4L;
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);

    private PrivilegeGroupService cut;
    private AutoCloseable openMocks;

    @Mock
    private BaseGroupService baseGroupService;
    @Mock
    private PrivilegeGroupRepository privilegeGroupRepository;
    @Mock
    private PrivilegeGroupToUserRepository privilegeGroupToUserRepository;
    @Mock
    private PrivilegeToBaseGroupRepository privilegeToBaseGroupRepository;
    @Mock
    private PrivilegeGroup privilegeGroup;
    @Mock
    private PrivilegeGroupDao privilegeGroupDao;
    @Mock
    private BaseGroupDao baseGroupDao;
    @Mock
    private UserDao userDao;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new PrivilegeGroupService();
        cut.setBaseGroupService(baseGroupService);
        cut.setPrivilegeGroupRepository(privilegeGroupRepository);
        cut.setPrivilegeGroupToUserRepository(privilegeGroupToUserRepository);
        cut.setPrivilegeToBaseGroupRepository(privilegeToBaseGroupRepository);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Delete privilege group")
    @Test
    public void testDelete() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupToUserRepository.deleteByPrivilegeGroup(any())).thenReturn(1L);
        when(privilegeToBaseGroupRepository.deleteByPrivilegeGroup(any())).thenReturn(2L);

        cut.delete(privilegeGroup);

        verify(privilegeGroupToUserRepository).deleteByPrivilegeGroup(any());
        verify(privilegeToBaseGroupRepository).deleteByPrivilegeGroup(any());
        verify(privilegeGroupRepository).delete(any());
    }

    @DisplayName("Check existence of privilege group")
    @Test
    public void testPrivilegeGroupExits() {
        when(privilegeGroupRepository.existsById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Boolean.TRUE);

        assertTrue(cut.privilegeGroupExits(PRIVILEGE_GROUP_IDENTIFICATION), "The result should be true");
        verify(privilegeGroupRepository).existsById(eq(PRIVILEGE_GROUP_ID));
    }

    @DisplayName("Find non existing privilege group")
    @Test
    public void testFindPrivilegeGroupNonExisting() {
        when(privilegeGroupRepository.findById(any())).thenReturn(Optional.empty());

        Optional<PrivilegeGroup> result = cut.findPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
    }

    @DisplayName("Find non existing privilege group")
    @Test
    public void testFindPrivilegeGroup() {
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(any())).thenReturn(Optional.of(privilegeGroupDao));

        Optional<PrivilegeGroup> result = cut.findPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
    }

    @DisplayName("Find privilege group with all sub entities")
    @Test
    public void testFindBaseGroupTree() {
        ArrayList<PrivilegeGroupToBaseGroupDao> subGroups = new ArrayList<>();
        ArrayList<PrivilegeGroupToUserDao> users = new ArrayList<>();

        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(privilegeGroupDao.getAggBaseGroup()).thenReturn(subGroups);
        when(privilegeGroupDao.getAggUser()).thenReturn(users);

        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);

        PrivilegeGroupToBaseGroupDao btb = mock(PrivilegeGroupToBaseGroupDao.class);
        when(btb.getPrivilegeGroup()).thenReturn(privilegeGroupDao);
        when(btb.getBaseGroup()).thenReturn(baseGroupDao);
        when(btb.getFilterRole()).thenReturn(Role.MANAGER);
        when(privilegeToBaseGroupRepository.findAllByPrivilegeGroup(eq(privilegeGroupDao))).thenReturn(Collections.singletonList(btb));

        PrivilegeGroupToUserDao btu = mock(PrivilegeGroupToUserDao.class);
        when(btu.getPrivilegeGroup()).thenReturn(privilegeGroupDao);
        when(btu.getUser()).thenReturn(userDao);
        when(btu.getFilterRole()).thenReturn(Role.ADMIN);
        when(privilegeGroupToUserRepository.findAllByPrivilegeGroup(eq(privilegeGroupDao))).thenReturn(Collections.singletonList(btu));

        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);

        Optional<PrivilegeGroup> result = cut.findPrivilegeGroupTree(PRIVILEGE_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");
        assertEquals(0, result.get().getAdminGroups().size(), "Wrong number of admin groups");
        assertEquals(0, result.get().getBlockGroups().size(), "Wrong number of blocked groups");
        assertEquals(0, result.get().getContributorGroups().size(), "Wrong number of contributor groups");
        assertEquals(1, result.get().getManagerGroups().size(), "Wrong number of manager groups");
        assertEquals(0, result.get().getVisitorGroups().size(), "Wrong number of visitor groups");
        assertTrue(result.get().getManagerGroups().stream().anyMatch(bg -> bg.getIdentification().equals(BASE_GROUP_IDENTIFICATION))
                , "Wrong identification at manager group");

        assertEquals(1, result.get().getAdmins().size(), "Wrong number of admin users");
        assertEquals(0, result.get().getBlocks().size(), "Wrong number of blocked users");
        assertEquals(0, result.get().getContributors().size(), "Wrong number of contributor users");
        assertEquals(0, result.get().getManagers().size(), "Wrong number of manager users");
        assertEquals(0, result.get().getVisitors().size(), "Wrong number of visitor users");
        assertTrue(result.get().getAdmins().stream().anyMatch(u -> u.getIdentification().equals(USER_IDENTIFICATION))
                , "Wrong identification at admin user");

        verify(privilegeGroupRepository).findById(any());
        verify(privilegeToBaseGroupRepository).findAllByPrivilegeGroup(any());
        verify(privilegeGroupToUserRepository).findAllByPrivilegeGroup(any());
        verify(baseGroupService).loadSubTree(any());
    }

    @DisplayName("Find non existing privilege group with all sub entities")
    @Test
    public void testFindBaseGroupTreeNonExisting() {
        ArrayList<PrivilegeGroupToBaseGroupDao> subGroups = new ArrayList<>();
        ArrayList<PrivilegeGroupToUserDao> users = new ArrayList<>();

        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.empty());
        when(privilegeGroupDao.getAggBaseGroup()).thenReturn(subGroups);
        when(privilegeGroupDao.getAggUser()).thenReturn(users);

        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);

        PrivilegeGroupToBaseGroupDao btb = mock(PrivilegeGroupToBaseGroupDao.class);
        when(btb.getPrivilegeGroup()).thenReturn(privilegeGroupDao);
        when(btb.getBaseGroup()).thenReturn(baseGroupDao);
        when(btb.getFilterRole()).thenReturn(Role.MANAGER);
        when(privilegeToBaseGroupRepository.findAllByPrivilegeGroup(eq(privilegeGroupDao))).thenReturn(Collections.singletonList(btb));

        PrivilegeGroupToUserDao btu = mock(PrivilegeGroupToUserDao.class);
        when(btu.getPrivilegeGroup()).thenReturn(privilegeGroupDao);
        when(btu.getUser()).thenReturn(userDao);
        when(btu.getFilterRole()).thenReturn(Role.ADMIN);
        when(privilegeGroupToUserRepository.findAllByPrivilegeGroup(eq(privilegeGroupDao))).thenReturn(Collections.singletonList(btu));

        when(userDao.getId()).thenReturn(USER_ID);
        when(userDao.getIdentification()).thenReturn(USER_IDENTIFICATION);

        Optional<PrivilegeGroup> result = cut.findPrivilegeGroupTree(PRIVILEGE_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(privilegeGroupRepository).findById(any());
        verify(privilegeToBaseGroupRepository, never()).findAllByPrivilegeGroup(any());
        verify(privilegeGroupToUserRepository, never()).findAllByPrivilegeGroup(any());
        verify(baseGroupService, never()).loadSubTree(any());
    }

    @DisplayName("Find all privilege groups at common group")
    @Test
    public void testFindAllPrivilegeGroups() {
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findByParentCommonGroup(any())).thenReturn(Collections.singletonList(privilegeGroupDao));

        List<PrivilegeGroup> result = cut.findAllPrivilegeGroups(PRIVILEGE_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupRepository).findByParentCommonGroup(any());
    }

    @DisplayName("Save privilege group")
    @Test
    public void testSave() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(privilegeGroupRepository.save(any())).then(a -> a.getArgument(0));
        when(privilegeGroupRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(privilegeGroupRepository.getIdOfParentCommonGroup(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));

        Optional<PrivilegeGroup> result = cut.save(privilegeGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");

        verify(privilegeGroupRepository).getIdOfParentCommonGroup(eq(PRIVILEGE_GROUP_ID));
        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(privilegeGroupRepository).save(any());
    }

    @DisplayName("Save privilege group with non existing parent")
    @Test
    public void testSaveNonExistingParent() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(privilegeGroupRepository.save(any())).then(a -> a.getArgument(0));
        when(privilegeGroupRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());

        Optional<PrivilegeGroup> result = cut.save(privilegeGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(privilegeGroupRepository).getIdOfParentCommonGroup(eq(PRIVILEGE_GROUP_ID));
        verify(privilegeGroupRepository, never()).findById(any());
        verify(privilegeGroupRepository, never()).save(any());
    }

    @DisplayName("Save privilege group without identification")
    @Test
    public void testSaveNoIdentification() {
        when(privilegeGroup.getIdentification()).thenReturn(null);
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(privilegeGroupRepository.save(any())).then(a -> a.getArgument(0));
        when(privilegeGroupRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(privilegeGroupRepository.getIdOfParentCommonGroup(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));

        Optional<PrivilegeGroup> result = cut.save(privilegeGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(privilegeGroupRepository, never()).getIdOfParentCommonGroup(any());
        verify(privilegeGroupRepository, never()).findById(any());
        verify(privilegeGroupRepository, never()).save(any());
    }

    @DisplayName("Save new privilege group at common group")
    @Test
    public void testSaveAtCommonGroupNew() {
        when(privilegeGroup.getIdentification()).thenReturn(null);
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(privilegeGroupRepository.save(any())).then(a -> {
            assertNotNull(((PrivilegeGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((PrivilegeGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            ((PrivilegeGroupDao) a.getArgument(0)).setId(PRIVILEGE_GROUP_ID);
            return a.getArgument(0);
        });

        Optional<PrivilegeGroup> result = cut.save(privilegeGroup, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(privilegeGroupRepository, never()).findById(any());
        verify(privilegeGroupRepository).save(any());
    }

    @DisplayName("Save existing privilege group at common group")
    @Test
    public void testSaveAtCommonGroupExisting() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(privilegeGroupRepository.save(any())).then(a -> {
            assertNotNull(((PrivilegeGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((PrivilegeGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });

        Optional<PrivilegeGroup> result = cut.save(privilegeGroup, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(privilegeGroupRepository).findById(any());
        verify(privilegeGroupRepository).save(any());
    }

    @DisplayName("Save non existing privilege group at common group")
    @Test
    public void testSaveAtCommonGroupNonExisting() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.empty());
        when(privilegeGroupRepository.save(any())).then(a -> {
            assertNotNull(((PrivilegeGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((PrivilegeGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });

        Optional<PrivilegeGroup> result = cut.save(privilegeGroup, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(privilegeGroupRepository).findById(any());
        verify(privilegeGroupRepository, never()).save(any());
    }
}
