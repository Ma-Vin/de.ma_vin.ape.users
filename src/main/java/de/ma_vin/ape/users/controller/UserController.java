package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.dto.group.UserIdRoleDto;
import de.ma_vin.ape.users.model.gen.dto.group.UserRoleDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.users.model.gen.mapper.UserTransportMapper;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.utils.controller.response.ResponseUtil;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import de.ma_vin.ape.utils.controller.response.Status;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static de.ma_vin.ape.utils.controller.response.ResponseUtil.*;

@RestController
@RequestMapping(path = "user")
@Data
public class UserController extends AbstractDefaultOperationController {

    @Autowired
    private UserService userService;
    @Autowired
    private PrivilegeGroupService privilegeGroupService;

    @PreAuthorize("isContributor(#commonGroupIdentification, 'COMMON')")
    @PostMapping("/createUser")
    public @ResponseBody
    ResponseWrapper<UserDto> createUser(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String commonGroupIdentification) {
        Optional<User> result = userService.saveAtCommonGroup(new UserExt(firstName, lastName, Role.VISITOR), commonGroupIdentification);
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The user with name \"%s, %s\" was not created", lastName, firstName));
        }
        return ResponseUtil.createSuccessResponse(UserTransportMapper.convertToUserDto(result.get()));
    }

    @PreAuthorize("isManager(#userIdentification, 'USER')")
    @DeleteMapping("/deleteUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteUser(@PathVariable String userIdentification) {
        return delete(userIdentification, User.class
                , getNonGlobalAdminSearcher()
                , objectToDelete -> userService.delete(objectToDelete)
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

    @PreAuthorize("isPrincipalItself(#userIdentification) or isManager(#userIdentification, 'USER')")
    @PutMapping("/updateUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<UserDto> updateUser(@RequestBody UserDto user, @PathVariable String userIdentification) {
        return update(user, userIdentification, User.class
                , getNonGlobalAdminSearcher()
                , (stored, modified) -> {
                    modified.setRole(stored.getRole());
                    return modified;
                }
                , objectToUpdate -> userService.save(objectToUpdate)
                , UserTransportMapper::convertToUserDto
                , UserTransportMapper::convertToUser
        );
    }

    @PreAuthorize("isPrincipalItself(#userIdentification) or isAdmin(#userIdentification, 'USER')")
    @PatchMapping("/setUserPassword/{userIdentification}")
    public ResponseWrapper<Boolean> setUserPassword(@PathVariable String userIdentification, @RequestBody String rawPassword) {
        if (userService.setPassword(userIdentification, rawPassword, false)) {
            return createSuccessResponse(Boolean.TRUE);
        }
        return createEmptyResponseWithError(String.format("The password could not be set at user with identification %s", userIdentification));
    }

    @PreAuthorize("isAdmin(#userIdentification, 'USER')")
    @PatchMapping("/setUserRole/{userIdentification}")
    public ResponseWrapper<Boolean> setUserRole(@PathVariable String userIdentification, @RequestBody Role role) {
        if (userService.setRole(userIdentification, role)) {
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

        int pageToUse = page == null ? DEFAULT_PAGE : page;
        int sizeToUse = size == null ? DEFAULT_SIZE : size;

        List<User> users = page == null && size == null
                ? userService.findAllUsersAtCommonGroup(commonGroupIdentification)
                : userService.findAllUsersAtCommonGroup(commonGroupIdentification, pageToUse, sizeToUse);

        List<UserDto> result = users.stream()
                .map(UserTransportMapper::convertToUserDto)
                .collect(Collectors.toList());

        return createPageableResponse(result, page, size);
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PatchMapping("/addUserToPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> addUserToPrivilegeGroup(@PathVariable String privilegeGroupIdentification, @RequestBody UserIdRoleDto userRole) {
        Optional<User> storedUser = userService.findUser(userRole.getUserIdentification());
        if (storedUser.isPresent() && storedUser.get().isGlobalAdmin()) {
            return createEmptyResponseWithError(String.format("The user \"%s\" is an global admin and could not be added to an privilege group"
                    , userRole.getUserIdentification()));
        }

        boolean result = userService.addUserToPrivilegeGroup(privilegeGroupIdentification, userRole.getUserIdentification(), userRole.getRole());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not added with role %s to privilege group with identification \"%s\""
                , userRole.getUserIdentification(), userRole.getRole().getDescription(), privilegeGroupIdentification));
    }

    @PreAuthorize("isManager(#privilegeGroupIdentification, 'PRIVILEGE')")
    @PatchMapping("/removeUserFromPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeUserFromPrivilegeGroup(@PathVariable String privilegeGroupIdentification, @RequestBody String userIdentification) {
        boolean result = userService.removeUserFromPrivilegeGroup(privilegeGroupIdentification, userIdentification);
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not removed from privilege group with identification \"%s\""
                , userIdentification, privilegeGroupIdentification));
    }

    @PreAuthorize("isContributor(#baseGroupIdentification, 'BASE')")
    @PatchMapping("/addUserToBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> addUserToBaseGroup(@PathVariable String baseGroupIdentification, @RequestBody String userIdentification) {
        Optional<User> storedUser = userService.findUser(userIdentification);
        if (storedUser.isPresent() && storedUser.get().isGlobalAdmin()) {
            return createEmptyResponseWithError(String.format("The user \"%s\" is an global admin and could not be added to an privilege group", userIdentification));
        }

        boolean result = userService.addUserToBaseGroup(baseGroupIdentification, userIdentification);
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not added to base group with identification \"%s\""
                , userIdentification, baseGroupIdentification));
    }

    @PreAuthorize("isContributor(#baseGroupIdentification, 'BASE')")
    @PatchMapping("/removeUserFromBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeUserFromBaseGroup(@PathVariable String baseGroupIdentification, @RequestBody String userIdentification) {
        boolean result = userService.removeUserFromBaseGroup(baseGroupIdentification, userIdentification);
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

        int pageToUse = page == null ? DEFAULT_PAGE : page;
        int sizeToUse = size == null ? DEFAULT_SIZE : size;

        List<User> users = page == null && size == null
                ? userService.findAllUsersAtBaseGroup(baseGroupIdentification, Boolean.TRUE.equals(dissolveSubgroups))
                : userService.findAllUsersAtBaseGroup(baseGroupIdentification, pageToUse, sizeToUse);

        List<UserDto> result = users.stream()
                .map(UserTransportMapper::convertToUserDto)
                .collect(Collectors.toList());

        ResponseWrapper<List<UserDto>> responseWrapper = createPageableResponse(result, page, size);
        if (Boolean.TRUE.equals(dissolveSubgroups) && (page != null || size != null)) {
            responseWrapper.addMessage("Dissolving subgroups is not available while using pages", Status.WARN);
        }
        return responseWrapper;
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

        int pageToUse = page == null ? DEFAULT_PAGE : page;
        int sizeToUse = size == null ? DEFAULT_SIZE : size;

        Map<Role, List<User>> rolesWithUsers = page == null && size == null
                ? userService.findAllUsersAtPrivilegeGroup(privilegeGroupIdentification, role, Boolean.TRUE.equals(dissolveSubgroups))
                : userService.findAllUsersAtPrivilegeGroup(privilegeGroupIdentification, role, pageToUse, sizeToUse);

        List<UserRoleDto> result = rolesWithUsers.entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(u -> {
                            UserRoleDto userRoleDto = new UserRoleDto();
                            userRoleDto.setRole(e.getKey());
                            userRoleDto.setUser(UserTransportMapper.convertToUserDto(u));
                            return userRoleDto;
                        }))
                .collect(Collectors.toList());

        ResponseWrapper<List<UserRoleDto>> responseWrapper = createPageableResponse(result, page, size);
        if (Boolean.TRUE.equals(dissolveSubgroups) && (page != null || size != null)) {
            responseWrapper.addMessage("Dissolving subgroups is not available while using pages", Status.WARN);
        }
        return responseWrapper;
    }
}
