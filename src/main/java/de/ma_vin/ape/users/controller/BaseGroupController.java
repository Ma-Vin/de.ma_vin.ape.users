package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.group.BaseGroupExt;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.history.BaseGroupChange;
import de.ma_vin.ape.users.model.gen.dto.ITransportable;
import de.ma_vin.ape.users.model.gen.dto.group.BaseGroupDto;
import de.ma_vin.ape.users.model.gen.dto.group.BaseGroupIdRoleDto;
import de.ma_vin.ape.users.model.gen.dto.group.part.BaseGroupPartDto;
import de.ma_vin.ape.users.model.gen.dto.history.ChangeDto;
import de.ma_vin.ape.users.model.gen.mapper.GroupPartTransportMapper;
import de.ma_vin.ape.users.model.gen.mapper.GroupTransportMapper;
import de.ma_vin.ape.users.model.mapper.ChangeTransportMapper;
import de.ma_vin.ape.users.service.BaseGroupService;
import de.ma_vin.ape.users.service.history.BaseGroupChangeService;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.ma_vin.ape.utils.controller.response.ResponseUtil.*;

@RestController
@RequestMapping(path = "group/base")
@Log4j2
@Data
public class BaseGroupController extends AbstractDefaultOperationController {

    @Autowired
    private BaseGroupService baseGroupService;
    @Autowired
    private BaseGroupChangeService baseGroupChangeService;

    @PreAuthorize("isManager(#commonGroupIdentification, 'COMMON')")
    @PostMapping("/createBaseGroup")
    public @ResponseBody
    ResponseWrapper<BaseGroupDto> createBaseGroup(Principal principal, @RequestParam String groupName, @RequestParam String commonGroupIdentification) {
        Optional<BaseGroup> result = baseGroupService.save(new BaseGroupExt(groupName), commonGroupIdentification, principal.getName());
        if (result.isEmpty()) {
            return createEmptyResponseWithError(String.format("The group with name \"%s\" was not created", groupName));
        }
        return createSuccessResponse(GroupTransportMapper.convertToBaseGroupDto(result.get()));
    }

