package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.persistence.BaseGroupRepository;
import de.ma_vin.ape.users.persistence.BaseGroupToUserRepository;
import de.ma_vin.ape.users.persistence.PrivilegeGroupRepository;
import de.ma_vin.ape.users.persistence.PrivilegeToBaseGroupRepository;
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

public class BaseGroupServiceTest {
    public static final Long BASE_GROUP_ID = 1L;
    public static final Long COMMON_GROUP_ID = 2L;
    public static final Long PRIVILEGE_GROUP_ID = 3L;
    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);

    private BaseGroupService cut;
    private AutoCloseable openMocks;

    @Mock
    private BaseGroupRepository baseGroupRepository;
    @Mock
    private BaseGroupToUserRepository baseGroupToUserRepository;
    @Mock
    private PrivilegeToBaseGroupRepository privilegeToBaseGroupRepository;
    @Mock
    private PrivilegeGroupRepository privilegeGroupRepository;
    @Mock
    private BaseGroup baseGroup;
    @Mock
    private BaseGroupDao baseGroupDao;
    @Mock
    private PrivilegeGroupDao privilegeGroupDao;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new BaseGroupService();
        cut.setBaseGroupRepository(baseGroupRepository);
        cut.setBaseGroupToUserRepository(baseGroupToUserRepository);
        cut.setPrivilegeToBaseGroupRepository(privilegeToBaseGroupRepository);
        cut.setPrivilegeGroupRepository(privilegeGroupRepository);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Delete base group")
    @Test
    public void testDelete() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupToUserRepository.deleteByBaseGroup(any())).thenReturn(1L);

        cut.delete(baseGroup);

        verify(baseGroupToUserRepository).deleteByBaseGroup(any());
        verify(baseGroupRepository).delete(any());
    }

    @DisplayName("Check existence of base group")
    @Test
    public void testBaseGroupExits() {
        when(baseGroupRepository.existsById(eq(BASE_GROUP_ID))).thenReturn(Boolean.TRUE);

        assertTrue(cut.baseGroupExits(BASE_GROUP_IDENTIFICATION), "The result should be true");
        verify(baseGroupRepository).existsById(eq(BASE_GROUP_ID));
    }

    @DisplayName("Find non existing base group")
    @Test
    public void testFindBaseGroupNonExisting() {
        when(baseGroupRepository.findById(any())).thenReturn(Optional.empty());

        Optional<BaseGroup> result = cut.findBaseGroup(BASE_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
    }

    @DisplayName("Find non existing base group")
    @Test
    public void testFindBaseGroup() {
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupRepository.findById(any())).thenReturn(Optional.of(baseGroupDao));

        Optional<BaseGroup> result = cut.findBaseGroup(BASE_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(BASE_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");

        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
    }

    @DisplayName("Find all base groups at common group")
    @Test
    public void testFindAllBaseGroups() {
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupRepository.findByParentCommonGroup(any())).thenReturn(Collections.singletonList(baseGroupDao));

        List<BaseGroup> result = cut.findAllBaseGroups(BASE_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(BASE_GROUP_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupRepository).findByParentCommonGroup(any());
    }

    @DisplayName("Save base group")
    @Test
    public void testSave() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(baseGroupRepository.save(any())).then(a -> a.getArgument(0));
        when(baseGroupRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(baseGroupRepository.getIdOfParentCommonGroup(eq(BASE_GROUP_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));

        Optional<BaseGroup> result = cut.save(baseGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");

        verify(baseGroupRepository).getIdOfParentCommonGroup(eq(BASE_GROUP_ID));
        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
        verify(baseGroupRepository).save(any());
    }

    @DisplayName("Save base group with non existing parent")
    @Test
    public void testSaveNonExistingParent() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(baseGroupRepository.save(any())).then(a -> a.getArgument(0));
        when(baseGroupRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());

        Optional<BaseGroup> result = cut.save(baseGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(baseGroupRepository).getIdOfParentCommonGroup(eq(BASE_GROUP_ID));
        verify(baseGroupRepository, never()).findById(any());
        verify(baseGroupRepository, never()).save(any());
    }

    @DisplayName("Save base group without identification")
    @Test
    public void testSaveNoIdentification() {
        when(baseGroup.getIdentification()).thenReturn(null);
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(baseGroupRepository.save(any())).then(a -> a.getArgument(0));
        when(baseGroupRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());
        when(baseGroupRepository.getIdOfParentCommonGroup(eq(BASE_GROUP_ID))).thenReturn(Optional.of(COMMON_GROUP_ID));

        Optional<BaseGroup> result = cut.save(baseGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(baseGroupRepository, never()).getIdOfParentCommonGroup(any());
        verify(baseGroupRepository, never()).findById(any());
        verify(baseGroupRepository, never()).save(any());
    }

    @DisplayName("Save new base group at common group")
    @Test
    public void testSaveAtCommonGroupNew() {
        when(baseGroup.getIdentification()).thenReturn(null);
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(baseGroupRepository.save(any())).then(a -> {
            assertNotNull(((BaseGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((BaseGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            ((BaseGroupDao) a.getArgument(0)).setId(BASE_GROUP_ID);
            return a.getArgument(0);
        });

        Optional<BaseGroup> result = cut.save(baseGroup, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(BASE_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(baseGroupRepository, never()).findById(any());
        verify(baseGroupRepository).save(any());
    }

    @DisplayName("Save existing base group at common group")
    @Test
    public void testSaveAtCommonGroupExisting() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(baseGroupRepository.save(any())).then(a -> {
            assertNotNull(((BaseGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((BaseGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });

        Optional<BaseGroup> result = cut.save(baseGroup, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(BASE_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(baseGroupRepository).findById(any());
        verify(baseGroupRepository).save(any());
    }

    @DisplayName("Save non existing base group at common group")
    @Test
    public void testSaveAtCommonGroupNonExisting() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.empty());
        when(baseGroupRepository.save(any())).then(a -> {
            assertNotNull(((BaseGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Parent at save should not be null");
            assertEquals(COMMON_GROUP_ID, ((BaseGroupDao) a.getArgument(0)).getParentCommonGroup().getId(), "Wrong parent at save");
            return a.getArgument(0);
        });

        Optional<BaseGroup> result = cut.save(baseGroup, COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(baseGroupRepository).findById(any());
        verify(baseGroupRepository, never()).save(any());
    }

    @DisplayName("Add a base to privilege group")
    @Test
    public void testAddBaseToPrivilegeGroup() {
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);

        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(privilegeToBaseGroupRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addBaseToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION, Role.CONTRIBUTOR);

        assertTrue(added, "The base group should be added to the privilege group");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
        verify(privilegeToBaseGroupRepository).save(any());
    }

    @DisplayName("Add a base to non existing privilege group")
    @Test
    public void testAddBaseToPrivilegeGroupMissingPrivilegeGroup() {
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);

        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.empty());
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(privilegeToBaseGroupRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addBaseToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION, Role.CONTRIBUTOR);

        assertFalse(added, "The base group should not be added to the privilege group");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(baseGroupRepository, never()).findById(eq(BASE_GROUP_ID));
        verify(privilegeToBaseGroupRepository, never()).save(any());
    }

    @DisplayName("Add non existing base to privilege group")
    @Test
    public void testAddBaseToPrivilegeGroupMissingBaseGroup() {
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);

        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.empty());
        when(privilegeToBaseGroupRepository.save(any())).then(a -> a.getArgument(0));

        boolean added = cut.addBaseToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION, Role.CONTRIBUTOR);

        assertFalse(added, "The base group should not be added to the privilege group");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
        verify(privilegeToBaseGroupRepository, never()).save(any());
    }

    @DisplayName("Add a base to privilege group without result at saving")
    @Test
    public void testAddBaseToPrivilegeGroupNoSavingResult() {
        when(baseGroupDao.getId()).thenReturn(BASE_GROUP_ID);
        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(privilegeGroupDao.getId()).thenReturn(PRIVILEGE_GROUP_ID);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);

        when(privilegeGroupRepository.findById(eq(PRIVILEGE_GROUP_ID))).thenReturn(Optional.of(privilegeGroupDao));
        when(baseGroupRepository.findById(eq(BASE_GROUP_ID))).thenReturn(Optional.of(baseGroupDao));
        when(privilegeToBaseGroupRepository.save(any())).thenReturn(null);

        boolean added = cut.addBaseToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION, Role.CONTRIBUTOR);

        assertFalse(added, "The base group should not be added to the privilege group");

        verify(privilegeGroupRepository).findById(eq(PRIVILEGE_GROUP_ID));
        verify(baseGroupRepository).findById(eq(BASE_GROUP_ID));
        verify(privilegeToBaseGroupRepository).save(any());
    }

    @DisplayName("Remove a base from privilege group")
    @Test
    public void testRemoveBaseFromPrivilegeGroup() {
        when(privilegeToBaseGroupRepository.deleteByPrivilegeGroupAndBaseGroup(any(), any())).thenReturn(1L);

        boolean removed = cut.removeBaseFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        assertTrue(removed, "The base group should be removed from the privilege group");

        verify(privilegeToBaseGroupRepository).deleteByPrivilegeGroupAndBaseGroup(any(), any());
    }

    @DisplayName("Remove a base from privilege group, but not connection exists")
    @Test
    public void testRemoveBaseFromPrivilegeGroupNonExisting() {
        when(privilegeToBaseGroupRepository.deleteByPrivilegeGroupAndBaseGroup(any(), any())).thenReturn(0L);

        boolean removed = cut.removeBaseFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        assertFalse(removed, "The base group should not be removed from the privilege group");

        verify(privilegeToBaseGroupRepository).deleteByPrivilegeGroupAndBaseGroup(any(), any());
    }

    @DisplayName("Remove a base from privilege group, but non more than one connection exists")
    @Test
    public void testRemoveBaseFromPrivilegeGroupNotUnique() {
        when(privilegeToBaseGroupRepository.deleteByPrivilegeGroupAndBaseGroup(any(), any())).thenReturn(2L);

        boolean removed = cut.removeBaseFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        assertFalse(removed, "The base group should not be removed from the privilege group");

        verify(privilegeToBaseGroupRepository).deleteByPrivilegeGroupAndBaseGroup(any(), any());
    }
}
