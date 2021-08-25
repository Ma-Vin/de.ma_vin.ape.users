package de.ma_vin.ape.users.controller;


import de.ma_vin.ape.users.model.domain.group.PrivilegeGroupExt;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.dto.group.PrivilegeGroupDto;
import de.ma_vin.ape.users.model.gen.mapper.GroupTransportMapper;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.utils.controller.response.ResponseUtil;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.ma_vin.ape.utils.controller.response.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping(path = "group/privilege")
@Log4j2
@Data
public class PrivilegeGroupController extends AbstractDefaultOperationController {

    @Autowired
    private PrivilegeGroupService privilegeGroupService;

    @PreAuthorize("isManager(#commonGroupIdentification, 'COMMON')")
    @PostMapping("/createPrivilegeGroup")
    public @ResponseBody
    ResponseWrapper<PrivilegeGroupDto> createPrivilegeGroup(@RequestParam String groupName, @RequestParam String commonGroupIdentification) {
        Optional<PrivilegeGroup> result = privilegeGroupService.save(new PrivilegeGroupExt(groupName), commonGroupIdentification);
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The group with name \"%s\" was not created", groupName));
        }
        return ResponseUtil.createSuccessResponse(GroupTransportMapper.convertToPrivilegeGroupDto(result.get()));
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @DeleteMapping("/deletePrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deletePrivilegeGroup(@PathVariable String privilegeGroupIdentification) {
        return delete(privilegeGroupIdentification, PrivilegeGroup.class
                , identificationToDelete -> privilegeGroupService.findPrivilegeGroup(identificationToDelete)
                , objectToDelete -> privilegeGroupService.delete(objectToDelete)
                , identificationToCheck -> privilegeGroupService.privilegeGroupExits(identificationToCheck));
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<PrivilegeGroupDto> getPrivilegeGroup(@PathVariable String privilegeGroupIdentification) {
        return get(privilegeGroupIdentification, PrivilegeGroup.class
                , identificationToGet -> privilegeGroupService.findPrivilegeGroup(identificationToGet)
                , GroupTransportMapper::convertToPrivilegeGroupDto
        );
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PutMapping("/updatePrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<PrivilegeGroupDto> updatePrivilegeGroup(@RequestBody PrivilegeGroupDto privilegeGroup, @PathVariable String privilegeGroupIdentification) {
        return update(privilegeGroup, privilegeGroupIdentification, PrivilegeGroup.class
                , identificationToUpdate -> privilegeGroupService.findPrivilegeGroup(identificationToUpdate)
                , objectToUpdate -> privilegeGroupService.save(objectToUpdate)
                , GroupTransportMapper::convertToPrivilegeGroupDto
                , GroupTransportMapper::convertToPrivilegeGroup
        );
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/countPrivilegeGroups/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countPrivilegeGroups(@PathVariable String commonGroupIdentification) {
        return createSuccessResponse(privilegeGroupService.countPrivilegeGroups(commonGroupIdentification));
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/getAllPrivilegeGroups/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<PrivilegeGroupDto>> getAllPrivilegeGroups(@PathVariable String commonGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        int pageToUse = page == null ? DEFAULT_PAGE : page;
        int sizeToUse = size == null ? DEFAULT_SIZE : size;

        List<PrivilegeGroup> privilegeGroups = page == null && size == null
                ? privilegeGroupService.findAllPrivilegeGroups(commonGroupIdentification)
                : privilegeGroupService.findAllPrivilegeGroups(commonGroupIdentification, pageToUse, sizeToUse);

        List<PrivilegeGroupDto> result = privilegeGroups.stream()
                .map(GroupTransportMapper::convertToPrivilegeGroupDto)
                .collect(Collectors.toList());

        return createPageableResponse(result, page, size);
    }
}
