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
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(path = "group/privilege")
@Log4j2
@Data
public class PrivilegeGroupController extends AbstractDefaultOperationController {

    @Autowired
    private PrivilegeGroupService privilegeGroupService;

    @PostMapping("/createPrivilegeGroup")
    public @ResponseBody
    ResponseWrapper<PrivilegeGroupDto> createPrivilegeGroup(@RequestParam String groupName, @RequestParam String commonGroupIdentification) {
        Optional<PrivilegeGroup> result = privilegeGroupService.save(new PrivilegeGroupExt(groupName), commonGroupIdentification);
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The group with name \"%s\" was not created", groupName));
        }
        return ResponseUtil.createSuccessResponse(GroupTransportMapper.convertToPrivilegeGroupDto(result.get()));
    }

    @DeleteMapping("/deletePrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deletePrivilegeGroup(@PathVariable String privilegeGroupIdentification) {
        return delete(privilegeGroupIdentification, PrivilegeGroup.class
                , identificationToDelete -> privilegeGroupService.findPrivilegeGroup(identificationToDelete)
                , objectToDelete -> privilegeGroupService.delete(objectToDelete)
                , identificationToCheck -> privilegeGroupService.privilegeGroupExits(identificationToCheck));
    }

    @GetMapping("/getPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<PrivilegeGroupDto> getPrivilegeGroup(@PathVariable String privilegeGroupIdentification) {
        return get(privilegeGroupIdentification, PrivilegeGroup.class
                , identificationToGet -> privilegeGroupService.findPrivilegeGroup(identificationToGet)
                , GroupTransportMapper::convertToPrivilegeGroupDto
        );
    }

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
}
