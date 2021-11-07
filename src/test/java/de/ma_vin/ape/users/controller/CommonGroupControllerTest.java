package de.ma_vin.ape.users.controller;

import static de.ma_vin.ape.users.controller.AbstractDefaultOperationController.DEFAULT_PAGE;
import static de.ma_vin.ape.users.controller.AbstractDefaultOperationController.DEFAULT_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.*;

import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.dto.group.CommonGroupDto;
import de.ma_vin.ape.users.service.CommonGroupService;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
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

public class CommonGroupControllerTest {

    public static final Long COMMON_GROUP_ID = 1L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);

    private AutoCloseable openMocks;
    private CommonGroupController cut;

    @Mock
    private CommonGroupService commonGroupService;
    @Mock
    private CommonGroup commonGroup;
    @Mock
    private CommonGroupDto commonGroupDto;


    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new CommonGroupController();
        cut.setCommonGroupService(commonGroupService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Create a common group")
    @Test
    public void testCreateCommonGroup() {
        when(commonGroupService.save(any())).thenAnswer(a -> {
            ((CommonGroup) a.getArgument(0)).setIdentification(COMMON_GROUP_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<CommonGroupDto> response = cut.createCommonGroup("SomeName");

        checkOk(response);

        verify(commonGroupService).save(any());
    }

    @DisplayName("Create a common group but without save result")
    @Test
    public void testCreateCommonGroupWithoutResult() {
        when(commonGroupService.save(any())).thenAnswer(a -> Optional.empty());

        ResponseWrapper<CommonGroupDto> response = cut.createCommonGroup("SomeName");

        checkError(response);

        verify(commonGroupService).save(any());
    }

    @DisplayName("Delete a common group")
    @Test
    public void testDeleteCommonGroup() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            when(commonGroupService.commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(commonGroupService).delete(eq(commonGroup));

        ResponseWrapper<Boolean> response = cut.deleteCommonGroup(COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).delete(any());
        verify(commonGroupService).commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a non existing common group")
    @Test
    public void testDeleteCommonGroupNonExisting() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(commonGroupService.commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.deleteCommonGroup(COMMON_GROUP_IDENTIFICATION);

        checkWarn(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService, never()).delete(any());
        verify(commonGroupService, never()).commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a common group but still existing afterwards")
    @Test
    public void testDeleteCommonGroupExistingAfterDeletion() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            return null;
        }).when(commonGroupService).delete(eq(commonGroup));

        ResponseWrapper<Boolean> response = cut.deleteCommonGroup(COMMON_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).delete(any());
        verify(commonGroupService).commonGroupExits(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a common group")
    @Test
    public void testGetCommonGroup() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
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
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
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
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
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
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
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
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findAllCommonGroups(any(), any())).thenReturn(Collections.singletonList(commonGroup));
        when(commonGroupService.findAllCommonGroups()).thenReturn(Collections.singletonList(commonGroup));

        ResponseWrapper<List<CommonGroupDto>> response = cut.getAllCommonGroups(2, null);

        checkWarn(response);
        assertEquals(1, response.getResponse().size(), "Wrong number of common groups");
        assertEquals(COMMON_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at common group");

        verify(commonGroupService, never()).findAllCommonGroups();
        verify(commonGroupService).findAllCommonGroups(eq(Integer.valueOf(2)), eq(DEFAULT_SIZE));
    }

    @DisplayName("Update a common group")
    @Test
    public void testUpdateCommonGroup() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupDto.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.save(any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<CommonGroupDto> response = cut.updateCommonGroup(commonGroupDto, COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).save(any());
    }

    @DisplayName("Update a non existing common group")
    @Test
    public void testUpdateCommonGroupNonExisting() {
        when(commonGroupDto.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(commonGroupService.save(any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<CommonGroupDto> response = cut.updateCommonGroup(commonGroupDto, COMMON_GROUP_IDENTIFICATION);

        checkError(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService, never()).save(any());
    }

    @DisplayName("Update a common group without save return")
    @Test
    public void testUpdateCommonGroupNoSaveReturn() {
        when(commonGroup.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupDto.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.save(any())).then(a -> Optional.empty());

        ResponseWrapper<CommonGroupDto> response = cut.updateCommonGroup(commonGroupDto, COMMON_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(commonGroupService).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).save(any());
    }

    @DisplayName("Update a common group with different identification as parameter")
    @Test
    public void testUpdateCommonGroupDifferentIdentification() {
        List<String> storedIdentification = new ArrayList<>();
        String otherIdentification = COMMON_GROUP_IDENTIFICATION + "1";
        when(commonGroup.getIdentification()).thenReturn(otherIdentification);
        when(commonGroupDto.getIdentification()).thenReturn(COMMON_GROUP_IDENTIFICATION);
        when(commonGroupService.findCommonGroup(eq(otherIdentification))).thenReturn(Optional.of(commonGroup));
        when(commonGroupService.save(any())).then(a -> {
            storedIdentification.add(((CommonGroup) a.getArgument(0)).getIdentification());
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<CommonGroupDto> response = cut.updateCommonGroup(commonGroupDto, otherIdentification);

        checkWarn(response, 1);

        assertEquals(1, storedIdentification.size(), "Wrong number of stored identifications");
        assertEquals(otherIdentification, storedIdentification.get(0), "Wrong stored identification");

        verify(commonGroupService).findCommonGroup(eq(otherIdentification));
        verify(commonGroupService, never()).findCommonGroup(eq(COMMON_GROUP_IDENTIFICATION));
        verify(commonGroupService).save(any());
    }
}
