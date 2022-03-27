package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.domain.user.history.UserChange;
import de.ma_vin.ape.users.model.gen.dto.IBasicTransportable;
import de.ma_vin.ape.users.model.gen.dto.ITransportable;
import de.ma_vin.ape.users.model.gen.dto.group.UserIdRoleDto;
import de.ma_vin.ape.users.model.gen.dto.group.UserRoleDto;
import de.ma_vin.ape.users.model.gen.dto.group.part.UserRolePartDto;
import de.ma_vin.ape.users.model.gen.dto.history.ChangeDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.users.model.gen.dto.user.part.UserPartDto;
import de.ma_vin.ape.users.model.gen.mapper.UserPartTransportMapper;
import de.ma_vin.ape.users.model.gen.mapper.UserTransportMapper;
import de.ma_vin.ape.users.model.mapper.ChangeTransportMapper;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.users.service.history.UserChangeService;
import de.ma_vin.ape.utils.controller.response.ResponseUtil;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import de.ma_vin.ape.utils.controller.response.Status;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static de.ma_vin.ape.utils.controller.response.ResponseUtil.*;

@RestController
@RequestMapping(path = "user")
@Data
public class UserController extends AbstractDefaultOperationController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserChangeService userChangeService;

    @PreAuthorize("isContributor(#commonGroupIdentification, 'COMMON')")
    @PostMapping("/createUser")
    public @ResponseBody
    ResponseWrapper<UserDto> createUser(Principal principal, @RequestParam String firstName, @RequestParam String lastName, @RequestParam String commonGroupIdentification) {
        Optional<User> result = userService.saveAtCommonGroup(new UserExt(firstName, lastName, Role.VISITOR), commonGroupIdentification, principal.getName());
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The user with name \"%s, %s\" was not created", lastName, firstName));
        }
        return ResponseUtil.createSuccessResponse(UserTransportMapper.convertToUserDto(result.get()));
    }

    @PreAuthorize("isAdmin(#userIdentification, 'USER') or (isManager(#userIdentification, 'USER') and hasPrincipalHigherPrivilege(#userIdentification))")
    @DeleteMapping("/deleteUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteUser(Principal principal, @PathVariable String userIdentification) {
        return delete(userIdentification, User.class
                , getNonGlobalAdminSearcher()
                , objectToDelete -> userService.delete(objectToDelete, principal.getName())
                , identificationToCheck -> userService.userExits(identificationToCheck));
    }

    @PreAuthorize("isVisitor(#userIdentification, 'USER')")
    @GetMapping("/getUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<UserDto> getUser(@PathVariable String userIdentification) {
        return get(userIdentification, User.class
                , getNonGlobalAdminSearcher()
                , UserTransportMapper::convertToUserDto
        );
    }

    @PreAuthorize("isPrincipalItself(#userIdentification) or isAdmin(#userIdentification, 'USER') or (isManager(#userIdentification, 'USER') and hasPrincipalHigherPrivilege(#userIdentification))")
    @PutMapping("/updateUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<UserDto> updateUser(Principal principal, @RequestBody UserDto user, @PathVariable String userIdentification) {
        return update(user, userIdentification, User.class
                , getNonGlobalAdminSearcher()
                , (stored, modified) -> {
                    modified.setRole(stored.getRole());
                    return modified;
                }
                , objectToUpdate -> userService.save(objectToUpdate, principal.getName())
                , UserTransportMapper::convertToUserDto
                , UserTransportMapper::convertToUser
        );
    }

    @PreAuthorize("isPrincipalItself(#userIdentification) or isAdmin(#userIdentification, 'USER')")
    @PatchMapping("/setUserPassword/{userIdentification}")
    public ResponseWrapper<Boolean> setUserPassword(Principal principal, @PathVariable String userIdentification, @RequestBody String rawPassword) {
        if (userService.setPassword(userIdentification, rawPassword, false, principal.getName())) {
            return createSuccessResponse(Boolean.TRUE);
        }
        return createEmptyResponseWithError(String.format("The password could not be set at user with identification %s", userIdentification));
    }

    @PreAuthorize("isAdmin(#userIdentification, 'USER')")
    @PatchMapping("/setUserRole/{userIdentification}")
    public ResponseWrapper<Boolean> setUserRole(Principal principal, @PathVariable String userIdentification, @RequestBody Role role) {
        if (userService.setRole(userIdentification, role, principal.getName())) {
            return createSuccessResponse(Boolean.TRUE);
        }
        return createEmptyResponseWithError(String.format("The role could not be set at user with identification %s", userIdentification));
    }

    private Searcher<User> getNonGlobalAdminSearcher() {
        return identificationToDelete -> {
            Optional<User> searchResult = userService.findUser(identificationToDelete);
            return searchResult.isPresent() && !searchResult.get().isGlobalAdmin() ? searchResult : Optional.empty();
        };
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/countUsers/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countUsers(@PathVariable String commonGroupIdentification) {
        return createSuccessResponse(userService.countUsersAtCommonGroup(commonGroupIdentification));
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/getAllUsers/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserDto>> getAllUsers(@PathVariable String commonGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllUsers(commonGroupIdentification, page, size, UserTransportMapper::convertToUserDto);
    }

    @PreAuthorize("isVisitor(#commonGroupIdentification, 'COMMON')")
    @GetMapping("/getAllUserParts/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserPartDto>> getAllUserParts(@PathVariable String commonGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllUsers(commonGroupIdentification, page, size, UserPartTransportMapper::convertToUserPartDto);
    }

    /**
     * Loads all users and converts them to a wrapped list of {@code T} elements
     *
     * @param commonGroupIdentification the parent common group
     * @param page                      zero-based page index, must not be negative.
     * @param size                      the size of the page to be returned, must be greater than 0.
     * @param mapper                    a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                       the transport model
     * @return a wrapped list of loaded users
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllUsers(String commonGroupIdentification
            , Integer page, Integer size, Function<User, T> mapper) {

        return getAllSubElements(commonGroupIdentification, page, size
                , identification -> userService.findAllUsersAtCommonGroup(identification)
                , (identification, pageToUse, sizeToUse) -> userService.findAllUsersAtCommonGroup(identification, pageToUse, sizeToUse)
                , mapper);
    }

    @PreAuthorize("isVisitor(#userIdentification, 'USER')")
    @GetMapping("/getUserHistory/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<List<ChangeDto>> getUserHistory(@PathVariable String userIdentification) {
        Optional<User> user = getNonGlobalAdminSearcher().find(userIdentification);
        if (user.isEmpty()) {
            return createEmptyResponseWithError(String.format("There cannot be any user history for an admin %s", userIdentification));
        }
        List<UserChange> changes = userChangeService.loadChanges(userIdentification);
        if (changes.isEmpty()) {
            return createResponseWithWarning(Collections.emptyList(), String.format("No changes were found for user %s, but at least one creation should exist at history", userIdentification));
        }
        return createSuccessResponse(changes.stream().map(ChangeTransportMapper::convertToChangeDto).toList());
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PatchMapping("/addUserToPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> addUserToPrivilegeGroup(Principal principal, @PathVariable String privilegeGroupIdentification, @RequestBody UserIdRoleDto userRole) {
        Optional<User> storedUser = userService.findUser(userRole.getUserIdentification());
        if (storedUser.isPresent() && storedUser.get().isGlobalAdmin()) {
            return createEmptyResponseWithError(String.format("The user \"%s\" is an global admin and could not be added to an privilege group"
                    , userRole.getUserIdentification()));
        }

        boolean result = userService.addUserToPrivilegeGroup(privilegeGroupIdentification, userRole.getUserIdentification(), userRole.getRole(), principal.getName());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not added with role %s to privilege group with identification \"%s\""
                , userRole.getUserIdentification(), userRole.getRole().getDescription(), privilegeGroupIdentification));
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PatchMapping("/removeUserFromPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeUserFromPrivilegeGroup(Principal principal, @PathVariable String privilegeGroupIdentification, @RequestBody String userIdentification) {
        boolean result = userService.removeUserFromPrivilegeGroup(privilegeGroupIdentification, userIdentification, principal.getName());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not removed from privilege group with identification \"%s\""
                , userIdentification, privilegeGroupIdentification));
    }

    @PreAuthorize("isContributor(#baseGroupIdentification, 'BASE')")
    @PatchMapping("/addUserToBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> addUserToBaseGroup(Principal principal, @PathVariable String baseGroupIdentification, @RequestBody String userIdentification) {
        Optional<User> storedUser = userService.findUser(userIdentification);
        if (storedUser.isPresent() && storedUser.get().isGlobalAdmin()) {
            return createEmptyResponseWithError(String.format("The user \"%s\" is an global admin and could not be added to an privilege group", userIdentification));
        }

        boolean result = userService.addUserToBaseGroup(baseGroupIdentification, userIdentification, principal.getName());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not added to base group with identification \"%s\""
                , userIdentification, baseGroupIdentification));
    }

    @PreAuthorize("isContributor(#baseGroupIdentification, 'BASE')")
    @PatchMapping("/removeUserFromBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeUserFromBaseGroup(Principal principal, @PathVariable String baseGroupIdentification, @RequestBody String userIdentification) {
        boolean result = userService.removeUserFromBaseGroup(baseGroupIdentification, userIdentification, principal.getName());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not removed from base group with identification \"%s\""
                , userIdentification, baseGroupIdentification));
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/countUsersAtBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countUsersAtBaseGroup(@PathVariable String baseGroupIdentification) {
        return createSuccessResponse(userService.countUsersAtBaseGroup(baseGroupIdentification));
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/getAllUsersFromBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserDto>> getAllUsersFromBaseGroup(@PathVariable String baseGroupIdentification
            , @RequestParam(required = false) Boolean dissolveSubgroups
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllUsersFromBaseGroup(baseGroupIdentification, dissolveSubgroups, page, size, UserTransportMapper::convertToUserDto);
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/getAllUserPartsFromBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserPartDto>> getAllUserPartsFromBaseGroup(@PathVariable String baseGroupIdentification
            , @RequestParam(required = false) Boolean dissolveSubgroups
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllUsersFromBaseGroup(baseGroupIdentification, dissolveSubgroups, page, size, UserPartTransportMapper::convertToUserPartDto);
    }

    /**
     * Loads all users and converts them to a wrapped list of {@code T} elements
     *
     * @param baseGroupIdentification the parent base group
     * @param dissolveSubgroups       indicator if the users of subgroups should also be added
     * @param page                    zero-based page index, must not be negative.
     * @param size                    the size of the page to be returned, must be greater than 0.
     * @param mapper                  a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                     the transport model
     * @return a wrapped list of loaded users
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllUsersFromBaseGroup(String baseGroupIdentification
            , Boolean dissolveSubgroups, Integer page, Integer size, Function<User, T> mapper) {

        ResponseWrapper<List<T>> responseWrapper = getAllSubElements(baseGroupIdentification, page, size
                , identification -> userService.findAllUsersAtBaseGroup(identification, Boolean.TRUE.equals(dissolveSubgroups))
                , (identification, pageToUse, sizeToUse) -> userService.findAllUsersAtBaseGroup(identification, pageToUse, sizeToUse)
                , mapper);

        if (Boolean.TRUE.equals(dissolveSubgroups) && (page != null || size != null)) {
            responseWrapper.addMessage("Dissolving subgroups is not available while using pages", Status.WARN);
        }
        return responseWrapper;
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/countAvailableUsersForBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countAvailableUsersForBaseGroup(@PathVariable String baseGroupIdentification) {
        return createSuccessResponse(userService.countAvailableUsersForBaseGroup(baseGroupIdentification));
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/getAvailableUsersForBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserDto>> getAvailableUsersForBaseGroup(@PathVariable String baseGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAvailableUsersForBaseGroup(baseGroupIdentification, page, size, UserTransportMapper::convertToUserDto);
    }

    @PreAuthorize("isVisitor(#baseGroupIdentification, 'BASE')")
    @GetMapping("/getAvailableUserPartsForBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserPartDto>> getAvailableUserPartsForBaseGroup(@PathVariable String baseGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAvailableUsersForBaseGroup(baseGroupIdentification, page, size, UserPartTransportMapper::convertToUserPartDto);
    }

    /**
     * Loads all users who are not added to the base group yet and converts them to a wrapped list of {@code T} elements
     *
     * @param baseGroupIdentification the parent base group
     * @param page                    zero-based page index, must not be negative.
     * @param size                    the size of the page to be returned, must be greater than 0.
     * @param mapper                  a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                     the transport model
     * @return a wrapped list of loaded users
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAvailableUsersForBaseGroup(String baseGroupIdentification
            , Integer page, Integer size, Function<User, T> mapper) {

        return getAllSubElements(baseGroupIdentification, page, size
                , identification -> userService.findAllAvailableUsersForBaseGroup(identification)
                , (identification, pageToUse, sizeToUse) -> userService.findAllAvailableUsersForBaseGroup(identification, pageToUse, sizeToUse)
                , mapper);
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/countUsersAtPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countUsersAtPrivilegeGroup(@PathVariable String privilegeGroupIdentification, @RequestParam(required = false) Role role) {
        return createSuccessResponse(userService.countUsersAtPrivilegeGroup(privilegeGroupIdentification, role));
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getAllUsersFromPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserRoleDto>> getAllUsersFromPrivilegeGroup(@PathVariable String privilegeGroupIdentification
            , @RequestParam(required = false) Boolean dissolveSubgroups, @RequestParam(required = false) Role role
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllUsersFromPrivilegeGroup(privilegeGroupIdentification, dissolveSubgroups, role, page, size
                , (u, r) -> {
                    UserRoleDto userRoleDto = new UserRoleDto();
                    userRoleDto.setRole(r);
                    userRoleDto.setUser(UserTransportMapper.convertToUserDto(u));
                    return userRoleDto;
                });
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getAllUserPartsFromPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserRolePartDto>> getAllUserPartsFromPrivilegeGroup(@PathVariable String privilegeGroupIdentification
            , @RequestParam(required = false) Boolean dissolveSubgroups, @RequestParam(required = false) Role role
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllUsersFromPrivilegeGroup(privilegeGroupIdentification, dissolveSubgroups, role, page, size
                , (u, r) -> {
                    UserRolePartDto userRoleDto = new UserRolePartDto();
                    userRoleDto.setRole(r);
                    userRoleDto.setUser(UserPartTransportMapper.convertToUserPartDto(u));
                    return userRoleDto;
                });
    }

    /**
     * Loads all users and converts them to a wrapped list of {@code T} elements
     *
     * @param privilegeGroupIdentification the parent privilege group
     * @param dissolveSubgroups            indicator if the users of subgroups should also be added
     * @param role                         role of the users. If null or Role.NOT_RELEVANT all roles will be loaded
     * @param page                         zero-based page index, must not be negative.
     * @param size                         the size of the page to be returned, must be greater than 0.
     * @param mapper                       a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                          the transport model
     * @return a wrapped list of loaded users
     */
    private <T extends IBasicTransportable> ResponseWrapper<List<T>> getAllUsersFromPrivilegeGroup(String privilegeGroupIdentification
            , Boolean dissolveSubgroups, Role role, Integer page, Integer size, UserRoleMapper<T> mapper) {

        int pageToUse = page == null ? DEFAULT_PAGE : page;
        int sizeToUse = size == null ? DEFAULT_SIZE : size;

        Map<Role, List<User>> rolesWithUsers = page == null && size == null
                ? userService.findAllUsersAtPrivilegeGroup(privilegeGroupIdentification, role, Boolean.TRUE.equals(dissolveSubgroups))
                : userService.findAllUsersAtPrivilegeGroup(privilegeGroupIdentification, role, pageToUse, sizeToUse);

        List<T> result = rolesWithUsers.entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(u -> mapper.map(u, e.getKey())))
                .toList();

        ResponseWrapper<List<T>> responseWrapper = createPageableResponse(result, page, size);
        if (Boolean.TRUE.equals(dissolveSubgroups) && (page != null || size != null)) {
            responseWrapper.addMessage("Dissolving subgroups is not available while using pages", Status.WARN);
        }
        return responseWrapper;
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/countAvailableUsersForPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countAvailableUsersForPrivilegeGroup(@PathVariable String privilegeGroupIdentification) {
        return createSuccessResponse(userService.countAvailableUsersForPrivilegeGroup(privilegeGroupIdentification));
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getAvailableUsersForPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserDto>> getAvailableUsersForPrivilegeGroup(@PathVariable String privilegeGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAvailableUsersForPrivilegeGroup(privilegeGroupIdentification, page, size, UserTransportMapper::convertToUserDto);
    }

    @PreAuthorize("isVisitor(#privilegeGroupIdentification, 'PRIVILEGE')")
    @GetMapping("/getAvailableUserPartsForPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserPartDto>> getAvailableUserPartsForPrivilegeGroup(@PathVariable String privilegeGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAvailableUsersForPrivilegeGroup(privilegeGroupIdentification, page, size, UserPartTransportMapper::convertToUserPartDto);
    }

    /**
     * Loads all users who are not added to the privilege group yet and converts them to a wrapped list of {@code T} elements
     *
     * @param privilegeGroupIdentification the privilege group
     * @param page                         zero-based page index, must not be negative.
     * @param size                         the size of the page to be returned, must be greater than 0.
     * @param mapper                       a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                          the transport model
     * @return a wrapped list of loaded users
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAvailableUsersForPrivilegeGroup(String privilegeGroupIdentification
            , Integer page, Integer size, Function<User, T> mapper) {

        return getAllSubElements(privilegeGroupIdentification, page, size
                , identification -> userService.findAllAvailableUsersForPrivilegeGroup(identification)
                , (identification, pageToUse, sizeToUse) -> userService.findAllAvailableUsersForPrivilegeGroup(identification, pageToUse, sizeToUse)
                , mapper);
    }

    @FunctionalInterface
    private interface UserRoleMapper<T extends IBasicTransportable> {
        T map(User user, Role role);
    }
}
