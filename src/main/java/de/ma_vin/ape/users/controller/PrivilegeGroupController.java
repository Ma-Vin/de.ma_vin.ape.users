package de.ma_vin.ape.users.controller;


import de.ma_vin.ape.users.model.domain.group.PrivilegeGroupExt;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.dto.ITransportable;
import de.ma_vin.ape.users.model.gen.dto.group.PrivilegeGroupDto;
import de.ma_vin.ape.users.model.gen.dto.group.part.PrivilegeGroupPartDto;
import de.ma_vin.ape.users.model.gen.mapper.GroupPartTransportMapper;
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
import java.util.function.Function;

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

        return getAllPrivilegeGroups(commonGroupIdentification, page, size, GroupTransportMapper::convertToPrivilegeGroupDto);
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/getAllPrivilegeGroupParts/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<PrivilegeGroupPartDto>> getAllPrivilegeGroupParts(@PathVariable String commonGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllPrivilegeGroups(commonGroupIdentification, page, size, GroupPartTransportMapper::convertToPrivilegeGroupPartDto);
    }

    /**
     * Loads all privilege groups and converts them to a wrapped list of {@code T} elements
     *
     * @param commonGroupIdentification the parent common group
     * @param page                      zero-based page index, must not be negative.
     * @param size                      the size of the page to be returned, must be greater than 0.
     * @param mapper                    a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                       the transport model
     * @return a wrapped list of loaded privilege groups
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllPrivilegeGroups(String commonGroupIdentification
            , Integer page, Integer size, Function<PrivilegeGroup, T> mapper) {

        return getAllSubElements(commonGroupIdentification, page, size
                , identification -> privilegeGroupService.findAllPrivilegeGroups(identification)
                , (identification, pageToUse, sizeToUse) -> privilegeGroupService.findAllPrivilegeGroups(identification, pageToUse, sizeToUse)
                , mapper);
    }
}
