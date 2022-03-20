package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.BaseGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.CommonGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.PrivilegeGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.persistence.history.BaseGroupChangeRepository;
import de.ma_vin.ape.users.persistence.history.CommonGroupChangeRepository;
import de.ma_vin.ape.users.persistence.history.PrivilegeGroupChangeRepository;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class BaseGroupChangeServiceTest {
    public static final Long BASE_GROUP_ID = 1L;
    public static final Long COMMON_GROUP_ID = 2L;
    public static final Long PRIVILEGE_GROUP_ID = 3L;
    public static final Long ANOTHER_BASE_GROUP_ID = 4L;
    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String ANOTHER_BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(ANOTHER_BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String PRINCIPAL_IDENTIFICATION = "UAA00001";

    @Mock
    private CommonGroupChangeRepository commonGroupChangeRepository;
    @Mock
    private BaseGroupChangeRepository baseGroupChangeRepository;
    @Mock
    private PrivilegeGroupChangeRepository privilegeGroupChangeRepository;
    @Mock
    private BaseGroupDao baseGroupDao;
    @Mock
    private BaseGroupDao storedBaseGroupDao;
    @Mock
    private BaseGroupDao anotherBaseGroupDao;
    @Mock
    private CommonGroupDao commonGroupDao;
    @Mock
    private PrivilegeGroupDao privilegeGroupDao;


    private BaseGroupChangeService cut;
    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new BaseGroupChangeService();

        cut.setBaseGroupChangeRepository(baseGroupChangeRepository);
        cut.setCommonGroupChangeRepository(commonGroupChangeRepository);
        cut.setPrivilegeGroupChangeRepository(privilegeGroupChangeRepository);

        when(baseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(anotherBaseGroupDao.getIdentification()).thenReturn(ANOTHER_BASE_GROUP_IDENTIFICATION);
        when(baseGroupDao.getParentCommonGroup()).thenReturn(commonGroupDao);
        when(storedBaseGroupDao.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(commonGroupDao.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(privilegeGroupDao.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Save a new base group")
    @Test
    public void testSaveCreation() {
        when(baseGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.CREATE, ((BaseGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for base group change");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getEditor(), "Missing editor for base group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for base group change");
            return null;
        });
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.ADD, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for common group change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing editor for common group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for common group change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getBaseGroup(), "Missing base group for common group change");
            assertEquals(BASE_GROUP_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getBaseGroup().getIdentification(), "Wrong base group for common group change");
            return null;
        });

        cut.saveCreation(baseGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(baseGroupChangeRepository).save(any());
        verify(commonGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified base group")
    @Test
    public void testSaveChange() {
        when(baseGroupDao.getGroupName()).thenReturn("abc");
        when(storedBaseGroupDao.getGroupName()).thenReturn("123");
        when(baseGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((BaseGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"123\" -> \"abc\"", ((BaseGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(baseGroupDao, storedBaseGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(baseGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified base group, change value to null")
    @Test
    public void testSaveChangeToNull() {
        when(storedBaseGroupDao.getGroupName()).thenReturn("123");
        when(baseGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((BaseGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"123\" -> \"null\"", ((BaseGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(baseGroupDao, storedBaseGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(baseGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified base group, change value from null")
    @Test
    public void testSaveChangeFromNull() {
        when(baseGroupDao.getGroupName()).thenReturn("abc");
        when(baseGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.MODIFY, ((BaseGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertEquals("GroupName: \"null\" -> \"abc\"", ((BaseGroupChangeDao) a.getArgument(0)).getAction(), "Wrong action");
            return null;
        });

        cut.saveChange(baseGroupDao, storedBaseGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(baseGroupChangeRepository).save(any());
    }

    @DisplayName("Save a modified base group, but nothing changed")
    @Test
    public void testSaveChangeNoChanges() {
        when(baseGroupDao.getGroupName()).thenReturn("abc");
        when(baseGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.UNKNOWN, ((BaseGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNull(((BaseGroupChangeDao) a.getArgument(0)).getAction(), "The action should be null");
            return null;
        });

        cut.saveChange(baseGroupDao, baseGroupDao, PRINCIPAL_IDENTIFICATION);
        verify(baseGroupChangeRepository).save(any());
    }

    @DisplayName("Delete a base group")
    @Test
    public void testDelete() {
        when(baseGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.DELETE, ((BaseGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for base group change");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier for base group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for base group change");
            assertEquals(BASE_GROUP_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information for base group change");
            return null;
        });
        when(commonGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.REMOVE, ((CommonGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type for base group change");
            assertNotNull(((CommonGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier for base group change");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier for base group change");
            assertEquals(BASE_GROUP_IDENTIFICATION, ((CommonGroupChangeDao) a.getArgument(0)).getDeletionInformation(), "Wrong delete information for base group change");
            return null;
        });

        cut.delete(baseGroupDao, PRINCIPAL_IDENTIFICATION);

        verify(baseGroupChangeRepository).save(any());
        verify(commonGroupChangeRepository).save(any());
        verify(baseGroupChangeRepository).markedAsDeleted(any(BaseGroupDao.class), eq(BASE_GROUP_IDENTIFICATION));
        verify(commonGroupChangeRepository).markedAsDeleted(any(BaseGroupDao.class), eq(BASE_GROUP_IDENTIFICATION));

        verify(privilegeGroupChangeRepository).markedAsDeleted(any(BaseGroupDao.class), any());
        verify(baseGroupChangeRepository).markedSubAsDeleted(any(BaseGroupDao.class), any());
    }

    @DisplayName("Add to first type of parent")
    @Test
    public void testAddToParentFirstType() {
        when(privilegeGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.ADD, ((PrivilegeGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getPrivilegeGroup(), "Missing privilege group");
            assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getPrivilegeGroup().getIdentification(), "Wrong privilege group");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getBaseGroup(), "Missing base group");
            assertEquals(BASE_GROUP_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getBaseGroup().getIdentification(), "Wrong base group");
            return null;
        });

        cut.addToParentFirstType(baseGroupDao, privilegeGroupDao, PRINCIPAL_IDENTIFICATION);

        verify(privilegeGroupChangeRepository).save(any());
    }

    @DisplayName("Add to second type of parent")
    @Test
    public void testAddToParentSecondType() {
        when(baseGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.ADD, ((BaseGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getBaseGroup(), "Missing base group");
            assertEquals(ANOTHER_BASE_GROUP_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getBaseGroup().getIdentification(), "Wrong base group");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getSubBaseGroup(), "Missing sub base group");
            assertEquals(BASE_GROUP_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getSubBaseGroup().getIdentification(), "Wrong sub base group");
            return null;
        });

        cut.addToParentSecondType(baseGroupDao, anotherBaseGroupDao, PRINCIPAL_IDENTIFICATION);

        verify(baseGroupChangeRepository).save(any());
    }

    @DisplayName("Remove to first type of parent")
    @Test
    public void testRemoveFromParentFirstType() {
        when(privilegeGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.REMOVE, ((PrivilegeGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getPrivilegeGroup(), "Missing privilege group");
            assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getPrivilegeGroup().getIdentification(), "Wrong privilege group");
            assertNotNull(((PrivilegeGroupChangeDao) a.getArgument(0)).getBaseGroup(), "Missing base group");
            assertEquals(BASE_GROUP_IDENTIFICATION, ((PrivilegeGroupChangeDao) a.getArgument(0)).getBaseGroup().getIdentification(), "Wrong base group");
            return null;
        });

        cut.removeFromParentFirstType(baseGroupDao, privilegeGroupDao, PRINCIPAL_IDENTIFICATION);

        verify(privilegeGroupChangeRepository).save(any());
    }

    @DisplayName("Remove to second type of parent")
    @Test
    public void testRemoveFromParentSecondType() {
        when(baseGroupChangeRepository.save(any())).then(a -> {
            assertEquals(ChangeType.REMOVE, ((BaseGroupChangeDao) a.getArgument(0)).getChangeType(), "Wrong change type");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getEditor(), "Missing modifier");
            assertEquals(PRINCIPAL_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getEditor().getIdentification(), "Wrong modifier");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getBaseGroup(), "Missing base group");
            assertEquals(ANOTHER_BASE_GROUP_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getBaseGroup().getIdentification(), "Wrong base group");
            assertNotNull(((BaseGroupChangeDao) a.getArgument(0)).getSubBaseGroup(), "Missing sub base group");
            assertEquals(BASE_GROUP_IDENTIFICATION, ((BaseGroupChangeDao) a.getArgument(0)).getSubBaseGroup().getIdentification(), "Wrong sub base group");
            return null;
        });

        cut.removeFromParentSecondType(baseGroupDao, anotherBaseGroupDao, PRINCIPAL_IDENTIFICATION);

        verify(baseGroupChangeRepository).save(any());
    }
}
