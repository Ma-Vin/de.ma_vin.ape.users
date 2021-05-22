package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.dto.group.UserRoleDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.users.model.gen.mapper.UserTransportMapper;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.utils.controller.response.ResponseUtil;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.ma_vin.ape.utils.controller.response.ResponseUtil.*;

@RestController
@RequestMapping(path = "user")
@Data
public class UserController extends AbstractDefaultOperationController {

    @Autowired
    private UserService userService;

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
    ResponseWrapper<Boolean> addUserToPrivilegeGroup(@PathVariable String privilegeGroupIdentification, @RequestBody UserRoleDto userRoleDto) {
        Optional<User> storedUser = userService.findUser(userRoleDto.getIdentification());
        if (storedUser.isPresent() && storedUser.get().isGlobalAdmin()) {
            return createEmptyResponseWithError(String.format("The user \"%s\" is an global admin and could not be added to an privilege group", userRoleDto.getIdentification()));
        }

        boolean result = userService.addUserToPrivilegeGroup(privilegeGroupIdentification, userRoleDto.getIdentification(), userRoleDto.getRole());
        return result ? createSuccessResponse(Boolean.TRUE)
                : createResponseWithWarning(Boolean.FALSE, String.format("The user with identification \"%s\" was not added with role %s to privilege group with identification \"%s\""
                , userRoleDto.getIdentification(), userRoleDto.getRole().getDescription(), privilegeGroupIdentification));
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
}
