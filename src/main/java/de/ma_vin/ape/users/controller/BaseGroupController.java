package de.ma_vin.ape.users.controller;

import static de.ma_vin.ape.utils.controller.response.ResponseUtil.*;

import de.ma_vin.ape.users.model.domain.group.BaseGroupExt;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.dto.group.BaseGroupDto;
import de.ma_vin.ape.users.model.gen.dto.group.BaseGroupIdRoleDto;
import de.ma_vin.ape.users.model.gen.mapper.GroupTransportMapper;
import de.ma_vin.ape.users.service.BaseGroupService;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "group/base")
@Log4j2
@Data
public class BaseGroupController extends AbstractDefaultOperationController {

    @Autowired
    private BaseGroupService baseGroupService;

    @PreAuthorize("isManager(#commonGroupIdentification, 'COMMON')")
    @PostMapping("/createBaseGroup")
    public @ResponseBody
    ResponseWrapper<BaseGroupDto> createBaseGroup(@RequestParam String groupName, @RequestParam String commonGroupIdentification) {
        Optional<BaseGroup> result = baseGroupService.save(new BaseGroupExt(groupName), commonGroupIdentification);
        if (result.isEmpty()) {
            return createEmptyResponseWithError(String.format("The group with name \"%s\" was not created", groupName));
        }
        return createSuccessResponse(GroupTransportMapper.convertToBaseGroupDto(result.get()));
    }

    @PreAuthorize("isManager(#baseGroupIdentification, 'BASE')")
    @DeleteMapping("/deleteBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteBaseGroup(@PathVariable String baseGroupIdentification) {
        return delete(baseGroupIdentification, BaseGroup.class
                , identificationToDelete -> baseGroupService.findBaseGroup(identificationToDelete)
                , objectToDelete -> baseGroupService.delete(objectToDelete)
                , identificationToCheck -> baseGroupService.baseGroupExits(identificationToCheck));
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/getBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<BaseGroupDto> getBaseGroup(@PathVariable String baseGroupIdentification) {
        return get(baseGroupIdentification, BaseGroup.class
                , identificationToGet -> baseGroupService.findBaseGroup(identificationToGet)
                , GroupTransportMapper::convertToBaseGroupDto
        );
    }

    @PreAuthorize("isManager(#baseGroupIdentification, 'BASE')")
    @PutMapping("/updateBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<BaseGroupDto> updateBaseGroup(@RequestBody BaseGroupDto baseGroup, @PathVariable String baseGroupIdentification) {
        return update(baseGroup, baseGroupIdentification, BaseGroup.class
                , identificationToUpdate -> baseGroupService.findBaseGroup(identificationToUpdate)
                , objectToUpdate -> baseGroupService.save(objectToUpdate)
                , GroupTransportMapper::convertToBaseGroupDto
                , GroupTransportMapper::convertToBaseGroup
        );
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/getAllBaseGroups/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupDto>> getAllBaseGroups(@PathVariable String commonGroupIdentification) {
        List<BaseGroupDto> result = baseGroupService.findAllBaseGroups(commonGroupIdentification).stream()
                .map(GroupTransportMapper::convertToBaseGroupDto)
                .collect(Collectors.toList());

        return createSuccessResponse(result);
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PatchMapping("/addBaseToPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> addBaseToPrivilegeGroup(@PathVariable String privilegeGroupIdentification, @RequestBody BaseGroupIdRoleDto baseGroupRole) {
        boolean result = baseGroupService.addBaseToPrivilegeGroup(privilegeGroupIdentification, baseGroupRole.getBaseGroupIdentification(), baseGroupRole.getRole());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The base group with identification \"%s\" was not added with role %s to privilege group with identification \"%s\""
                , baseGroupRole.getBaseGroupIdentification(), baseGroupRole.getRole().getDescription(), privilegeGroupIdentification));
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PatchMapping("/removeBaseFromPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeBaseFromPrivilegeGroup(@PathVariable String privilegeGroupIdentification, @RequestBody String baseGroupIdentification) {
        boolean result = baseGroupService.removeBaseFromPrivilegeGroup(privilegeGroupIdentification, baseGroupIdentification);
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The base group with identification \"%s\" was not removed from privilege group with identification \"%s\""
                , baseGroupIdentification, privilegeGroupIdentification));
    }

    @PreAuthorize("isVisitor(#parentGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/findAllBaseAtPrivilegeGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupDto>> findAllBaseAtPrivilegeGroup(@PathVariable String parentGroupIdentification) {
        List<BaseGroupDto> result = baseGroupService.findAllBaseAtPrivilegeGroup(parentGroupIdentification).stream()
                .map(GroupTransportMapper::convertToBaseGroupDto)
                .collect(Collectors.toList());
        return createSuccessResponse(result);
    }

    @PreAuthorize("isContributor(#parentGroupIdentification, 'BASE')")
    @PatchMapping("/addBaseToBaseGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> addBaseToBaseGroup(@PathVariable String parentGroupIdentification, @RequestBody String baseGroupIdentification) {
        boolean result = baseGroupService.addBaseToBaseGroup(parentGroupIdentification, baseGroupIdentification);
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The base group with identification \"%s\" was not added base group with identification \"%s\""
                , baseGroupIdentification, parentGroupIdentification));
    }

    @PreAuthorize("isContributor(#parentGroupIdentification, 'BASE')")
    @PatchMapping("/removeBaseFromBaseGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeBaseFromBaseGroup(@PathVariable String parentGroupIdentification, @RequestBody String baseGroupIdentification) {
        boolean result = baseGroupService.removeBaseFromBaseGroup(parentGroupIdentification, baseGroupIdentification);
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The base group with identification \"%s\" was not removed from base group with identification \"%s\""
                , baseGroupIdentification, parentGroupIdentification));
    }

    @PreAuthorize("isVisitor(#parentGroupIdentification, 'BASE')")
    @GetMapping("/findAllBaseAtBaseGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupDto>> findAllBaseAtBaseGroup(@PathVariable String parentGroupIdentification) {
        List<BaseGroupDto> result = baseGroupService.findAllBasesAtBaseGroup(parentGroupIdentification).stream()
                .map(GroupTransportMapper::convertToBaseGroupDto)
                .collect(Collectors.toList());
        return createSuccessResponse(result);
    }
}
