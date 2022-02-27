package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.dto.group.BaseGroupDto;
import de.ma_vin.ape.users.model.gen.dto.group.BaseGroupIdRoleDto;
import de.ma_vin.ape.users.model.gen.dto.group.part.BaseGroupPartDto;
import de.ma_vin.ape.users.service.BaseGroupService;
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

import static de.ma_vin.ape.utils.controller.response.ResponseTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class BaseGroupControllerTest {

    public static final Long COMMON_GROUP_ID = 1L;
    public static final Long BASE_GROUP_ID = 2L;
    public static final Long PRIVILEGE_GROUP_ID = 3L;
    public static final Long PARENT_BASE_GROUP_ID = 4L;
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(COMMON_GROUP_ID, CommonGroup.ID_PREFIX);
    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String PRIVILEGE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PRIVILEGE_GROUP_ID, PrivilegeGroup.ID_PREFIX);
    public static final String PARENT_BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(PARENT_BASE_GROUP_ID, BaseGroup.ID_PREFIX);

    private AutoCloseable openMocks;
    private BaseGroupController cut;

    @Mock
    private BaseGroupService baseGroupService;
    @Mock
    private BaseGroup baseGroup;
    @Mock
    private BaseGroupDto baseGroupDto;
    @Mock
    private BaseGroupIdRoleDto baseGroupIdRoleDto;


    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new BaseGroupController();
        cut.setBaseGroupService(baseGroupService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Create a base group")
    @Test
    public void testCreateBaseGroup() {
        when(baseGroupService.save(any(), any())).thenAnswer(a -> {
            ((BaseGroup) a.getArgument(0)).setIdentification(BASE_GROUP_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<BaseGroupDto> response = cut.createBaseGroup("SomeName", COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(baseGroupService).save(any(), eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Create a base group but without save result")
    @Test
    public void testCreateBaseGroupWithoutResult() {
        when(baseGroupService.save(any(), any())).thenAnswer(a -> Optional.empty());

        ResponseWrapper<BaseGroupDto> response = cut.createBaseGroup("SomeName", COMMON_GROUP_IDENTIFICATION);

        checkError(response);

        verify(baseGroupService).save(any(), eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a base group")
    @Test
    public void testDeleteBaseGroup() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(baseGroup));
        when(baseGroupService.baseGroupExits(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            when(baseGroupService.baseGroupExits(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);
            return null;
        }).when(baseGroupService).delete(eq(baseGroup));

        ResponseWrapper<Boolean> response = cut.deleteBaseGroup(BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(baseGroupService).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).delete(any());
        verify(baseGroupService).baseGroupExits(eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a non existing base group")
    @Test
    public void testDeleteBaseGroupNonExisting() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(baseGroupService.baseGroupExits(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.deleteBaseGroup(BASE_GROUP_IDENTIFICATION);

        checkWarn(response);

        verify(baseGroupService).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).delete(any());
        verify(baseGroupService, never()).baseGroupExits(eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Delete a base group but still existing afterwards")
    @Test
    public void testDeleteBaseGroupExistingAfterDeletion() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(baseGroup));
        when(baseGroupService.baseGroupExits(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Boolean.TRUE);
        doAnswer(a -> {
            when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
            return null;
        }).when(baseGroupService).delete(eq(baseGroup));

        ResponseWrapper<Boolean> response = cut.deleteBaseGroup(BASE_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(baseGroupService).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).delete(any());
        verify(baseGroupService).baseGroupExits(eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a base group")
    @Test
    public void testGetBaseGroup() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(baseGroup));

        ResponseWrapper<BaseGroupDto> response = cut.getBaseGroup(BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(baseGroupService).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get a non existing base group")
    @Test
    public void testGetBaseGroupNonExisting() {
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());

        ResponseWrapper<BaseGroupDto> response = cut.getBaseGroup(BASE_GROUP_IDENTIFICATION);

        checkError(response);

        verify(baseGroupService).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Update a base group")
    @Test
    public void testUpdateBaseGroup() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupDto.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(baseGroup));
        when(baseGroupService.save(any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<BaseGroupDto> response = cut.updateBaseGroup(baseGroupDto, BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(baseGroupService).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).save(any());
        verify(baseGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a non existing base group")
    @Test
    public void testUpdateBaseGroupNonExisting() {
        when(baseGroupDto.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.empty());
        when(baseGroupService.save(any())).then(a -> Optional.of(a.getArgument(0)));

        ResponseWrapper<BaseGroupDto> response = cut.updateBaseGroup(baseGroupDto, BASE_GROUP_IDENTIFICATION);

        checkError(response);

        verify(baseGroupService).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).save(any());
        verify(baseGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a base group without save return")
    @Test
    public void testUpdateBaseGroupNoSaveReturn() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupDto.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findBaseGroup(eq(BASE_GROUP_IDENTIFICATION))).thenReturn(Optional.of(baseGroup));
        when(baseGroupService.save(any())).then(a -> Optional.empty());

        ResponseWrapper<BaseGroupDto> response = cut.updateBaseGroup(baseGroupDto, BASE_GROUP_IDENTIFICATION);

        checkFatal(response);

        verify(baseGroupService).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).save(any());
        verify(baseGroupService, never()).save(any(), any());
    }

    @DisplayName("Update a base group with different identification as parameter")
    @Test
    public void testUpdateBaseGroupDifferentIdentification() {
        List<String> storedIdentification = new ArrayList<>();
        String otherIdentification = BASE_GROUP_IDENTIFICATION + "1";
        when(baseGroup.getIdentification()).thenReturn(otherIdentification);
        when(baseGroupDto.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findBaseGroup(eq(otherIdentification))).thenReturn(Optional.of(baseGroup));
        when(baseGroupService.save(any())).then(a -> {
            storedIdentification.add(((BaseGroup) a.getArgument(0)).getIdentification());
            return Optional.of(a.getArgument(0));
        });

        ResponseWrapper<BaseGroupDto> response = cut.updateBaseGroup(baseGroupDto, otherIdentification);

        checkWarn(response, 1);

        assertEquals(1, storedIdentification.size(), "Wrong number of stored identifications");
        assertEquals(otherIdentification, storedIdentification.get(0), "Wrong stored identification");

        verify(baseGroupService).findBaseGroup(eq(otherIdentification));
        verify(baseGroupService, never()).findBaseGroup(eq(BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).save(any());
        verify(baseGroupService, never()).save(any(), any());
    }

    @DisplayName("Add base to privilege group")
    @Test
    public void testAddBaseToPrivilegeGroup() {
        when(baseGroupIdRoleDto.getBaseGroupIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupIdRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(baseGroupService.addBaseToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.addBaseToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, baseGroupIdRoleDto);

        checkOk(response);

        verify(baseGroupService).addBaseToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(BASE_GROUP_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Add base to privilege group, but not successful")
    @Test
    public void testAddBaseToPrivilegeGroupNotSuccessful() {
        when(baseGroupIdRoleDto.getBaseGroupIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupIdRoleDto.getRole()).thenReturn(Role.CONTRIBUTOR);
        when(baseGroupService.addBaseToPrivilegeGroup(any(), any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.addBaseToPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, baseGroupIdRoleDto);

        checkWarn(response, 1);

        verify(baseGroupService).addBaseToPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(BASE_GROUP_IDENTIFICATION), eq(Role.CONTRIBUTOR));
    }

    @DisplayName("Remove base from privilege group")
    @Test
    public void testRemoveBaseFromPrivilegeGroup() {
        when(baseGroupService.removeBaseFromPrivilegeGroup(any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.removeBaseFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(baseGroupService).removeBaseFromPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Remove base from privilege group, but not successful")
    @Test
    public void testRemoveBaseFromPrivilegeGroupNotSuccessful() {
        when(baseGroupService.removeBaseFromPrivilegeGroup(any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.removeBaseFromPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        checkWarn(response, 1);

        verify(baseGroupService).removeBaseFromPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Add base to base group")
    @Test
    public void testAddBaseToBaseGroup() {
        when(baseGroupService.addBaseToBaseGroup(any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.addBaseToBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(baseGroupService).addBaseToBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Add base to base group, but not successful")
    @Test
    public void testAddBaseToBaseGroupNotSuccessful() {
        when(baseGroupService.addBaseToBaseGroup(any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.addBaseToBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        checkWarn(response, 1);

        verify(baseGroupService).addBaseToBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Remove base from base group")
    @Test
    public void testRemoveBaseFromBaseGroup() {
        when(baseGroupService.removeBaseFromBaseGroup(any(), any())).thenReturn(Boolean.TRUE);

        ResponseWrapper<Boolean> response = cut.removeBaseFromBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        verify(baseGroupService).removeBaseFromBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Remove base from base group, but not successful")
    @Test
    public void testRemoveBaseFromBaseGroupNotSuccessful() {
        when(baseGroupService.removeBaseFromBaseGroup(any(), any())).thenReturn(Boolean.FALSE);

        ResponseWrapper<Boolean> response = cut.removeBaseFromBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, BASE_GROUP_IDENTIFICATION);

        checkWarn(response, 1);

        verify(baseGroupService).removeBaseFromBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), eq(BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Count base groups")
    @Test
    public void testCountBaseGroups() {
        when(baseGroupService.countBaseGroups(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countBaseGroups(COMMON_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of elements");

        verify(baseGroupService).countBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
    }

    @DisplayName("Count base groups at base group")
    @Test
    public void testCountBaseAtBaseGroup() {
        when(baseGroupService.countBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countBaseAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of elements");

        verify(baseGroupService).countBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Count base groups at privilege group")
    @Test
    public void testCountBaseAtPrivilegeGroup() {
        when(baseGroupService.countBasesAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countBaseAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of elements");

        verify(baseGroupService).countBasesAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER));
    }

    @DisplayName("Find all base groups at base group")
    @Test
    public void testFindAllBaseAtBaseGroup() {
        mockDefaultFindAllBaseAtBaseGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.findAllBaseAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Find all base groups at base group with pages, but missing page")
    @Test
    public void testFindAllBaseAtBaseGroupPageableMissingPage() {
        mockDefaultFindAllBaseAtBaseGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.findAllBaseAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Find all base groups at base group with pages, but missing size")
    @Test
    public void testFindAllBaseAtBaseGroupPageableMissingSize() {
        mockDefaultFindAllBaseAtBaseGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.findAllBaseAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Find all base groups at base group with pages")
    @Test
    public void testFindAllBaseAtBaseGroupPageable() {
        mockDefaultFindAllBaseAtBaseGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.findAllBaseAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultFindAllBaseAtBaseGroup() {
        when(baseGroupService.findAllBasesAtBaseGroup(any())).thenReturn(Collections.singletonList(baseGroup));
        when(baseGroupService.findAllBasesAtBaseGroup(any(), any(), any())).thenReturn(Collections.singletonList(baseGroup));
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Find all base group parts at base group")
    @Test
    public void testFindAllBasePartAtBaseGroup() {
        mockDefaultFindAllBaseAtBaseGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.findAllBasePartAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Find all base group parts at base group with pages, but missing page")
    @Test
    public void testFindAllBasePartAtBaseGroupPageableMissingPage() {
        mockDefaultFindAllBaseAtBaseGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.findAllBasePartAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Find all base group parts at base group with pages, but missing size")
    @Test
    public void testFindAllBasePartAtBaseGroupPageableMissingSize() {
        mockDefaultFindAllBaseAtBaseGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.findAllBasePartAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Find all base group parts at base group with pages")
    @Test
    public void testFindAllBasePartAtBaseGroupPageable() {
        mockDefaultFindAllBaseAtBaseGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.findAllBasePartAtBaseGroup(PARENT_BASE_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBasesAtBaseGroup(eq(PARENT_BASE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Find all base groups at privilege group")
    @Test
    public void testFindAllBaseAtPrivilegeGroup() {
        mockDefaultFindAllBaseAtPrivilegeGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.findAllBaseAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER));
        verify(baseGroupService, never()).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any(), any());
    }

    @DisplayName("Find all base groups at privilege group with pages, but missing page")
    @Test
    public void testFindAllBaseAtPrivilegeGroupPageableMissingPage() {
        mockDefaultFindAllBaseAtPrivilegeGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.findAllBaseAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any());
        verify(baseGroupService).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Find all base groups at privilege group with pages, but missing size")
    @Test
    public void testFindAllBaseAtPrivilegeGroupPageableMissingSize() {
        mockDefaultFindAllBaseAtPrivilegeGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.findAllBaseAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any());
        verify(baseGroupService).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Find all base groups at privilege group with pages")
    @Test
    public void testFindAllBaseAtPrivilegeGroupPageable() {
        mockDefaultFindAllBaseAtPrivilegeGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.findAllBaseAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any());
        verify(baseGroupService).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultFindAllBaseAtPrivilegeGroup() {
        when(baseGroupService.findAllBaseAtPrivilegeGroup(any(), any())).thenReturn(Collections.singletonList(baseGroup));
        when(baseGroupService.findAllBaseAtPrivilegeGroup(any(), any(), any(), any())).thenReturn(Collections.singletonList(baseGroup));
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
    }

    @DisplayName("Find all base group parts at privilege group")
    @Test
    public void testFindAllBasePartAtPrivilegeGroup() {
        mockDefaultFindAllBaseAtPrivilegeGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.findAllBasePartAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER));
        verify(baseGroupService, never()).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any(), any());
    }

    @DisplayName("Find all base group parts at privilege group with pages, but missing page")
    @Test
    public void testFindAllBasePartAtPrivilegeGroupPageableMissingPage() {
        mockDefaultFindAllBaseAtPrivilegeGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.findAllBasePartAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any());
        verify(baseGroupService).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Find all base group parts at privilege group with pages, but missing size")
    @Test
    public void testFindAllBasePartAtPrivilegeGroupPageableMissingSize() {
        mockDefaultFindAllBaseAtPrivilegeGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.findAllBasePartAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any());
        verify(baseGroupService).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Find all base group parts at privilege group with pages")
    @Test
    public void testFindAllBasePartAtPrivilegeGroupPageable() {
        mockDefaultFindAllBaseAtPrivilegeGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.findAllBasePartAtPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, Role.MANAGER, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of sub base groups");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any());
        verify(baseGroupService).findAllBaseAtPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Role.MANAGER), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Count available base groups for privilege group")
    @Test
    public void testCountAvailableBasesForPrivilegeGroup() {
        when(baseGroupService.countAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Long.valueOf(42L));

        ResponseWrapper<Long> response = cut.countAvailableBasesForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION);

        checkOk(response);

        assertEquals(Long.valueOf(42L), response.getResponse(), "Wrong number of result elements");

        verify(baseGroupService).countAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
    }

    @DisplayName("Get all available base groups for privilege group")
    @Test
    public void testGetAvailableBasesForPrivilegeGroup() {
        mockDefaultGetAvailableBasesForPrivilegeGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.getAllAvailableBasesForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all available base groups for privilege group with pages, but missing page")
    @Test
    public void testGetAvailableBasesForPrivilegeGroupPageableMissingPage() {
        mockDefaultGetAvailableBasesForPrivilegeGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.getAllAvailableBasesForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available base groups for privilege group with pages, but missing size")
    @Test
    public void testGetAvailableBasesForPrivilegeGroupPageableMissingSize() {
        mockDefaultGetAvailableBasesForPrivilegeGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.getAllAvailableBasesForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all available base groups for privilege group with pages")
    @Test
    public void testGetAvailableBasesForPrivilegeGroupPageable() {
        mockDefaultGetAvailableBasesForPrivilegeGroup();

        ResponseWrapper<List<BaseGroupDto>> response = cut.getAllAvailableBasesForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available base group parts for privilege group")
    @Test
    public void testGetAvailableBasePartsForPrivilegeGroup() {
        mockDefaultGetAvailableBasesForPrivilegeGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.getAllAvailableBasePartsForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all available base group parts for privilege group with pages, but missing page")
    @Test
    public void testGetAvailableBasePartsForPrivilegeGroupPageableMissingPage() {
        mockDefaultGetAvailableBasesForPrivilegeGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.getAllAvailableBasePartsForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all available base group for privilege group with pages, but missing size")
    @Test
    public void testGetAvailableBasePartsForPrivilegeGroupPageableMissingSize() {
        mockDefaultGetAvailableBasesForPrivilegeGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.getAllAvailableBasePartsForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all available base group for privilege group with pages")
    @Test
    public void testGetAvailableBasePartsForPrivilegeGroupPageable() {
        mockDefaultGetAvailableBasesForPrivilegeGroup();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.getAllAvailableBasePartsForPrivilegeGroup(PRIVILEGE_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of result elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }

    private void mockDefaultGetAvailableBasesForPrivilegeGroup() {
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
        when(baseGroupService.findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(baseGroup));
        when(baseGroupService.findAllAvailableBasesForPrivilegeGroup(eq(PRIVILEGE_GROUP_IDENTIFICATION), any(), any())).thenReturn(Collections.singletonList(baseGroup));
    }

    @DisplayName("Get all base groups")
    @Test
    public void testGetAllBaseGroups() {
        mockDefaultGetAllBaseGroups();

        ResponseWrapper<List<BaseGroupDto>> response = cut.getAllBaseGroups(COMMON_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all base groups with pages, but missing page")
    @Test
    public void testGetAllBaseGroupsPageableMissingPage() {
        mockDefaultGetAllBaseGroups();

        ResponseWrapper<List<BaseGroupDto>> response = cut.getAllBaseGroups(COMMON_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all base groups with pages, but missing size")
    @Test
    public void testGetAllBaseGroupsPageableMissingSize() {
        mockDefaultGetAllBaseGroups();

        ResponseWrapper<List<BaseGroupDto>> response = cut.getAllBaseGroups(COMMON_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all base groups with pages")
    @Test
    public void testGetAllBaseGroupsPageable() {
        mockDefaultGetAllBaseGroups();

        ResponseWrapper<List<BaseGroupDto>> response = cut.getAllBaseGroups(COMMON_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }


    @DisplayName("Get all base group parts")
    @Test
    public void testGetAllBaseGroupParts() {
        mockDefaultGetAllBaseGroups();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.getAllBaseGroupParts(COMMON_GROUP_IDENTIFICATION, null, null);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService, never()).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), any());
    }

    @DisplayName("Get all base group parts with pages, but missing page")
    @Test
    public void testGetAllBaseGroupPartsPageableMissingPage() {
        mockDefaultGetAllBaseGroups();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.getAllBaseGroupParts(COMMON_GROUP_IDENTIFICATION, null, 20);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), eq(Integer.valueOf(20)));
    }

    @DisplayName("Get all base group parts with pages, but missing size")
    @Test
    public void testGetAllBaseGroupPartsPageableMissingSize() {
        mockDefaultGetAllBaseGroups();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.getAllBaseGroupParts(COMMON_GROUP_IDENTIFICATION, 2, null);

        checkWarn(response);

        assertEquals(1, response.getMessages().size(), "Wrong number of warnings");
        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), any());
    }

    @DisplayName("Get all base group parts with pages")
    @Test
    public void testGetAllBaseGroupPartsPageable() {
        mockDefaultGetAllBaseGroups();

        ResponseWrapper<List<BaseGroupPartDto>> response = cut.getAllBaseGroupParts(COMMON_GROUP_IDENTIFICATION, 2, 20);

        checkOk(response);

        assertEquals(1, response.getResponse().size(), "Wrong number of elements");
        assertEquals(BASE_GROUP_IDENTIFICATION, response.getResponse().get(0).getIdentification(), "Wrong identification at first entry");

        verify(baseGroupService, never()).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION));
        verify(baseGroupService).findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), eq(Integer.valueOf(2)), eq(Integer.valueOf(20)));
    }


    private void mockDefaultGetAllBaseGroups() {
        when(baseGroupService.findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION))).thenReturn(Collections.singletonList(baseGroup));
        when(baseGroupService.findAllBaseGroups(eq(COMMON_GROUP_IDENTIFICATION), any(), any())).thenReturn(Collections.singletonList(baseGroup));
        when(baseGroup.getIdentification()).thenReturn(BASE_GROUP_IDENTIFICATION);
    }
}
