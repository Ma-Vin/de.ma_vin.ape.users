package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.model.domain.group.CommonGroupExt;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.dto.ITransportable;
import de.ma_vin.ape.users.model.gen.dto.group.CommonGroupDto;
import de.ma_vin.ape.users.model.gen.dto.group.part.CommonGroupPartDto;
import de.ma_vin.ape.users.model.gen.mapper.GroupPartTransportMapper;
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

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
    ResponseWrapper<CommonGroupDto> createCommonGroup(Principal principal, @RequestParam String groupName) {
        Optional<CommonGroup> result = commonGroupService.save(new CommonGroupExt(groupName), principal.getName());
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The group with name \"%s\" was not created", groupName));
        }
        return ResponseUtil.createSuccessResponse(GroupTransportMapper.convertToCommonGroupDto(result.get()));
    }

    @PreAuthorize("isGlobalAdmin()")
    @DeleteMapping("/deleteCommonGroup/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteCommonGroup(Principal principal, @PathVariable String commonGroupIdentification) {
        return delete(commonGroupIdentification, CommonGroup.class
                , identificationToDelete -> commonGroupService.findCommonGroup(identificationToDelete)
                , objectToDelete -> commonGroupService.delete(objectToDelete, principal.getName())
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

        return getAllCommonGroups(page, size, GroupTransportMapper::convertToCommonGroupDto);
    }

    @PreAuthorize("isGlobalAdmin()")
    @GetMapping("/getAllCommonGroupParts")
    public @ResponseBody
    ResponseWrapper<List<CommonGroupPartDto>> getAllCommonGroupParts(@RequestParam(required = false) Integer page
            , @RequestParam(required = false) Integer size) {

        return getAllCommonGroups(page, size, GroupPartTransportMapper::convertToCommonGroupPartDto);
    }

    /**
     * Loads all common groups and converts them to a wrapped list of {@code T} elements
     *
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @param mapper a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>    the transport model
     * @return a wrapped list of loaded common groups
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllCommonGroups(Integer page, Integer size, Function<CommonGroup, T> mapper) {

        return getAllSubElements(null, page, size
                , identification -> commonGroupService.findAllCommonGroups()
                , (identification, pageToUse, sizeToUse) -> commonGroupService.findAllCommonGroups(pageToUse, sizeToUse)
                , mapper);
    }

    @PreAuthorize("isAdmin(#commonGroupIdentification, 'COMMON')")
    @PutMapping("/updateCommonGroup/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<CommonGroupDto> updateCommonGroup(Principal principal, @RequestBody CommonGroupDto commonGroup, @PathVariable String commonGroupIdentification) {
        return update(commonGroup, commonGroupIdentification, CommonGroup.class
                , identificationToUpdate -> commonGroupService.findCommonGroup(identificationToUpdate)
                , objectToUpdate -> commonGroupService.save(objectToUpdate, principal.getName())
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
