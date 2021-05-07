package de.ma_vin.ape.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.AdminGroupRepository;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AdminGroupServiceTest {
    public static final Long ADMIN_GROUP_ID = 1L;
    public static final String ADMIN_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(ADMIN_GROUP_ID, AdminGroup.ID_PREFIX);

    private AdminGroupService cut;
    private AutoCloseable openMocks;

    @Mock
    private UserService userService;
    @Mock
    private AdminGroupRepository adminGroupRepository;
    @Mock
    private AdminGroup adminGroup;
    @Mock
    private AdminGroupDao adminGroupDao;
    @Mock
    private User user;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new AdminGroupService();
        cut.setUserService(userService);
        cut.setAdminGroupRepository(adminGroupRepository);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Delete admin group without sub entities")
    @Test
    public void testDelete() {
        when(userService.findAllUsersAtAdminGroup(anyString())).thenReturn(Collections.emptyList());
        when(adminGroup.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);

        cut.delete(adminGroup);

        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService, never()).delete(any());
        verify(adminGroupRepository).delete(any());
    }

    @DisplayName("Delete admin group with sub entities")
    @Test
    public void testDeleteWithSubEntities() {
        when(userService.findAllUsersAtAdminGroup(anyString())).thenReturn(Collections.singletonList(user));
        when(adminGroup.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);

        cut.delete(adminGroup);

        verify(userService).findAllUsersAtAdminGroup(eq(ADMIN_GROUP_IDENTIFICATION));
        verify(userService).delete(eq(user));
        verify(adminGroupRepository).delete(any());
    }

    @DisplayName("Check existence of admin group")
    @Test
    public void testAdminGroupExits() {
        when(adminGroupRepository.existsById(eq(ADMIN_GROUP_ID))).thenReturn(Boolean.TRUE);

        assertTrue(cut.adminGroupExits(ADMIN_GROUP_IDENTIFICATION), "The result should be true");
        verify(adminGroupRepository).existsById(eq(ADMIN_GROUP_ID));
    }

    @DisplayName("Find non existing admin group")
    @Test
    public void testFindAdminGroupNonExisting() {
        when(adminGroupRepository.findById(any())).thenReturn(Optional.empty());

        Optional<AdminGroup> result = cut.findAdminGroup(ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(adminGroupRepository).findById(eq(ADMIN_GROUP_ID));
    }

    @DisplayName("Find non existing admin group")
    @Test
    public void testFindAdminGroup() {
        when(adminGroupDao.getId()).thenReturn(ADMIN_GROUP_ID);
        when(adminGroupDao.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(adminGroupRepository.findById(any())).thenReturn(Optional.of(adminGroupDao));

        Optional<AdminGroup> result = cut.findAdminGroup(ADMIN_GROUP_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(ADMIN_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");

        verify(adminGroupRepository).findById(eq(ADMIN_GROUP_ID));
    }

    @DisplayName("Find all admin groups")
    @Test
    public void testFindAllAdminGroups() {
        when(adminGroupDao.getId()).thenReturn(ADMIN_GROUP_ID);
        when(adminGroupDao.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(adminGroupRepository.findAll()).thenReturn(Collections.singletonList(adminGroupDao));

        List<AdminGroup> result = cut.findAllAdminGroups();
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.size(), "Wrong number of elements at result");
        assertEquals(ADMIN_GROUP_IDENTIFICATION, result.get(0).getIdentification(), "Wrong identification at first entry");

        verify(adminGroupRepository).findAll();
    }

    @DisplayName("Save new admin group")
    @Test
    public void testSaveNew() {
        when(adminGroup.getIdentification()).thenReturn(null);
        when(adminGroupRepository.save(any())).then(a -> {
            ((AdminGroupDao) a.getArgument(0)).setId(ADMIN_GROUP_ID);
            return a.getArgument(0);
        });

        Optional<AdminGroup> result = cut.save(adminGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(ADMIN_GROUP_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(adminGroupRepository).save(any());
    }

    @DisplayName("Save existing admin group")
    @Test
    public void testSaveExisting() {
        when(adminGroup.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(adminGroupDao.getId()).thenReturn(ADMIN_GROUP_ID);
        when(adminGroupDao.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(adminGroupRepository.findById(eq(ADMIN_GROUP_ID))).thenReturn(Optional.of(adminGroupDao));
        when(adminGroupRepository.save(any())).then(a -> a.getArgument(0));

        Optional<AdminGroup> result = cut.save(adminGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");

        verify(adminGroupRepository).findById(eq(ADMIN_GROUP_ID));
        verify(adminGroupRepository).save(any());
    }

    @DisplayName("Save non existing admin group")
    @Test
    public void testSaveNonExisting() {
        when(adminGroup.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(adminGroupDao.getId()).thenReturn(ADMIN_GROUP_ID);
        when(adminGroupDao.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(adminGroupRepository.findById(eq(ADMIN_GROUP_ID))).thenReturn(Optional.empty());
        when(adminGroupRepository.save(any())).then(a -> a.getArgument(0));

        Optional<AdminGroup> result = cut.save(adminGroup);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be present");

        verify(adminGroupRepository).findById(eq(ADMIN_GROUP_ID));
        verify(adminGroupRepository, never()).save(any());
    }
}