    @PreAuthorize("isManager(#baseGroupIdentification, 'BASE')")
    @DeleteMapping("/deleteBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteBaseGroup(Principal principal, @PathVariable String baseGroupIdentification) {
        return delete(baseGroupIdentification, BaseGroup.class
                , identificationToDelete -> baseGroupService.findBaseGroup(identificationToDelete)
                , objectToDelete -> baseGroupService.delete(objectToDelete, principal.getName())
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
    ResponseWrapper<BaseGroupDto> updateBaseGroup(Principal principal, @RequestBody BaseGroupDto baseGroup, @PathVariable String baseGroupIdentification) {
        return update(baseGroup, baseGroupIdentification, BaseGroup.class
                , identificationToUpdate -> baseGroupService.findBaseGroup(identificationToUpdate)
                , objectToUpdate -> baseGroupService.save(objectToUpdate, principal.getName())
                , GroupTransportMapper::convertToBaseGroupDto
                , GroupTransportMapper::convertToBaseGroup
        );
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/countBaseGroups/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countBaseGroups(@PathVariable String commonGroupIdentification) {
        return createSuccessResponse(baseGroupService.countBaseGroups(commonGroupIdentification));
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/getAllBaseGroups/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupDto>> getAllBaseGroups(@PathVariable String commonGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllBaseGroups(commonGroupIdentification, page, size, GroupTransportMapper::convertToBaseGroupDto);
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/getAllBaseGroupParts/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupPartDto>> getAllBaseGroupParts(@PathVariable String commonGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllBaseGroups(commonGroupIdentification, page, size, GroupPartTransportMapper::convertToBaseGroupPartDto);
    }

    /**
     * Loads all base groups and converts them to a wrapped list of {@code T} elements
     *
     * @param commonGroupIdentification the parent common group
     * @param page                      zero-based page index, must not be negative.
     * @param size                      the size of the page to be returned, must be greater than 0.
     * @param mapper                    a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                       the transport model
     * @return a wrapped list of loaded base groups
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllBaseGroups(String commonGroupIdentification
            , Integer page, Integer size, Function<BaseGroup, T> mapper) {

        return getAllSubElements(commonGroupIdentification, page, size
                , identification -> baseGroupService.findAllBaseGroups(identification)
                , (identification, pageToUse, sizeToUse) -> baseGroupService.findAllBaseGroups(identification, pageToUse, sizeToUse)
                , mapper);
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/getBaseGroupHistory/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<ChangeDto>> getBaseGroupHistory(@PathVariable String baseGroupIdentification) {
        List<BaseGroupChange> changes = baseGroupChangeService.loadChanges(baseGroupIdentification);
        if (changes.isEmpty()) {
            return createResponseWithWarning(Collections.emptyList(), String.format(NO_CHANGES_FOUND_WARNING_TEXT, "base group", baseGroupIdentification));
        }
        return createSuccessResponse(changes.stream().map(ChangeTransportMapper::convertToChangeDto).toList());
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PatchMapping("/addBaseToPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> addBaseToPrivilegeGroup(Principal principal, @PathVariable String privilegeGroupIdentification, @RequestBody BaseGroupIdRoleDto baseGroupRole) {
        boolean result = baseGroupService.addBaseToPrivilegeGroup(privilegeGroupIdentification, baseGroupRole.getBaseGroupIdentification(), baseGroupRole.getRole(), principal.getName());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The base group with identification \"%s\" was not added with role %s to privilege group with identification \"%s\""
                , baseGroupRole.getBaseGroupIdentification(), baseGroupRole.getRole().getDescription(), privilegeGroupIdentification));
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PatchMapping("/removeBaseFromPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeBaseFromPrivilegeGroup(Principal principal, @PathVariable String privilegeGroupIdentification, @RequestBody String baseGroupIdentification) {
        boolean result = baseGroupService.removeBaseFromPrivilegeGroup(privilegeGroupIdentification, baseGroupIdentification, principal.getName());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The base group with identification \"%s\" was not removed from privilege group with identification \"%s\""
                , baseGroupIdentification, privilegeGroupIdentification));
    }

    @PreAuthorize("isVisitor(#parentGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/countBaseAtPrivilegeGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countBaseAtPrivilegeGroup(@PathVariable String parentGroupIdentification, @RequestParam(required = false) Role role) {
        return createSuccessResponse(baseGroupService.countBasesAtPrivilegeGroup(parentGroupIdentification, role));
    }

    @PreAuthorize("isVisitor(#parentGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getAllBaseAtPrivilegeGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupDto>> getAllBaseAtPrivilegeGroup(@PathVariable String parentGroupIdentification, @RequestParam(required = false) Role role
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllBaseAtPrivilegeGroup(parentGroupIdentification, role, page, size, GroupTransportMapper::convertToBaseGroupDto);
    }

    @PreAuthorize("isVisitor(#parentGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getAllBasePartAtPrivilegeGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupPartDto>> getAllBasePartAtPrivilegeGroup(@PathVariable String parentGroupIdentification, @RequestParam(required = false) Role role
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllBaseAtPrivilegeGroup(parentGroupIdentification, role, page, size, GroupPartTransportMapper::convertToBaseGroupPartDto);
    }

    /**
     * Loads all base groups at a privilege one and converts them to a wrapped list of {@code T} elements
     *
     * @param privilegeGroupIdentification the parent privilege group
     * @param role                         role of base groups at privilege group. If the role is given, the result is filtered by this role
     * @param page                         zero-based page index, must not be negative.
     * @param size                         the size of the page to be returned, must be greater than 0.
     * @param mapper                       a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                          the transport model
     * @return a wrapped list of loaded base groups
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllBaseAtPrivilegeGroup(String privilegeGroupIdentification
            , Role role, Integer page, Integer size, Function<BaseGroup, T> mapper) {

        return getAllSubElements(privilegeGroupIdentification, page, size
                , identification -> baseGroupService.findAllBaseAtPrivilegeGroup(identification, role)
                , (identification, pageToUse, sizeToUse) -> baseGroupService.findAllBaseAtPrivilegeGroup(identification, role, pageToUse, sizeToUse)
                , mapper);
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/countAvailableBasesForPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countAvailableBasesForPrivilegeGroup(@PathVariable String privilegeGroupIdentification) {
        return createSuccessResponse(baseGroupService.countAvailableBasesForPrivilegeGroup(privilegeGroupIdentification));
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getAllAvailableBasesForPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupDto>> getAllAvailableBasesForPrivilegeGroup(@PathVariable String privilegeGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllAvailableBasesForPrivilegeGroup(privilegeGroupIdentification, page, size, GroupTransportMapper::convertToBaseGroupDto);
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getAllAvailableBasePartsForPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupPartDto>> getAllAvailableBasePartsForPrivilegeGroup(@PathVariable String privilegeGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllAvailableBasesForPrivilegeGroup(privilegeGroupIdentification, page, size, GroupPartTransportMapper::convertToBaseGroupPartDto);
    }

    /**
     * Loads all base groups which are not added to the privilege group yet and converts them to a wrapped list of {@code T} elements
     *
     * @param privilegeGroupIdentification the privilege group
     * @param page                         zero-based page index, must not be negative.
     * @param size                         the size of the page to be returned, must be greater than 0.
     * @param mapper                       a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                          the transport model
     * @return a wrapped list of loaded base groups
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllAvailableBasesForPrivilegeGroup(String privilegeGroupIdentification
            , Integer page, Integer size, Function<BaseGroup, T> mapper) {

        return getAllSubElements(privilegeGroupIdentification, page, size
                , identification -> baseGroupService.findAllAvailableBasesForPrivilegeGroup(identification)
                , (identification, pageToUse, sizeToUse) -> baseGroupService.findAllAvailableBasesForPrivilegeGroup(identification, pageToUse, sizeToUse)
                , mapper);
    }

    @PreAuthorize("isContributor(#parentGroupIdentification, 'BASE')")
    @PatchMapping("/addBaseToBaseGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> addBaseToBaseGroup(Principal principal, @PathVariable String parentGroupIdentification, @RequestBody String baseGroupIdentification) {
        boolean result = baseGroupService.addBaseToBaseGroup(parentGroupIdentification, baseGroupIdentification, principal.getName());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The base group with identification \"%s\" was not added base group with identification \"%s\""
                , baseGroupIdentification, parentGroupIdentification));
    }

    @PreAuthorize("isContributor(#parentGroupIdentification, 'BASE')")
    @PatchMapping("/removeBaseFromBaseGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeBaseFromBaseGroup(Principal principal, @PathVariable String parentGroupIdentification, @RequestBody String baseGroupIdentification) {
        boolean result = baseGroupService.removeBaseFromBaseGroup(parentGroupIdentification, baseGroupIdentification, principal.getName());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The base group with identification \"%s\" was not removed from base group with identification \"%s\""
                , baseGroupIdentification, parentGroupIdentification));
    }

    @PreAuthorize("isVisitor(#parentGroupIdentification, 'BASE')")
    @GetMapping("/countBaseAtBaseGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countBaseAtBaseGroup(@PathVariable String parentGroupIdentification) {
        return createSuccessResponse(baseGroupService.countBasesAtBaseGroup(parentGroupIdentification));
    }

    @PreAuthorize("isVisitor(#parentGroupIdentification, 'BASE')")
    @GetMapping("/getAllBaseAtBaseGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupDto>> getAllBaseAtBaseGroup(@PathVariable String parentGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllBaseAtBaseGroup(parentGroupIdentification, page, size, GroupTransportMapper::convertToBaseGroupDto);
    }

    @PreAuthorize("isVisitor(#parentGroupIdentification, 'BASE')")
    @GetMapping("/getAllBasePartAtBaseGroup/{parentGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupPartDto>> findAllBasePartAtBaseGroup(@PathVariable String parentGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllBaseAtBaseGroup(parentGroupIdentification, page, size, GroupPartTransportMapper::convertToBaseGroupPartDto);
    }

    /**
     * Loads all base groups at a base one and converts them to a wrapped list of {@code T} elements
     *
     * @param baseGroupIdentification the parent privilege group
     * @param page                    zero-based page index, must not be negative.
     * @param size                    the size of the page to be returned, must be greater than 0.
     * @param mapper                  a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                     the transport model
     * @return a wrapped list of loaded base groups
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllBaseAtBaseGroup(String baseGroupIdentification
            , Integer page, Integer size, Function<BaseGroup, T> mapper) {

        return getAllSubElements(baseGroupIdentification, page, size
                , identification -> baseGroupService.findAllBasesAtBaseGroup(identification)
                , (identification, pageToUse, sizeToUse) -> baseGroupService.findAllBasesAtBaseGroup(identification, pageToUse, sizeToUse)
                , mapper);
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/countAvailableBasesForBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countAvailableBasesForBaseGroup(@PathVariable String baseGroupIdentification) {
        return createSuccessResponse(baseGroupService.countAvailableBasesForBaseGroup(baseGroupIdentification));
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/getAllAvailableBasesForBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupDto>> getAllAvailableBasesForBaseGroup(@PathVariable String baseGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllAvailableBasesForBaseGroup(baseGroupIdentification, page, size, GroupTransportMapper::convertToBaseGroupDto);
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/getAllAvailableBasePartsForBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<BaseGroupPartDto>> getAllAvailableBasePartsForBaseGroup(@PathVariable String baseGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllAvailableBasesForBaseGroup(baseGroupIdentification, page, size, GroupPartTransportMapper::convertToBaseGroupPartDto);
    }

    /**
     * Loads all base groups which are not added to the base group yet and converts them to a wrapped list of {@code T} elements
     *
     * @param baseGroupIdentification the base group
     * @param page                    zero-based page index, must not be negative.
     * @param size                    the size of the page to be returned, must be greater than 0.
     * @param mapper                  a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                     the transport model
     * @return a wrapped list of loaded base groups
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllAvailableBasesForBaseGroup(String baseGroupIdentification
            , Integer page, Integer size, Function<BaseGroup, T> mapper) {

        return getAllSubElements(baseGroupIdentification, page, size
                , identification -> baseGroupService.findAllAvailableBasesForBaseGroup(identification)
                , (identification, pageToUse, sizeToUse) -> baseGroupService.findAllAvailableBasesForBaseGroup(identification, pageToUse, sizeToUse)
                , mapper);
    }
}
