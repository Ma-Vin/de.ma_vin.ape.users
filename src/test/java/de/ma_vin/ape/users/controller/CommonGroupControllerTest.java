package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.history.CommonGroupChange;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.dto.group.CommonGroupDto;
import de.ma_vin.ape.users.model.gen.dto.group.part.CommonGroupPartDto;
import de.ma_vin.ape.users.model.gen.dto.history.ChangeDto;
import de.ma_vin.ape.users.service.CommonGroupService;
import de.ma_vin.ape.users.service.history.CommonGroupChangeService;
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

import static de.ma_vin.ape.users.controller.AbstractDefaultOperationController.DEFAULT_PAGE;
import static de.ma_vin.ape.users.controller.AbstractDefaultOperationController.DEFAULT_SIZE;
import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class CommonGroupControllerTest {

    public static final Long COMMON_GROUP_ID = 1L;
    public static final Long EDITOR_ID = 2L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String EDITOR_IDENTIFICATION = IdGenerator.generateIdentification(EDITOR_ID, User.ID_PREFIX);
    public static final String PRINCIPAL_IDENTIFICATION = EDITOR_IDENTIFICATION;

    private AutoCloseable openMocks;
    private CommonGroupController cut;

    @Mock
    private CommonGroupService commonGroupService;
    @Mock
    private CommonGroupChangeService commonGroupChangeService;
    @Mock
    private CommonGroup commonGroup;
    @Mock
    private CommonGroupDto commonGroupDto;
    @Mock
    private CommonGroupChange commonGroupChange;
    @Mock
    private User editor;
    @Mock
    private Principal principal;


    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2022, 3, 27, 13, 7, 0));

        cut = new CommonGroupController();
        cut.setCommonGroupService(commonGroupService);
        cut.setCommonGroupChangeService(commonGroupChangeService);

        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(editor.getIdentification()).thenReturn(EDITOR_IDENTIFICATION);
        when(principal.getName()).thenReturn(PRINCIPAL_IDENTIFICATION);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Create a common group")
    @Test
    public void testCreateCommonGroup() {
        when(commonGroupService.save(any(), any())).thenAnswer(a -> {
            ((CommonGroup) a.getArgument(0)).setIdentification(COMMON_GROUP_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<CommonGroupDto> response = cut.createCommonGroup(principal, "SomeName");

        checkOk(response);

        verify(commonGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Create a common group but without save result")
    @Test
    public void testCreateCommonGroupWithoutResult() {
        when(commonGroupService.save(any(), any())).thenAnswer(a -> Optional.empty());

        ResponseWrapper<CommonGroupDto> response = cut.createCommonGroup(principal, "SomeName");

        checkError(response);

        verify(commonGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Delete a common group")
    @Test
    public void testDeleteCommonGroup() {
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            when(commonGroupService.commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(commonGroupService).delete(eq(commonGroup), any());

        ResponseWrapper<Boolean> response = cut.deleteCommonGroup(principal, COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(commonGroupService).commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a non existing common group")
    @Test
    public void testDeleteCommonGroupNonExisting() {
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(commonGroupService.commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.deleteCommonGroup(principal, COMMON_GROUP_IDENTIFICATION);

        checkWarn(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService, never()).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(commonGroupService, never()).commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a common group but still existing afterwards")
    @Test
    public void testDeleteCommonGroupExistingAfterDeletion() {
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            return null;
        }).when(commonGroupService).delete(eq(commonGroup), any());

        ResponseWrapper<Boolean> response = cut.deleteCommonGroup(principal, COMMON_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).delete(any(), eq(PRINCIPAL_IDENTIFICATION));
        verify(commonGroupService).commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a common group")
    @Test
    public void testGetCommonGroup() {
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));

        ResponseWrapper<CommonGroupDto> response = cut.getCommonGroup(COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a non existing common group")
    @Test
    public void testGetCommonGroupNonExisting() {
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<CommonGroupDto> response = cut.getCommonGroup(COMMON_GROUP_IDENTIFICATION);

        checkError(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
    }


    @DisplayName("Get all common groups without pages")
    @Test
    public void testGetAllCommonGroups() {
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupDto>> response = cut.getAllCommonGroups(null, null);

        checkOk(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService).findAllCommonGroups();
        verify(commonGroupService, never()).findAllCommonGroups(any(), any());
    }

    @DisplayName("Get all common groups with pages")
    @Test
    public void testGetAllCommonGroupsWithPages() {
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupDto>> response = cut.getAllCommonGroups(2, 20);

        checkOk(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService, never()).findAllCommonGroups();
        verify(commonGroupService).findAllCommonGroups(eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all common groups with pages, but missing page")
    @Test
    public void testGetAllCommonGroupsWithPagesMissingPage() {
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupDto>> response = cut.getAllCommonGroups(null, 20);

        checkWarn(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService, never()).findAllCommonGroups();
        verify(commonGroupService).findAllCommonGroups(eq(DEFAULT_PAGE), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all common groups with pages, but missing size")
    @Test
    public void testGetAllCommonGroupsWithPagesMissingSize() {
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupDto>> response = cut.getAllCommonGroups(2, null);

        checkWarn(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService, never()).findAllCommonGroups();
        verify(commonGroupService).findAllCommonGroups(eq(Integer.valueOf(2)), eq(DEFAULT_SIZE));
    }

    @DisplayName("Get all common group parts without pages")
    @Test
    public void testGetAllCommonGroupParts() {
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupPartDto>> response = cut.getAllCommonGroupParts(null, null);

        checkOk(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService).findAllCommonGroups();
        verify(commonGroupService, never()).findAllCommonGroups(any(), any());
    }

    @DisplayName("Get all common group parts with pages")
    @Test
    public void testGetAllCommonGroupPartsWithPages() {
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupPartDto>> response = cut.getAllCommonGroupParts(2, 20);

        checkOk(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService, never()).findAllCommonGroups();
        verify(commonGroupService).findAllCommonGroups(eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all common group parts with pages, but missing page")
    @Test
    public void testGetAllCommonGroupPartsWithPagesMissingPage() {
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupPartDto>> response = cut.getAllCommonGroupParts(null, 20);

        checkWarn(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService, never()).findAllCommonGroups();
        verify(commonGroupService).findAllCommonGroups(eq(DEFAULT_PAGE), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all common group parts with pages, but missing size")
    @Test
    public void testGetAllCommonGroupPartsWithPagesMissingSize() {
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupPartDto>> response = cut.getAllCommonGroupParts(2, null);

        checkWarn(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService, never()).findAllCommonGroups();
        verify(commonGroupService).findAllCommonGroups(eq(Integer.valueOf(2)), eq(DEFAULT_SIZE));
    }

    @DisplayName("Update a common group")
    @Test
    public void testUpdateCommonGroup() {
        when(commonGroupDto.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.save(any(), any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<CommonGroupDto> response = cut.updateCommonGroup(principal, commonGroupDto, COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Update a non existing common group")
    @Test
    public void testUpdateCommonGroupNonExisting() {
        when(commonGroupDto.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(commonGroupService.save(any(), any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<CommonGroupDto> response = cut.updateCommonGroup(principal, commonGroupDto, COMMON_GROUP_IDENTIFICATION);

        checkError(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a common group without save return")
    @Test
    public void testUpdateCommonGroupNoSaveReturn() {
        when(commonGroupDto.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.save(any(), any())).then(a -> Optional.empty());

        ResponseWrapper<CommonGroupDto> response = cut.updateCommonGroup(principal, commonGroupDto, COMMON_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Update a common group with different identification as parameter")
    @Test
    public void testUpdateCommonGroupDifferentIdentification() {
        List<String> storedIdentification = new ArrayList<>();
        String otherIdentification = COMMON_GROUP_IDENTIFICATION + "1";
        when(commonGroup.getIdentification()).thenReturn(otherIdentification);
        when(commonGroupDto.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(otherIdentification))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.save(any(), any())).then(a -> {
            storedIdentification.add(((CommonGroup) a.getArgument(0)).getIdentification());
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<CommonGroupDto> response = cut.updateCommonGroup(principal, commonGroupDto, otherIdentification);

        checkWarn(response, 1);

        assertEquals(1, storedIdentification.size(), "Wrong number of stored identifications");
        assertEquals(otherIdentification, storedIdentification.get(0), "Wrong stored identification");

        verify(commonGroupService).findCommonGroup(eq(otherIdentification));
        verify(commonGroupService, never()).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).save(any(), eq(PRINCIPAL_IDENTIFICATION));
    }

    @DisplayName("Get parent common group of user")
    @Test
    public void testGetParentCommonGroupOfUser() {
        String userIdentification = IdGenerator.generateIdentification(1L, User.ID_PREFIX);
        when(commonGroupService.findParentCommonGroupOfUser(eq(userIdentification))).thenReturn(Optional.of(commonGroup));

        ResponseWrapper<CommonGroupDto> response = cut.getParentCommonGroupOfUser(userIdentification);

        checkOk(response);

        verify(commonGroupService).findParentCommonGroupOfUser(eq(userIdentification));
    }

    @DisplayName("Get parent common group of user, but failed")
    @Test
    public void testGetParentCommonGroupOfUserNonExisting() {
        String userIdentification = IdGenerator.generateIdentification(1L, User.ID_PREFIX);
        when(commonGroupService.findParentCommonGroupOfUser(eq(userIdentification))).thenReturn(Optional.empty());

        ResponseWrapper<CommonGroupDto> response = cut.getParentCommonGroupOfUser(userIdentification);

        checkError(response);

        verify(commonGroupService).findParentCommonGroupOfUser(eq(userIdentification));
    }

    @DisplayName("Get history of a common group")
    @Test
    public void testGetCommonGroupHistory() {
        when(commonGroupChangeService.loadChanges(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(commonGroupChange));
        when(commonGroupChange.getCommonGroup()).thenReturn(commonGroup);
        when(commonGroupChange.getChangeType()).thenReturn(ChangeType.CREATE);
        when(commonGroupChange.getChangeTime()).thenReturn(SystemProperties.getSystemDateTime());
        when(commonGroupChange.getEditor()).thenReturn(editor);

        ResponseWrapper<List<ChangeDto>> response = cut.getCommonGroupHistory(COMMON_GROUP_IDENTIFICATION);

        checkOk(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of changes");
        ChangeDto change = response.getResponse().get(0);
        assertEquals(COMMON_GROUP_IDENTIFICATION, change.getSubjectIdentification(), "Wrong admin group id");
        assertEquals(ChangeType.CREATE, change.getChangeType(), "Wrong change typ");
        assertEquals(SystemProperties.getSystemDateTime(), change.getChangeTime(), "Wrong change time");
        assertEquals(EDITOR_IDENTIFICATION, change.getEditor(), "Wrong editor id");

        verify(commonGroupChangeService).loadChanges(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get empty history of a common group")
    @Test
    public void testGetCommonGroupHistoryEmpty() {
        when(commonGroupChangeService.loadChanges(any())).thenReturn(Collections.emptyList());

        ResponseWrapper<List<ChangeDto>> response = cut.getCommonGroupHistory(COMMON_GROUP_IDENTIFICATION);

        checkWarn(response);
        assertTrue(response.getMessages().stream()
                        .map(Message::getMessageText)
                        .anyMatch(String.format("No changes were found for common group %s, but at least one creation should exist at history", COMMON_GROUP_IDENTIFICATION)::equals)
                , "Missing warning message");

        verify(commonGroupChangeService).loadChanges(eq(COMMON_GROUP_IDENTIFICATION));
    }
}
