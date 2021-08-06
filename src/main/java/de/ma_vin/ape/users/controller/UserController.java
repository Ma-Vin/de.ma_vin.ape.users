package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.group.PrivilegeGroupExt;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.dto.group.UserIdRoleDto;
import de.ma_vin.ape.users.model.gen.dto.group.UserRoleDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.users.model.gen.mapper.UserTransportMapper;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.utils.controller.response.ResponseUtil;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/createUser")
    public @ResponseBody
    ResponseWrapper<UserDto> createUser(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String commonGroupIdentification) {
        Optional<User> result = userService.saveAtCommonGroup(new UserExt(firstName, lastName, Role.VISITOR), commonGroupIdentification);
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The user with name \"%s, %s\" was not created", lastName, firstName));
        }
        return ResponseUtil.createSuccessResponse(UserTransportMapper.convertToUserDto(result.get()));
    }

    @DeleteMapping("/deleteUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteUser(@PathVariable String userIdentification) {
        return delete(userIdentification, User.class
                , getNonGlobalAdminSearcher()
                , objectToDelete -> userService.delete(objectToDelete)
                , identificationToCheck -> userService.userExits(identificationToCheck));
    }

    @GetMapping("/getUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<UserDto> getUser(@PathVariable String userIdentification) {
        return get(userIdentification, User.class
                , getNonGlobalAdminSearcher()
                , UserTransportMapper::convertToUserDto
        );
    }

    @PutMapping("/updateUser/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<UserDto> updateUser(@RequestBody UserDto user, @PathVariable String userIdentification) {
        return update(user, userIdentification, User.class
                , getNonGlobalAdminSearcher()
                , objectToUpdate -> userService.save(objectToUpdate)
                , UserTransportMapper::convertToUserDto
                , UserTransportMapper::convertToUser
        );
    }

    @PatchMapping("/setUserPassword/{userIdentification}")
    public ResponseWrapper<Boolean> setUserPassword(@PathVariable String userIdentification, @RequestBody String rawPassword) {
        if (userService.setPassword(userIdentification, rawPassword)) {
            return createSuccessResponse(Boolean.TRUE);
        }
        return createEmptyResponseWithError(String.format("The password could not be set at user with identification %s", userIdentification));
    }

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

    @GetMapping("/getAllUsers/{commonGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserDto>> getAllUsers(@PathVariable String commonGroupIdentification) {
        List<UserDto> result = userService.findAllUsersAtCommonGroup(commonGroupIdentification).stream()
                .map(UserTransportMapper::convertToUserDto)
                .collect(Collectors.toList());

        return createSuccessResponse(result);
    }

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

    @PatchMapping("/removeUserFromPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeUserFromPrivilegeGroup(@PathVariable String privilegeGroupIdentification, @RequestBody String userIdentification) {
        boolean result = userService.removeUserFromPrivilegeGroup(privilegeGroupIdentification, userIdentification);
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not removed from privilege group with identification \"%s\""
                , userIdentification, privilegeGroupIdentification));
    }

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

    @PatchMapping("/removeUserFromBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> removeUserFromBaseGroup(@PathVariable String baseGroupIdentification, @RequestBody String userIdentification) {
        boolean result = userService.removeUserFromBaseGroup(baseGroupIdentification, userIdentification);
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not removed from base group with identification \"%s\""
                , userIdentification, baseGroupIdentification));
    }

    @GetMapping("/getAllUsersFromBaseGroup/{baseGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserDto>> getAllUsersFromBaseGroup(@PathVariable String baseGroupIdentification, @RequestParam(required = false) Boolean dissolveSubgroups) {
        List<UserDto> result = userService.findAllUsersAtBaseGroup(baseGroupIdentification, Boolean.TRUE.equals(dissolveSubgroups)).stream()
                .map(UserTransportMapper::convertToUserDto)
                .collect(Collectors.toList());

        return createSuccessResponse(result);
    }

    @GetMapping("/getAllUsersFromPrivilegeGroup/{privilegeGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserRoleDto>> getAllUsersFromPrivilegeGroup(@PathVariable String privilegeGroupIdentification
            , @RequestParam(required = false) Boolean dissolveSubgroups, @RequestParam(required = false) Role role) {

        Optional<PrivilegeGroup> privilegeGroup = privilegeGroupService.findPrivilegeGroupTree(privilegeGroupIdentification);
        if (privilegeGroup.isEmpty() || !(privilegeGroup.get() instanceof PrivilegeGroupExt)) {
            return createEmptyResponseWithError(String.format("The privilege group with identification \"%s\" could not be loaded", privilegeGroupIdentification));
        }

        List<UserRoleDto> result = new ArrayList<>();

        if (role == null || Role.NOT_RELEVANT.equals(role)) {
            for (Role r : Role.values()) {
                addUserRoleToResult((PrivilegeGroupExt) privilegeGroup.get(), result, r, dissolveSubgroups);
            }
        } else {
            addUserRoleToResult((PrivilegeGroupExt) privilegeGroup.get(), result, role, dissolveSubgroups);
        }

        return createSuccessResponse(result);
    }

    private void addUserRoleToResult(PrivilegeGroupExt privilegeGroup, List<UserRoleDto> result, Role role, Boolean dissolveSubgroups) {
        privilegeGroup.getUsersByRole(role, dissolveSubgroups).forEach(u -> {
            UserRoleDto userRoleDto = new UserRoleDto();
            userRoleDto.setRole(role);
            userRoleDto.setUser(UserTransportMapper.convertToUserDto(u));
            result.add(userRoleDto);
        });
    }
}
