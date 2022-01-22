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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "group/common")
@Log4j2
@Data
public class CommonGroupController extends AbstractDefaultOperationController {

    @Autowired
    private CommonGroupService commonGroupService;

    @PreAuthorize("isGlobalAdmin()")
    @PostMapping("/createCommonGroup")
    public @ResponseBody
    ResponseWrapper<CommonGroupDto> createCommonGroup(@RequestParam String groupName) {
        Optional<CommonGroup> result = commonGroupService.save(new CommonGroupExt(groupName));
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The group with name \"%s\" was not created", groupName));
        }
        return ResponseUtil.createSuccessResponse(GroupTransportMapper.convertToCommonGroupDto(result.get()));
    }

    @PreAuthorize("isGlobalAdmin()")
    @DeleteMapping("/deleteCommonGroup/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteCommonGroup(@PathVariable String commonGroupIdentification) {
        return delete(commonGroupIdentification, CommonGroup.class
                , identificationToDelete -> commonGroupService.findCommonGroup(identificationToDelete)
                , objectToDelete -> commonGroupService.delete(objectToDelete)
                , identificationToCheck -> commonGroupService.commonGroupExits(identificationToCheck));
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/getCommonGroup/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<CommonGroupDto> getCommonGroup(@PathVariable String commonGroupIdentification) {
        return get(commonGroupIdentification, CommonGroup.class
                , identificationToGet -> commonGroupService.findCommonGroup(identificationToGet)
                , GroupTransportMapper::convertToCommonGroupDto
        );
    }

    @PreAuthorize("isGlobalAdmin()")
    @GetMapping("/getAllCommonGroups")
    public @ResponseBody
    ResponseWrapper<List<CommonGroupDto>> getAllCommonGroups(@RequestParam(required = false) Integer page
            , @RequestParam(required = false) Integer size) {

        int pageToUse = page == null ? DEFAULT_PAGE : page;
        int sizeToUse = size == null ? DEFAULT_SIZE : size;

        List<CommonGroup> commonGroups = page == null && size == null
                ? commonGroupService.findAllCommonGroups()
                : commonGroupService.findAllCommonGroups(pageToUse, sizeToUse);

        List<CommonGroupDto> result = commonGroups.stream()
                .map(GroupTransportMapper::convertToCommonGroupDto)
                .collect(Collectors.toList());

        return createPageableResponse(result, page, size);
    }

    @PreAuthorize("isAdmin(#commonGroupIdentification, 'COMMON')")
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

    @PostAuthorize("returnObject.response!=null && isVisitor(returnObject.response.getIdentification(), 'COMMON')")
    @GetMapping("/getParentCommonGroupOfUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<CommonGroupDto> getParentCommonGroupOfUser(@PathVariable String userIdentification) {
        Optional<CommonGroup> result = commonGroupService.findParentCommonGroupOfUser(userIdentification);
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The parent common group of user \"%s\" was not found", userIdentification));
        }
        return ResponseUtil.createSuccessResponse(GroupTransportMapper.convertToCommonGroupDto(result.get()));
    }
}
