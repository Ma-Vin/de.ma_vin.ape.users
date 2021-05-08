package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.CommonGroupRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class CommonGroupServiceTest {
    public static final Long COMMON_GROUP_ID = 1L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);

    private CommonGroupService cut;
    private AutoCloseable openMocks;

    @Mock
    private UserService userService;
    @Mock
    private PrivilegeGroupService privilegeGroupService;
    @Mock
    private BaseGroupService baseGroupService;
    @Mock
    private CommonGroupRepository commonGroupRepository;
    @Mock
    private CommonGroup commonGroup;
    @Mock
    private CommonGroupDao commonGroupDao;
    @Mock
    private User user;
    @Mock
    private PrivilegeGroup privilegeGroup;
    @Mock
    private BaseGroup baseGroup;


    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new CommonGroupService();
        cut.setUserService(userService);
        cut.setCommonGroupRepository(commonGroupRepository);
        cut.setPrivilegeGroupService(privilegeGroupService);
        cut.setBaseGroupService(baseGroupService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Delete common group without sub entities")
    @Test
    public void testDelete() {
        when(userService.findAllUsersAtCommonGroup(anyString())).thenReturn(Collections.emptyList());
        when(privilegeGroupService.findAllPrivilegeGroups(anyString())).thenReturn(Collections.emptyList());
        when(baseGroupService.findAllBaseGroups(anyString())).thenReturn(Collections.emptyList());
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);

        cut.delete(commonGroup);

        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService, never()).delete(any());
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService, never()).delete(any());
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).delete(any());
        verify(commonGroupRepository).delete(any());
    }

    @DisplayName("Delete common group with sub entities")
    @Test
    public void testDeleteWithSubEntities() {
        when(userService.findAllUsersAtCommonGroup(anyString())).thenReturn(Collections.singletonList(user));
        when(privilegeGroupService.findAllPrivilegeGroups(anyString())).thenReturn(Collections.singletonList(privilegeGroup));
        when(baseGroupService.findAllBaseGroups(anyString())).thenReturn(Collections.singletonList(baseGroup));
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);

        cut.delete(commonGroup);

        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService).delete(eq(user));
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).delete(eq(privilegeGroup));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService).delete(any());
        verify(commonGroupRepository).delete(any());
    }

    @DisplayName("Check existence of common group")
    @Test
    public void testCommonGroupExits() {
        when(commonGroupRepository.existsById(eq(COMMON_GROUP_ID))).thenReturn(Boolean.TRUE);

        assertTrue(cut.commonGroupExits(COMMON_GROUP_IDENTIFICATION), "The result should be true");
        verify(commonGroupRepository).existsById(eq(COMMON_GROUP_ID));
    }

    @DisplayName("Find non existing common group")
    @Test
    public void testFindCommonGroupNonExisting() {
        when(commonGroupRepository.findById(any())).thenReturn(Optional.empty());

        Optional<CommonGroup> result = cut.findCommonGroup(COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(commonGroupRepository).findById(eq(COMMON_GROUP_ID));
    }

    @DisplayName("Find non existing common group")
    @Test
    public void testFindCommonGroup() {
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findById(any())).thenReturn(Optional.of(commonGroupDao));

        Optional<CommonGroup> result = cut.findCommonGroup(COMMON_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(COMMON_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");

        verify(commonGroupRepository).findById(eq(COMMON_GROUP_ID));
    }

    @DisplayName("Find all common groups")
    @Test
    public void testFindAllCommonGroups() {
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findAll()).thenReturn(Collections.singletonList(commonGroupDao));

        List<CommonGroup> result = cut.findAllCommonGroups();
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(COMMON_GROUP_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(commonGroupRepository).findAll();
    }

    @DisplayName("Save new common group")
    @Test
    public void testSaveNew() {
        when(commonGroup.getIdentification()).thenReturn(null);
        when(commonGroupRepository.save(any())).then(a -> {
            ((CommonGroupDao) a.getArgument(0)).setId(COMMON_GROUP_ID);
            return a.getArgument(0);
        });

        Optional<CommonGroup> result = cut.save(commonGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(COMMON_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(commonGroupRepository).save(any());
    }

    @DisplayName("Save existing common group")
    @Test
    public void testSaveExisting() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findById(eq(COMMON_GROUP_ID))).thenReturn(Optional.of(commonGroupDao));
        when(commonGroupRepository.save(any())).then(a -> a.getArgument(0));

        Optional<CommonGroup> result = cut.save(commonGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");

        verify(commonGroupRepository).findById(eq(COMMON_GROUP_ID));
        verify(commonGroupRepository).save(any());
    }

    @DisplayName("Save non existing common group")
    @Test
    public void testSaveNonExisting() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findById(eq(COMMON_GROUP_ID))).thenReturn(Optional.empty());
        when(commonGroupRepository.save(any())).then(a -> a.getArgument(0));

        Optional<CommonGroup> result = cut.save(commonGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be present");

        verify(commonGroupRepository).findById(eq(COMMON_GROUP_ID));
        verify(commonGroupRepository, never()).save(any());
    }
}
