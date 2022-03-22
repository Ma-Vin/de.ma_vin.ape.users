package de.ma_vin.ape.users.model.mapper;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.group.history.AdminGroupChange;
import de.ma_vin.ape.users.model.gen.domain.group.history.BaseGroupChange;
import de.ma_vin.ape.users.model.gen.domain.group.history.CommonGroupChange;
import de.ma_vin.ape.users.model.gen.domain.group.history.PrivilegeGroupChange;
import de.ma_vin.ape.users.model.gen.domain.history.AbstractChange;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.domain.user.history.UserChange;
import de.ma_vin.ape.users.model.gen.dto.history.ChangeDto;
import de.ma_vin.ape.utils.generators.IdGenerator;
import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link ChangeTransportMapper} is the class under test
 */
public class ChangeTransportMapperTest {

    public static final Long USER_ID = 1L;
    public static final Long COMMON_GROUP_ID = 2L;
    public static final Long ADMIN_GROUP_ID = 3L;
    public static final Long PRIVILEGE_GROUP_ID = 4L;
    public static final Long BASE_GROUP_ID = 5L;
    public static final Long SUB_BASE_GROUP_ID = 6L;
    public static final Long EDITOR_ID = 7L;
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);
    public static final String EDITOR_IDENTIFICATION = IdGenerator.generateIdentification(EDITOR_ID, User.ID_PREFIX);
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String ADMIN_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(ADMIN_GROUP_ID, AdminGroup.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String SUB_BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(SUB_BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String DEFAULT_CHANGE_ACTION = "dummyChangeText";
    public static final String DEFAULT_CHANGE_DELETE_INFO = "dummyDeleteText";

    private AutoCloseable openMocks;

    @Mock
    private UserChange userChange;
    @Mock
    private AdminGroupChange adminGroupChange;
    @Mock
    private CommonGroupChange commonGroupChange;
    @Mock
    private PrivilegeGroupChange privilegeGroupChange;
    @Mock
    private BaseGroupChange baseGroupChange;
    @Mock
    private User user;
    @Mock
    private User editor;
    @Mock
    private AdminGroup adminGroup;
    @Mock
    private CommonGroup commonGroup;
    @Mock
    private PrivilegeGroup privilegeGroup;
    @Mock
    private BaseGroup baseGroup;
    @Mock
    private BaseGroup subBaseGroup;


    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2022, 3, 22, 19, 38, 0));

        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(editor.getIdentification()).thenReturn(EDITOR_IDENTIFICATION);
        when(adminGroup.getIdentification()).thenReturn(ADMIN_GROUP_IDENTIFICATION);
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(subBaseGroup.getIdentification()).thenReturn(SUB_BASE_GROUP_IDENTIFICATION);

        when(userChange.getUser()).thenReturn(user);
        when(adminGroupChange.getAdminGroup()).thenReturn(adminGroup);
        when(commonGroupChange.getCommonGroup()).thenReturn(commonGroup);
        when(privilegeGroupChange.getPrivilegeGroup()).thenReturn(privilegeGroup);
        when(baseGroupChange.getBaseGroup()).thenReturn(baseGroup);

        mockDefault(userChange);
        mockDefault(adminGroupChange);
        mockDefault(commonGroupChange);
        mockDefault(privilegeGroupChange);
        mockDefault(baseGroupChange);
    }

    private void mockDefault(AbstractChange change) {
        when(change.getEditor()).thenReturn(editor);
        when(change.getChangeTime()).thenReturn(SystemProperties.getSystemDateTime());
        when(change.getAction()).thenReturn(DEFAULT_CHANGE_ACTION);
        when(change.getDeletionInformation()).thenReturn(DEFAULT_CHANGE_DELETE_INFO);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Convert user change of type create")
    @Test
    public void testConvertToChangeDtoUserCreate() {
        when(userChange.getChangeType()).thenReturn(ChangeType.CREATE);

        checkDefaultCreate(ChangeTransportMapper.convertToChangeDto(userChange), USER_IDENTIFICATION);
    }

    @DisplayName("Convert user change of type modify")
    @Test
    public void testConvertToChangeDtoUserModify() {
        when(userChange.getChangeType()).thenReturn(ChangeType.MODIFY);

        checkDefaultModify(ChangeTransportMapper.convertToChangeDto(userChange), USER_IDENTIFICATION);
    }

    @DisplayName("Convert user change of type delete")
    @Test
    public void testConvertToChangeDtoUserDelete() {
        when(userChange.getChangeType()).thenReturn(ChangeType.DELETE);

        checkDefaultDelete(ChangeTransportMapper.convertToChangeDto(userChange));
    }

    @DisplayName("Convert user change of type add")
    @Test
    public void testConvertToChangeDtoUserAdd() {
        when(userChange.getChangeType()).thenReturn(ChangeType.ADD);

        checkDefaultUnsupported(ChangeTransportMapper.convertToChangeDto(userChange), ChangeType.ADD);
    }

    @DisplayName("Convert user change of type remove")
    @Test
    public void testConvertToChangeDtoUserRemove() {
        when(userChange.getChangeType()).thenReturn(ChangeType.REMOVE);

        checkDefaultUnsupported(ChangeTransportMapper.convertToChangeDto(userChange), ChangeType.REMOVE);
    }

    @DisplayName("Convert user change of type unknown")
    @Test
    public void testConvertToChangeDtoUserUnknown() {
        when(userChange.getChangeType()).thenReturn(ChangeType.UNKNOWN);

        checkDefaultUnsupported(ChangeTransportMapper.convertToChangeDto(userChange), ChangeType.UNKNOWN);
    }

    @DisplayName("Convert admin group change of type create")
    @Test
    public void testConvertToChangeDtoAdminGroupCreate() {
        when(adminGroupChange.getChangeType()).thenReturn(ChangeType.CREATE);

        checkDefaultCreate(ChangeTransportMapper.convertToChangeDto(adminGroupChange), ADMIN_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert admin group change of type modify")
    @Test
    public void testConvertToChangeDtoAdminGroupModify() {
        when(adminGroupChange.getChangeType()).thenReturn(ChangeType.MODIFY);

        checkDefaultModify(ChangeTransportMapper.convertToChangeDto(adminGroupChange), ADMIN_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert admin group change of type delete")
    @Test
    public void testConvertToChangeDtoAdminGroupDelete() {
        when(adminGroupChange.getChangeType()).thenReturn(ChangeType.DELETE);

        checkDefaultDelete(ChangeTransportMapper.convertToChangeDto(adminGroupChange));
    }

    @DisplayName("Convert admin group change of type add")
    @Test
    public void testConvertToChangeDtoAdminGroupAdd() {
        when(adminGroupChange.getChangeType()).thenReturn(ChangeType.ADD);
        when(adminGroupChange.getAdmin()).thenReturn(user);

        checkDefaultAdd(ChangeTransportMapper.convertToChangeDto(adminGroupChange), ADMIN_GROUP_IDENTIFICATION, USER_IDENTIFICATION);
    }

    @DisplayName("Convert admin group change of type remove")
    @Test
    public void testConvertToChangeDtoAdminGroupRemove() {
        when(adminGroupChange.getChangeType()).thenReturn(ChangeType.REMOVE);
        when(adminGroupChange.getAdmin()).thenReturn(user);

        checkDefaultRemove(ChangeTransportMapper.convertToChangeDto(adminGroupChange), ADMIN_GROUP_IDENTIFICATION, USER_IDENTIFICATION);
    }

    @DisplayName("Convert admin group change of type unknown")
    @Test
    public void testConvertToChangeDtoAdminGroupUnknown() {
        when(adminGroupChange.getChangeType()).thenReturn(ChangeType.UNKNOWN);

        checkDefaultUnsupported(ChangeTransportMapper.convertToChangeDto(adminGroupChange), ChangeType.UNKNOWN);
    }

    @DisplayName("Convert common group change of type create")
    @Test
    public void testConvertToChangeDtoCommonGroupCreate() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.CREATE);

        checkDefaultCreate(ChangeTransportMapper.convertToChangeDto(commonGroupChange), COMMON_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert common group change of type modify")
    @Test
    public void testConvertToChangeDtoCommonGroupModify() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.MODIFY);

        checkDefaultModify(ChangeTransportMapper.convertToChangeDto(commonGroupChange), COMMON_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert common group change of type delete")
    @Test
    public void testConvertToChangeDtoCommonGroupDelete() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.DELETE);

        checkDefaultDelete(ChangeTransportMapper.convertToChangeDto(commonGroupChange));
    }

    @DisplayName("Convert common group change of type add for user")
    @Test
    public void testConvertToChangeDtoCommonGroupAddUser() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.ADD);
        when(commonGroupChange.getUser()).thenReturn(user);

        checkDefaultAdd(ChangeTransportMapper.convertToChangeDto(commonGroupChange), COMMON_GROUP_IDENTIFICATION, USER_IDENTIFICATION);
    }

    @DisplayName("Convert common group change of type remove for user")
    @Test
    public void testConvertToChangeDtoCommonGroupRemoveUser() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.REMOVE);
        when(commonGroupChange.getUser()).thenReturn(user);

        checkDefaultRemove(ChangeTransportMapper.convertToChangeDto(commonGroupChange), COMMON_GROUP_IDENTIFICATION, USER_IDENTIFICATION);
    }

    @DisplayName("Convert common group change of type add for privilege group")
    @Test
    public void testConvertToChangeDtoCommonGroupAddPrivilegeGroup() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.ADD);
        when(commonGroupChange.getPrivilegeGroup()).thenReturn(privilegeGroup);

        checkDefaultAdd(ChangeTransportMapper.convertToChangeDto(commonGroupChange), COMMON_GROUP_IDENTIFICATION, PRIVILEGE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert common group change of type remove for privilege group")
    @Test
    public void testConvertToChangeDtoCommonGroupRemovePrivilegeGroup() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.REMOVE);
        when(commonGroupChange.getPrivilegeGroup()).thenReturn(privilegeGroup);

        checkDefaultRemove(ChangeTransportMapper.convertToChangeDto(commonGroupChange), COMMON_GROUP_IDENTIFICATION, PRIVILEGE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert common group change of type add for base group")
    @Test
    public void testConvertToChangeDtoCommonGroupAddBaseGroup() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.ADD);
        when(commonGroupChange.getBaseGroup()).thenReturn(baseGroup);

        checkDefaultAdd(ChangeTransportMapper.convertToChangeDto(commonGroupChange), COMMON_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert common group change of type remove for base group")
    @Test
    public void testConvertToChangeDtoCommonGroupRemoveBaseGroup() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.REMOVE);
        when(commonGroupChange.getBaseGroup()).thenReturn(baseGroup);

        checkDefaultRemove(ChangeTransportMapper.convertToChangeDto(commonGroupChange), COMMON_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert common group change of type add but all null")
    @Test
    public void testConvertToChangeDtoCommonGroupAddEmpty() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.ADD);

        assertNull(ChangeTransportMapper.convertToChangeDto(commonGroupChange), "The result should be null");
    }

    @DisplayName("Convert common group change of type unknown")
    @Test
    public void testConvertToChangeDtoCommonGroupUnknown() {
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.UNKNOWN);

        checkDefaultUnsupported(ChangeTransportMapper.convertToChangeDto(commonGroupChange), ChangeType.UNKNOWN);
    }

    @DisplayName("Convert privilege group change of type create")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupCreate() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.CREATE);

        checkDefaultCreate(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange), PRIVILEGE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert privilege group change of type modify")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupModify() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.MODIFY);

        checkDefaultModify(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange), PRIVILEGE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert privilege group change of type delete")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupDelete() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.DELETE);

        checkDefaultDelete(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange));
    }

    @DisplayName("Convert privilege group change of type add for user")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupAddUser() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.ADD);
        when(privilegeGroupChange.getUser()).thenReturn(user);

        checkDefaultAdd(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange), PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);
    }

    @DisplayName("Convert privilege group change of type remove for user")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupRemoveUser() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.REMOVE);
        when(privilegeGroupChange.getUser()).thenReturn(user);

        checkDefaultRemove(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange), PRIVILEGE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);
    }

    @DisplayName("Convert privilege group change of type add for base group")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupAddBaseGroup() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.ADD);
        when(privilegeGroupChange.getBaseGroup()).thenReturn(baseGroup);

        checkDefaultAdd(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange), PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert privilege group change of type remove for base group")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupRemoveBaseGroup() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.REMOVE);
        when(privilegeGroupChange.getBaseGroup()).thenReturn(baseGroup);

        checkDefaultRemove(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange), PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert privilege group change of type add but all null")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupAddEmpty() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.ADD);

        assertNull(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange), "The result should be null");
    }

    @DisplayName("Convert privilege group change of type unknown")
    @Test
    public void testConvertToChangeDtoPrivilegeGroupUnknown() {
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.UNKNOWN);

        checkDefaultUnsupported(ChangeTransportMapper.convertToChangeDto(privilegeGroupChange), ChangeType.UNKNOWN);
    }

    @DisplayName("Convert base group change of type create")
    @Test
    public void testConvertToChangeDtoBaseGroupCreate() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.CREATE);

        checkDefaultCreate(ChangeTransportMapper.convertToChangeDto(baseGroupChange), BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert base group change of type modify")
    @Test
    public void testConvertToChangeDtoBaseGroupModify() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.MODIFY);

        checkDefaultModify(ChangeTransportMapper.convertToChangeDto(baseGroupChange), BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert base group change of type delete")
    @Test
    public void testConvertToChangeDtoBaseGroupDelete() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.DELETE);

        checkDefaultDelete(ChangeTransportMapper.convertToChangeDto(baseGroupChange));
    }

    @DisplayName("Convert base group change of type add for user")
    @Test
    public void testConvertToChangeDtoBaseGroupAddUser() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.ADD);
        when(baseGroupChange.getUser()).thenReturn(user);

        checkDefaultAdd(ChangeTransportMapper.convertToChangeDto(baseGroupChange), BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);
    }

    @DisplayName("Convert base group change of type remove for user")
    @Test
    public void testConvertToChangeDtoBaseGroupRemoveUser() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.REMOVE);
        when(baseGroupChange.getUser()).thenReturn(user);

        checkDefaultRemove(ChangeTransportMapper.convertToChangeDto(baseGroupChange), BASE_GROUP_IDENTIFICATION, USER_IDENTIFICATION);
    }

    @DisplayName("Convert base group change of type add for base group")
    @Test
    public void testConvertToChangeDtoBaseGroupAddBaseGroup() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.ADD);
        when(baseGroupChange.getSubBaseGroup()).thenReturn(subBaseGroup);

        checkDefaultAdd(ChangeTransportMapper.convertToChangeDto(baseGroupChange), BASE_GROUP_IDENTIFICATION, SUB_BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert base group change of type remove for base group")
    @Test
    public void testConvertToChangeDtoBaseGroupRemoveBaseGroup() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.REMOVE);
        when(baseGroupChange.getSubBaseGroup()).thenReturn(subBaseGroup);

        checkDefaultRemove(ChangeTransportMapper.convertToChangeDto(baseGroupChange), BASE_GROUP_IDENTIFICATION, SUB_BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Convert base group change of type add but all null")
    @Test
    public void testConvertToChangeDtoBaseGroupAddEmpty() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.ADD);

        assertNull(ChangeTransportMapper.convertToChangeDto(baseGroupChange), "The result should be null");
    }


    @DisplayName("Convert base group change of type unknown")
    @Test
    public void testConvertToChangeDtoBaseGroupUnknown() {
        when(baseGroupChange.getChangeType()).thenReturn(ChangeType.UNKNOWN);

        checkDefaultUnsupported(ChangeTransportMapper.convertToChangeDto(baseGroupChange), ChangeType.UNKNOWN);
    }

    private void checkDefaultCreate(ChangeDto result, String subjectId) {
        assertNotNull(result, "There should be any result");
        assertEquals(ChangeType.CREATE, result.getChangeType(), "wrong change type");
        assertEquals(SystemProperties.getSystemDateTime(), result.getChangeTime(), "wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, result.getEditor(), "wrong editor");
        assertEquals(subjectId, result.getSubjectIdentification(), "wrong subject id");
        assertNull(result.getTargetIdentification(), "target should be empty");
        assertEquals(DEFAULT_CHANGE_ACTION, result.getAction(), "wrong subject id");
    }

    private void checkDefaultModify(ChangeDto result, String subjectId) {
        assertNotNull(result, "There should be any result");
        assertEquals(ChangeType.MODIFY, result.getChangeType(), "wrong change type");
        assertEquals(SystemProperties.getSystemDateTime(), result.getChangeTime(), "wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, result.getEditor(), "wrong editor");
        assertEquals(subjectId, result.getSubjectIdentification(), "wrong subject id");
        assertNull(result.getTargetIdentification(), "target should be empty");
        assertEquals(DEFAULT_CHANGE_ACTION, result.getAction(), "wrong subject id");
    }

    private void checkDefaultDelete(ChangeDto result) {
        assertNotNull(result, "There should be any result");
        assertEquals(ChangeType.DELETE, result.getChangeType(), "wrong change type");
        assertEquals(SystemProperties.getSystemDateTime(), result.getChangeTime(), "wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, result.getEditor(), "wrong editor");
        assertEquals(DEFAULT_CHANGE_DELETE_INFO, result.getSubjectIdentification(), "wrong subject id");
        assertNull(result.getTargetIdentification(), "target should be empty");
        assertEquals(DEFAULT_CHANGE_ACTION, result.getAction(), "wrong subject id");
    }

    private void checkDefaultAdd(ChangeDto result, String subjectId, String targetId) {
        assertNotNull(result, "There should be any result");
        assertEquals(ChangeType.ADD, result.getChangeType(), "wrong change type");
        assertEquals(SystemProperties.getSystemDateTime(), result.getChangeTime(), "wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, result.getEditor(), "wrong editor");
        assertEquals(subjectId, result.getSubjectIdentification(), "wrong subject id");
        assertEquals(targetId, result.getTargetIdentification(), "wrong target id");
        assertEquals(DEFAULT_CHANGE_ACTION, result.getAction(), "wrong subject id");
    }

    private void checkDefaultRemove(ChangeDto result, String subjectId, String targetId) {
        assertNotNull(result, "There should be any result");
        assertEquals(ChangeType.REMOVE, result.getChangeType(), "wrong change type");
        assertEquals(SystemProperties.getSystemDateTime(), result.getChangeTime(), "wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, result.getEditor(), "wrong editor");
        assertEquals(subjectId, result.getSubjectIdentification(), "wrong subject id");
        assertEquals(targetId, result.getTargetIdentification(), "wrong target id");
        assertEquals(DEFAULT_CHANGE_ACTION, result.getAction(), "wrong subject id");
    }

    private void checkDefaultUnsupported(ChangeDto result, ChangeType changeType) {
        assertNotNull(result, "There should be any result");
        assertEquals(changeType, result.getChangeType(), "wrong change type");
        assertEquals(SystemProperties.getSystemDateTime(), result.getChangeTime(), "wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, result.getEditor(), "wrong editor");
        assertNull(result.getSubjectIdentification(), "subject id should be null");
        assertNull(result.getTargetIdentification(), "target id should be null");
        assertEquals(DEFAULT_CHANGE_ACTION, result.getAction(), "wrong subject id");
    }
}
