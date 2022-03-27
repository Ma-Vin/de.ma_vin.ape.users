package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.group.history.PrivilegeGroupChange;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.dto.group.PrivilegeGroupDto;
import de.ma_vin.ape.users.model.gen.dto.group.part.PrivilegeGroupPartDto;
import de.ma_vin.ape.users.model.gen.dto.history.ChangeDto;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.users.service.history.PrivilegeGroupChangeService;
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

public class PrivilegeGroupControllerTest {

    public static final Long COMMON_GROUP_ID = 1L;
    public static final Long PRIVILEGE_GROUP_ID = 2L;
    public static final Long EDITOR_ID = 3L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String EDITOR_IDENTIFICATION = IdGenerator.generateIdentification(EDITOR_ID, User.ID_PREFIX);
    public static final String PRINCIPAL_IDENTIFICATION = EDITOR_IDENTIFICATION;

    private AutoCloseable openMocks;
    private PrivilegeGroupController cut;

    @Mock
    private PrivilegeGroupService privilegeGroupService;
    @Mock
    private PrivilegeGroupChangeService privilegeGroupChangeService;
    @Mock
    private PrivilegeGroup privilegeGroup;
    @Mock
    private PrivilegeGroupDto privilegeGroupDto;
    @Mock
    private User editor;
    @Mock
    private PrivilegeGroupChange privilegeGroupChange;
    @Mock
    private Principal principal;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2022, 3, 27, 13, 7, 0));

        cut = new PrivilegeGroupController();
        cut.setPrivilegeGroupService(privilegeGroupService);
        cut.setPrivilegeGroupChangeService(privilegeGroupChangeService);

        when(principal.getName()).thenReturn(PRINCIPAL_IDENTIFICATION);
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(editor.getIdentification()).thenReturn(EDITOR_IDENTIFICATION);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Create a privilege group")
    @Test
    public void testCreatePrivilegeGroup() {
        when(privilegeGroupService.save(any(), any(), eq(PRINCIPAL_IDENTIFICATION))).thenAnswer(a -> {
            ((PrivilegeGroup) a.getArgument(0)).setIdentification(PRIVILEGE_GROUP_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<PrivilegeGroupDto> response = cut.createPrivilegeGroup(principal, "SomeName", COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(privilegeGroupService).save(any(), eq(COMMON_GROUP_IDENTIFICATION), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Create a privilege group but without save result")
    @Test
    public void testCreatePrivilegeGroupWithoutResult() {
        when(privilegeGroupService.save(any(), any(), eq(PRINCIPAL_IDENTIFICATION))).thenAnswer(a -> Optional.empty());

        ResponseWrapper<PrivilegeGroupDto> response = cut.createPrivilegeGroup(principal, "SomeName", COMMON_GROUP_IDENTIFICATION);

        checkError(response);

        verify(privilegeGroupService).save(any(), eq(COMMON_GROUP_IDENTIFICATION), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Delete a privilege group")
    @Test
    public void testDeletePrivilegeGroup() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            when(privilegeGroupService.privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(privilegeGroupService).delete(eq(privilegeGroup), eq(PRINCIPAL_IDENTIFICATION));

        ResponseWrapper<Boolean> response = cut.deletePrivilegeGroup(principal, PRIVILEGE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(privilegeGroupService).privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a non existing privilege group")
    @Test
    public void testDeletePrivilegeGroupNonExisting() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(privilegeGroupService.privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.deletePrivilegeGroup(principal, PRIVILEGE_GROUP_IDENTIFICATION);

        checkWarn(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService, never()).delete(any(), any());
        verify(privilegeGroupService, never()).privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a privilege group but still existing afterwards")
    @Test
    public void testDeletePrivilegeGroupExistingAfterDeletion() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            return null;
        }).when(privilegeGroupService).delete(eq(privilegeGroup), eq(PRINCIPAL_IDENTIFICATION));

        ResponseWrapper<Boolean> response = cut.deletePrivilegeGroup(principal, PRIVILEGE_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(privilegeGroupService).privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a privilege group")
    @Test
    public void testGetPrivilegeGroup() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));

        ResponseWrapper<PrivilegeGroupDto> response = cut.getPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a non existing privilege group")
    @Test
    public void testGetPrivilegeGroupNonExisting() {
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<PrivilegeGroupDto> response = cut.getPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);

        checkError(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Update a privilege group")
    @Test
    public void testUpdatePrivilegeGroup() {
        when(privilegeGroupDto.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.save(any(), eq(PRINCIPAL_IDENTIFICATION))).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<PrivilegeGroupDto> response = cut.updatePrivilegeGroup(principal, privilegeGroupDto, PRIVILEGE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(privilegeGroupService, never()).save(any(), any(), any());
    }

    @DisplayName("Update a non existing privilege group")
    @Test
    public void testUpdatePrivilegeGroupNonExisting() {
        when(privilegeGroupDto.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(privilegeGroupService.save(any(), eq(PRINCIPAL_IDENTIFICATION))).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<PrivilegeGroupDto> response = cut.updatePrivilegeGroup(principal, privilegeGroupDto, PRIVILEGE_GROUP_IDENTIFICATION);

        checkError(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService, never()).save(any(), any());
        verify(privilegeGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a privilege group without save return")
    @Test
    public void testUpdatePrivilegeGroupNoSaveReturn() {
        when(privilegeGroupDto.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.save(any(), eq(PRINCIPAL_IDENTIFICATION))).then(a -> Optional.empty());

        ResponseWrapper<PrivilegeGroupDto> response = cut.updatePrivilegeGroup(principal, privilegeGroupDto, PRIVILEGE_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(privilegeGroupService, never()).save(any(), any(), any());
    }

    @DisplayName("Update a privilege group with different identification as parameter")
    @Test
    public void testUpdatePrivilegeGroupDifferentIdentification() {
        List<String> storedIdentification = new ArrayList<>();
        String otherIdentification = PRIVILEGE_GROUP_IDENTIFICATION + "1";
        when(privilegeGroup.getIdentification()).thenReturn(otherIdentification);
        when(privilegeGroupDto.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(otherIdentification))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.save(any(), eq(PRINCIPAL_IDENTIFICATION))).then(a -> {
            storedIdentification.add(((PrivilegeGroup) a.getArgument(0)).getIdentification());
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<PrivilegeGroupDto> response = cut.updatePrivilegeGroup(principal, privilegeGroupDto, otherIdentification);

        checkWarn(response, 1);

        assertEquals(1, storedIdentification.size(), "Wrong number of stored identifications");
        assertEquals(otherIdentification, storedIdentification.get(0), "Wrong stored identification");

        verify(privilegeGroupService).findPrivilegeGroup(eq(otherIdentification));
        verify(privilegeGroupService, never()).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(privilegeGroupService, never()).save(any(), any(), any());
    }

    @DisplayName("Count privilege groups")
    @Test
    public void testCountPrivilegeGroups() {
        when(privilegeGroupService.countPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countPrivilegeGroups(COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of elements");

        verify(privilegeGroupService).countPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get all privilege groups")
    @Test
    public void testGetAllPrivilegeGroups() {
        mockDefaultGetAllPrivilegeGroups();

        ResponseWrapper<List<PrivilegeGroupDto>> response = cut.getAllPrivilegeGroups(COMMON_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService, never()).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all privilege groups with pages, but missing page")
    @Test
    public void testGetAllPrivilegeGroupsPageableMissingPage() {
        mockDefaultGetAllPrivilegeGroups();

        ResponseWrapper<List<PrivilegeGroupDto>> response = cut.getAllPrivilegeGroups(COMMON_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupService, never()).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all privilege groups with pages, but missing size")
    @Test
    public void testGetAllPrivilegeGroupsPageableMissingSize() {
        mockDefaultGetAllPrivilegeGroups();

        ResponseWrapper<List<PrivilegeGroupDto>> response = cut.getAllPrivilegeGroups(COMMON_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupService, never()).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all privilege groups with pages")
    @Test
    public void testGetAllPrivilegeGroupsPageable() {
        mockDefaultGetAllPrivilegeGroups();

        ResponseWrapper<List<PrivilegeGroupDto>> response = cut.getAllPrivilegeGroups(COMMON_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupService, never()).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all privilege group parts")
    @Test
    public void testGetAllPrivilegeGroupParts() {
        mockDefaultGetAllPrivilegeGroups();

        ResponseWrapper<List<PrivilegeGroupPartDto>> response = cut.getAllPrivilegeGroupParts(COMMON_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService, never()).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all privilege group parts with pages, but missing page")
    @Test
    public void testGetAllPrivilegeGroupPartsPageableMissingPage() {
        mockDefaultGetAllPrivilegeGroups();

        ResponseWrapper<List<PrivilegeGroupPartDto>> response = cut.getAllPrivilegeGroupParts(COMMON_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupService, never()).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all privilege group parts with pages, but missing size")
    @Test
    public void testGetAllPrivilegeGroupPartsPageableMissingSize() {
        mockDefaultGetAllPrivilegeGroups();

        ResponseWrapper<List<PrivilegeGroupPartDto>> response = cut.getAllPrivilegeGroupParts(COMMON_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupService, never()).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all privilege group parts with pages")
    @Test
    public void testGetAllPrivilegeGroupPartsPageable() {
        mockDefaultGetAllPrivilegeGroups();

        ResponseWrapper<List<PrivilegeGroupPartDto>> response = cut.getAllPrivilegeGroupParts(COMMON_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(privilegeGroupService, never()).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultGetAllPrivilegeGroups() {
        when(privilegeGroupService.findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(privilegeGroup));
        when(privilegeGroupService.findAllPrivilegeGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), any())).thenReturn(Collections.singletonList(privilegeGroup));
    }

    @DisplayName("Get history of a privilege group")
    @Test
    public void testGetPrivilegeGroupHistory() {
        when(privilegeGroupChangeService.loadChanges(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(privilegeGroupChange));
        when(privilegeGroupChange.getPrivilegeGroup()).thenReturn(privilegeGroup);
        when(privilegeGroupChange.getChangeType()).thenReturn(ChangeType.CREATE);
        when(privilegeGroupChange.getChangeTime()).thenReturn(SystemProperties.getSystemDateTime());
        when(privilegeGroupChange.getEditor()).thenReturn(editor);

        ResponseWrapper<List<ChangeDto>> response = cut.getPrivilegeGroupHistory(PRIVILEGE_GROUP_IDENTIFICATION);

        checkOk(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of changes");
        ChangeDto change = response.getResponse().get(0);
        assertEquals(PRIVILEGE_GROUP_IDENTIFICATION, change.getSubjectIdentification(), "Wrong admin group id");
        assertEquals(ChangeType.CREATE, change.getChangeType(), "Wrong change typ");
        assertEquals(SystemProperties.getSystemDateTime(), change.getChangeTime(), "Wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, change.getEditor(), "Wrong editor id");

        verify(privilegeGroupChangeService).loadChanges(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get empty history of a privilege group")
    @Test
    public void testGetPrivilegeGroupHistoryEmpty() {
        when(privilegeGroupChangeService.loadChanges(any())).thenReturn(Collections.emptyList());

        ResponseWrapper<List<ChangeDto>> response = cut.getPrivilegeGroupHistory(PRIVILEGE_GROUP_IDENTIFICATION);

        checkWarn(response);
        assertTrue(response.getMessages().stream()
                        .map(Message::getMessageText)
                        .anyMatch(String.format("No changes were found for privilege group %s, but at least one creation should exist at history", PRIVILEGE_GROUP_IDENTIFICATION)::equals)
                , "Missing warning message");

        verify(privilegeGroupChangeService).loadChanges(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }
}
