package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.dto.ITransportable;
import de.ma_vin.ape.users.model.gen.dto.group.AdminGroupDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.users.model.gen.dto.user.part.UserPartDto;
import de.ma_vin.ape.users.model.gen.mapper.GroupTransportMapper;
import de.ma_vin.ape.users.model.gen.mapper.UserPartTransportMapper;
import de.ma_vin.ape.users.model.gen.mapper.UserTransportMapper;
import de.ma_vin.ape.users.service.AdminGroupService;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.utils.controller.response.ResponseUtil;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.ma_vin.ape.utils.controller.response.ResponseUtil.createEmptyResponseWithError;
import static de.ma_vin.ape.utils.controller.response.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping(path = "admin")
@Data
public class AdminController extends AbstractDefaultOperationController {

    @Autowired
    private UserService userService;
    @Autowired
    private AdminGroupService adminGroupService;


    @PreAuthorize("isGlobalAdmin()")
    @GetMapping("/getAdminGroup/{adminGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<AdminGroupDto> getAdminGroup(@PathVariable String adminGroupIdentification) {
        return get(adminGroupIdentification, AdminGroup.class
                , identificationToGet -> adminGroupService.findAdminGroup(identificationToGet)
                , GroupTransportMapper::convertToAdminGroupDto
        );
    }

    @PreAuthorize("isGlobalAdmin()")
    @PutMapping("/updateAdminGroup/{adminGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<AdminGroupDto> updateAdminGroup(@RequestBody AdminGroupDto adminGroup, @PathVariable String adminGroupIdentification) {
        return update(adminGroup, adminGroupIdentification, AdminGroup.class
                , identificationToUpdate -> adminGroupService.findAdminGroup(identificationToUpdate)
                , objectToUpdate -> adminGroupService.save(objectToUpdate)
                , GroupTransportMapper::convertToAdminGroupDto
                , GroupTransportMapper::convertToAdminGroup
        );
    }

    @PreAuthorize("isGlobalAdmin()")
    @PostMapping("/createAdmin")
    public @ResponseBody
    ResponseWrapper<UserDto> createAdmin(Principal principal, @RequestParam String firstName, @RequestParam String lastName, @RequestParam String adminGroupIdentification) {
        Optional<User> result = userService.saveAtAdminGroup(new UserExt(firstName, lastName, Role.ADMIN), adminGroupIdentification, principal.getName());
        if (result.isEmpty()) {
            return ResponseUtil.createEmptyResponseWithError(String.format("The admin with name \"%s, %s\" was not created", lastName, firstName));
        }
        return ResponseUtil.createSuccessResponse(UserTransportMapper.convertToUserDto(result.get()));
    }

    @PreAuthorize("isGlobalAdmin()")
    @DeleteMapping("/deleteAdmin/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<Boolean> deleteAdmin(@PathVariable String userIdentification) {
        return delete(userIdentification, User.class
                , getGlobalAdminSearcher()
                , objectToDelete -> userService.delete(objectToDelete)
                , identificationToCheck -> userService.userExits(identificationToCheck));
    }

    @PreAuthorize("isGlobalAdmin()")
    @GetMapping("/getAdmin/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<UserDto> getAdmin(@PathVariable String userIdentification) {
        return get(userIdentification, User.class
                , getGlobalAdminSearcher()
                , UserTransportMapper::convertToUserDto
        );
    }

    @PreAuthorize("isGlobalAdmin()")
    @GetMapping("/countAdmins/{adminGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<Long> countAdmins(@PathVariable String adminGroupIdentification) {
        return createSuccessResponse(userService.countUsersAtAdminGroup(adminGroupIdentification));
    }

    @PreAuthorize("isGlobalAdmin()")
    @GetMapping("/getAllAdmins/{adminGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserDto>> getAllAdmins(@PathVariable String adminGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllAdmins(adminGroupIdentification, page, size, UserTransportMapper::convertToUserDto);
    }

    @PreAuthorize("isGlobalAdmin()")
    @GetMapping("/getAllAdminParts/{adminGroupIdentification}")
    public @ResponseBody
    ResponseWrapper<List<UserPartDto>> getAllAdminParts(@PathVariable String adminGroupIdentification
            , @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {

        return getAllAdmins(adminGroupIdentification, page, size, UserPartTransportMapper::convertToUserPartDto);
    }

    /**
     * Loads all admins and converts them to a wrapped list of {@code T} elements
     *
     * @param adminGroupIdentification the parent admin group
     * @param page                     zero-based page index, must not be negative.
     * @param size                     the size of the page to be returned, must be greater than 0.
     * @param mapper                   a mapper to convert the loaded sub elements from the domain to the transport model
     * @param <T>                      the transport model
     * @return a wrapped list of loaded admins
     */
    private <T extends ITransportable> ResponseWrapper<List<T>> getAllAdmins(String adminGroupIdentification
            , Integer page, Integer size, Function<User, T> mapper) {

        return getAllSubElements(adminGroupIdentification, page, size
                , identification -> userService.findAllUsersAtAdminGroup(identification)
                , (identification, pageToUse, sizeToUse) -> userService.findAllUsersAtAdminGroup(identification, pageToUse, sizeToUse)
                , mapper);
    }

    @PreAuthorize("isGlobalAdmin()")
    @PutMapping("/updateAdmin/{userIdentification}")
    public @ResponseBody
    ResponseWrapper<UserDto> updateAdmin(Principal principal, @RequestBody UserDto user, @PathVariable String userIdentification) {
        return update(user, userIdentification, User.class
                , getGlobalAdminSearcher()
                , objectToUpdate -> userService.save(objectToUpdate, principal.getName())
                , UserTransportMapper::convertToUserDto
                , UserTransportMapper::convertToUser
        );
    }

    @PreAuthorize("isGlobalAdmin()")
    @PatchMapping("/setAdminPassword/{userIdentification}")
    public ResponseWrapper<Boolean> setAdminPassword(Principal principal, @PathVariable String userIdentification, @RequestBody String rawPassword) {
        if (userService.setPassword(userIdentification, rawPassword, true, principal.getName())) {
            return createSuccessResponse(Boolean.TRUE);
        }
        return createEmptyResponseWithError(String.format("The password could not be set at user with identification %s", userIdentification));
    }

    /**
     * @return Implementation of searcher functional interface to get an global admin
     */
    private Searcher<User> getGlobalAdminSearcher() {
        return identificationToDelete -> {
            Optional<User> searchResult = userService.findUser(identificationToDelete);
            return searchResult.isPresent() && searchResult.get().isGlobalAdmin() ? searchResult : Optional.empty();
        };
    }
}
