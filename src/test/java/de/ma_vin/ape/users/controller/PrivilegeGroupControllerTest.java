package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.dto.group.PrivilegeGroupDto;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class PrivilegeGroupControllerTest {

    public static final Long COMMON_GROUP_ID = 1L;
    public static final Long PRIVILEGE_GROUP_ID = 2L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);

    private AutoCloseable openMocks;
    private PrivilegeGroupController cut;

    @Mock
    private PrivilegeGroupService privilegeGroupService;
    @Mock
    private PrivilegeGroup privilegeGroup;
    @Mock
    private PrivilegeGroupDto privilegeGroupDto;


    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new PrivilegeGroupController();
        cut.setPrivilegeGroupService(privilegeGroupService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Create a privilege group")
    @Test
    public void testCreatePrivilegeGroup() {
        when(privilegeGroupService.save(any(), any())).thenAnswer(a -> {
            ((PrivilegeGroup) a.getArgument(0)).setIdentification(PRIVILEGE_GROUP_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<PrivilegeGroupDto> response = cut.createPrivilegeGroup("SomeName", COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(privilegeGroupService).save(any(), eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Create a privilege group but without save result")
    @Test
    public void testCreatePrivilegeGroupWithoutResult() {
        when(privilegeGroupService.save(any(), any())).thenAnswer(a -> Optional.empty());

        ResponseWrapper<PrivilegeGroupDto> response = cut.createPrivilegeGroup("SomeName", COMMON_GROUP_IDENTIFICATION);

        checkError(response);

        verify(privilegeGroupService).save(any(), eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a privilege group")
    @Test
    public void testDeletePrivilegeGroup() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            when(privilegeGroupService.privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(privilegeGroupService).delete(eq(privilegeGroup));

        ResponseWrapper<Boolean> response = cut.deletePrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).delete(any());
        verify(privilegeGroupService).privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a non existing privilege group")
    @Test
    public void testDeletePrivilegeGroupNonExisting() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(privilegeGroupService.privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.deletePrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);

        checkWarn(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService, never()).delete(any());
        verify(privilegeGroupService, never()).privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a privilege group but still existing afterwards")
    @Test
    public void testDeletePrivilegeGroupExistingAfterDeletion() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            return null;
        }).when(privilegeGroupService).delete(eq(privilegeGroup));

        ResponseWrapper<Boolean> response = cut.deletePrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).delete(any());
        verify(privilegeGroupService).privilegeGroupExits(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a privilege group")
    @Test
    public void testGetPrivilegeGroup() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
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
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupDto.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.save(any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<PrivilegeGroupDto> response = cut.updatePrivilegeGroup(privilegeGroupDto, PRIVILEGE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).save(any());
        verify(privilegeGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a non existing privilege group")
    @Test
    public void testUpdatePrivilegeGroupNonExisting() {
        when(privilegeGroupDto.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(privilegeGroupService.save(any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<PrivilegeGroupDto> response = cut.updatePrivilegeGroup(privilegeGroupDto, PRIVILEGE_GROUP_IDENTIFICATION);

        checkError(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService, never()).save(any());
        verify(privilegeGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a privilege group without save return")
    @Test
    public void testUpdatePrivilegeGroupNoSaveReturn() {
        when(privilegeGroup.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupDto.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.save(any())).then(a -> Optional.empty());

        ResponseWrapper<PrivilegeGroupDto> response = cut.updatePrivilegeGroup(privilegeGroupDto, PRIVILEGE_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(privilegeGroupService).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).save(any());
        verify(privilegeGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a privilege group with different identification as parameter")
    @Test
    public void testUpdatePrivilegeGroupDifferentIdentification() {
        List<String> storedIdentification = new ArrayList<>();
        String otherIdentification = PRIVILEGE_GROUP_IDENTIFICATION + "1";
        when(privilegeGroup.getIdentification()).thenReturn(otherIdentification);
        when(privilegeGroupDto.getIdentification()).thenReturn(PRIVILEGE_GROUP_IDENTIFICATION);
        when(privilegeGroupService.findPrivilegeGroup(eq(otherIdentification))).thenReturn(Optional.of(privilegeGroup));
        when(privilegeGroupService.save(any())).then(a -> {
            storedIdentification.add(((PrivilegeGroup) a.getArgument(0)).getIdentification());
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<PrivilegeGroupDto> response = cut.updatePrivilegeGroup(privilegeGroupDto, otherIdentification);

        checkWarn(response, 1);

        assertEquals(1, storedIdentification.size(), "Wrong number of stored identifications");
        assertEquals(otherIdentification, storedIdentification.get(0), "Wrong stored identification");

        verify(privilegeGroupService).findPrivilegeGroup(eq(otherIdentification));
        verify(privilegeGroupService, never()).findPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(privilegeGroupService).save(any());
        verify(privilegeGroupService, never()).save(any(), any());
    }
}
