package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.CommonGroupRepository;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.users.service.history.CommonGroupChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class CommonGroupServiceTest {
    public static final Long COMMON_GROUP_ID = 1L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String PRINCIPAL_IDENTIFICATION = "UAA00001";

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
    private UserRepository userRepository;
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
    @Mock
    private CommonGroupChangeService commonGroupChangeService;


    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new CommonGroupService();
        cut.setUserService(userService);
        cut.setCommonGroupRepository(commonGroupRepository);
        cut.setUserRepository(userRepository);
        cut.setPrivilegeGroupService(privilegeGroupService);
        cut.setBaseGroupService(baseGroupService);
        cut.setCommonGroupChangeService(commonGroupChangeService);
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

        cut.delete(commonGroup, PRINCIPAL_IDENTIFICATION);

        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService, never()).delete(any(), any());
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService, never()).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(commonGroupRepository).delete(any());
        verify(commonGroupChangeService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Delete common group with sub entities")
    @Test
    public void testDeleteWithSubEntities() {
        when(userService.findAllUsersAtCommonGroup(anyString())).thenReturn(Collections.singletonList(user));
        when(privilegeGroupService.findAllPrivilegeGroups(anyString())).thenReturn(Collections.singletonList(privilegeGroup));
        when(baseGroupService.findAllBaseGroups(anyString())).thenReturn(Collections.singletonList(baseGroup));
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);

        cut.delete(commonGroup, PRINCIPAL_IDENTIFICATION);

        verify(userService).findAllUsersAtCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(userService).delete(eq(user), eq(PRINCIPAL_IDENTIFICATION));
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).delete(eq(privilegeGroup), eq(PRINCIPAL_IDENTIFICATION));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(commonGroupRepository).delete(any());
        verify(commonGroupChangeService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
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

    @DisplayName("Find existing common group")
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

    @DisplayName("Count common groups")
    @Test
    public void testCountCommonGroups() {
        when(commonGroupRepository.count()).thenReturn(42L);

        Long result = cut.countCommonGroups();
        assertNotNull(result, "The result should not be null");
        assertEquals(Long.valueOf(42L), result, "Wrong number of elements at result");

        verify(commonGroupRepository).count();
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

    @DisplayName("Find all common groups with pages")
    @Test
    public void testFindAllCommonGroupsPageable() {
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(commonGroupDao)));
        when(commonGroupRepository.findAll()).thenReturn(Collections.singletonList(commonGroupDao));

        List<CommonGroup> result = cut.findAllCommonGroups(1, 20);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(COMMON_GROUP_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(commonGroupRepository).findAll(eq(PageRequest.of(1, 20)));
        verify(commonGroupRepository, never()).findAll();
    }

    @DisplayName("Find all common groups with pages, but missing page")
    @Test
    public void testFindAllCommonGroupsPageableMissingPage() {
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(commonGroupDao)));
        when(commonGroupRepository.findAll()).thenReturn(Collections.singletonList(commonGroupDao));

        List<CommonGroup> result = cut.findAllCommonGroups(null, 20);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(COMMON_GROUP_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(commonGroupRepository, never()).findAll(any(Pageable.class));
        verify(commonGroupRepository).findAll();
    }

    @DisplayName("Find all common groups with pages, but missing size")
    @Test
    public void testFindAllCommonGroupsPageableMissingSize() {
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(commonGroupDao)));
        when(commonGroupRepository.findAll()).thenReturn(Collections.singletonList(commonGroupDao));


        List<CommonGroup> result = cut.findAllCommonGroups(1, null);
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(COMMON_GROUP_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(commonGroupRepository, never()).findAll(any(Pageable.class));
        verify(commonGroupRepository).findAll();
    }

    @DisplayName("Find parent common group of user")
    @Test
    public void testFindParentCommonGroupOfUser() {
        Long userId = 1L;
        String userIdentification = IdGenerator.generateIdentification(userId, User.ID_PREFIX);
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findById(any())).thenReturn(Optional.of(commonGroupDao));
        when(userRepository.getIdOfParentCommonGroup(eq(userId))).thenReturn(Optional.of(COMMON_GROUP_ID));

        Optional<CommonGroup> result = cut.findParentCommonGroupOfUser(userIdentification);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(COMMON_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");

        verify(commonGroupRepository).findById(eq(COMMON_GROUP_ID));
        verify(userRepository).getIdOfParentCommonGroup(eq(userId));
    }

    @DisplayName("Find parent common group of user, but user does not exists")
    @Test
    public void testFindParentCommonGroupOfUserNotExistingUser() {
        Long userId = 1L;
        String userIdentification = IdGenerator.generateIdentification(userId, User.ID_PREFIX);
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findById(any())).thenReturn(Optional.of(commonGroupDao));
        when(userRepository.getIdOfParentCommonGroup(any())).thenReturn(Optional.empty());

        Optional<CommonGroup> result = cut.findParentCommonGroupOfUser(userIdentification);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(commonGroupRepository, never()).findById(eq(COMMON_GROUP_ID));
        verify(userRepository).getIdOfParentCommonGroup(eq(userId));
    }

    @DisplayName("Find parent common group of user, but common group does not exists")
    @Test
    public void testFindParentCommonGroupOfUserNotExistingCommonGroup() {
        Long userId = 1L;
        String userIdentification = IdGenerator.generateIdentification(userId, User.ID_PREFIX);
        when(commonGroupRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.getIdOfParentCommonGroup(eq(userId))).thenReturn(Optional.of(COMMON_GROUP_ID));

        Optional<CommonGroup> result = cut.findParentCommonGroupOfUser(userIdentification);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(commonGroupRepository).findById(eq(COMMON_GROUP_ID));
        verify(userRepository).getIdOfParentCommonGroup(eq(userId));
    }

    @DisplayName("Save new common group")
    @Test
    public void testSaveNew() {
        when(commonGroup.getIdentification()).thenReturn(null);
        when(commonGroupRepository.save(any())).then(a -> {
            ((CommonGroupDao) a.getArgument(0)).setId(COMMON_GROUP_ID);
            return a.getArgument(0);
        });

        Optional<CommonGroup> result = cut.save(commonGroup, PRINCIPAL_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(COMMON_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(commonGroupRepository).save(any());
        verify(commonGroupChangeService).saveCreation(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(commonGroupChangeService, never()).saveChange(any(), any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Save existing common group")
    @Test
    public void testSaveExisting() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findById(eq(COMMON_GROUP_ID))).thenReturn(Optional.of(commonGroupDao));
        when(commonGroupRepository.save(any())).then(a -> a.getArgument(0));

        Optional<CommonGroup> result = cut.save(commonGroup, PRINCIPAL_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");

        verify(commonGroupRepository).findById(eq(COMMON_GROUP_ID));
        verify(commonGroupRepository).save(any());
        verify(commonGroupChangeService, never()).saveCreation(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(commonGroupChangeService).saveChange(any(), any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Save non existing common group")
    @Test
    public void testSaveNonExisting() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupDao.getId()).thenReturn(COMMON_GROUP_ID);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupRepository.findById(eq(COMMON_GROUP_ID))).thenReturn(Optional.empty());
        when(commonGroupRepository.save(any())).then(a -> a.getArgument(0));

        Optional<CommonGroup> result = cut.save(commonGroup, PRINCIPAL_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be present");

        verify(commonGroupRepository).findById(eq(COMMON_GROUP_ID));
        verify(commonGroupRepository, never()).save(any());
        verify(commonGroupChangeService, never()).saveCreation(any(), any());
        verify(commonGroupChangeService, never()).saveChange(any(), any(), eq(PRINCIPAL_IDENTIFICATION));
    }
}
