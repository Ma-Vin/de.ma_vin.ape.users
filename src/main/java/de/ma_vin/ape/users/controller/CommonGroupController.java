package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.model.domain.group.CommonGroupExt;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.dto.group.CommonGroupDto;
import de.ma_vin.ape.users.model.gen.mapper.GroupTransportMapper;
import de.ma_vin.ape.users.service.CommonGroupService;
import de.ma_vin.ape.utils.controller.response.ResponseUtil;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(path = "group/common")
@Log4j2
@Data
public class CommonGroupController extends AbstractDefaultOperationController {

    @Autowired
    private CommonGroupService commonGroupService;

    // TODO: @PreAuthorize("hasAuthority('GlobalAdmin')")
    @PostMapping("/createCommonGroup")
    public @ResponseBody
    ResponseWrapper<CommonGroupDto> createCommonGroup(@RequestParam String groupName) {
        Optional<CommonGroup> result = commonGroupService.save(new CommonGroupExt(groupName));
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The group with name \"%s\" was not created", groupName));
        }
        return ResponseUtil.createSuccessResponse(GroupTransportMapper.convertToCommonGroupDto(result.get()));
    }

    @DeleteMapping("/deleteCommonGroup/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteCommonGroup(@PathVariable String commonGroupIdentification) {
        return delete(commonGroupIdentification, CommonGroup.class
                , identificationToDelete -> commonGroupService.findCommonGroup(identificationToDelete)
                , objectToDelete -> commonGroupService.delete(objectToDelete)
                , identificationToCheck -> commonGroupService.commonGroupExits(identificationToCheck));
    }

    @GetMapping("/getCommonGroup/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<CommonGroupDto> getCommonGroup(@PathVariable String commonGroupIdentification) {
        return get( commonGroupIdentification, CommonGroup.class
                , identificationToGet -> commonGroupService.findCommonGroup(identificationToGet)
                , GroupTransportMapper::convertToCommonGroupDto
        );
    }

    @PutMapping("/updateCommonGroup/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<CommonGroupDto> updateCommonGroup(@RequestBody CommonGroupDto commonGroup, @PathVariable String commonGroupIdentification) {
        return update(commonGroup, commonGroupIdentification, CommonGroup.class
                , identificationToUpdate -> commonGroupService.findCommonGroup(identificationToUpdate)
                , objectToUpdate -> commonGroupService.save(objectToUpdate)
                , GroupTransportMapper::convertToCommonGroupDto
                , GroupTransportMapper::convertToCommonGroup
        );
    }
}
