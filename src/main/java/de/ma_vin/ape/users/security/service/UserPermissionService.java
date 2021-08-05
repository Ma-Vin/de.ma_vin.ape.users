package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.enums.GroupType;
import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.service.BaseGroupService;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.users.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service which provides additional permissions based on users model
 */
@Service
@Data
public class UserPermissionService {

    @Autowired
    private UserService userService;
    @Autowired
    private BaseGroupService baseGroupService;
    @Autowired
    private PrivilegeGroupService privilegeGroupService;

    /**
     * Determines whether a user is global admin
     *
     * @param username username to check
     * @return {@true} if there exits a user with the given username exists and is a member of admin group. Otherwise {@code false}
     */
    public boolean isGlobalAdmin(Optional<String> username) {
        return username.isPresent() && userService.findUser(username.get()).map(User::isGlobalAdmin).orElse(Boolean.FALSE);
    }

    /**
     * Checks whether the principal has admin permissions
     *
     * @param username            username to check
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as admin. Otherwise {@code false}
     */
    public boolean isAdmin(Optional<String> username, String groupIdentification, GroupType groupType) {
        return hasUserRole(username, groupIdentification, groupType, Role.ADMIN);
    }

    /**
     * Checks whether the principal has manager permissions
     *
     * @param username            username to check
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as manager. Otherwise {@code false}
     */
    public boolean isManager(Optional<String> username, String groupIdentification, GroupType groupType) {
        return hasUserRole(username, groupIdentification, groupType, Role.MANAGER);
    }

    /**
     * Checks whether the principal has contributor permissions
     *
     * @param username            username to check
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as contributor. Otherwise {@code false}
     */
    public boolean isContributor(Optional<String> username, String groupIdentification, GroupType groupType) {
        return hasUserRole(username, groupIdentification, groupType, Role.CONTRIBUTOR);
    }

    /**
     * Checks whether the principal has visitor permissions
     *
     * @param username            username to check
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as visitor. Otherwise {@code false}
     */
    public boolean isVisitor(Optional<String> username, String groupIdentification, GroupType groupType) {
        return hasUserRole(username, groupIdentification, groupType, Role.VISITOR);
    }

    /**
     * Checks whether the principal has a given role permissions
     *
     * @param username            username to check
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @param role                role to check
     * @return {@code true} if the username from oauth2 principal can be identified as owner of the given role. Otherwise {@code false}
     */
    private boolean hasUserRole(Optional<String> username, String groupIdentification, GroupType groupType, Role role) {
        if (username.isEmpty()) {
            return false;
        }
        Optional<String> commonGroupIdentification = switch (groupType) {
            case COMMON -> Optional.of(groupIdentification);
            case BASE -> baseGroupService.findBaseGroup(groupIdentification).map(BaseGroup::getCommonGroupId);
            case ADMIN -> Optional.empty();
            case PRIVILEGE -> privilegeGroupService.findPrivilegeGroup(groupIdentification).map(PrivilegeGroup::getCommonGroupId);
        };

        return userService.findUser(username.get())
                .map(u -> role.getLevel() <= u.getRole().getLevel() &&
                        (u.isGlobalAdmin() || (commonGroupIdentification.isPresent() && commonGroupIdentification.get().equals(u.getCommonGroupId()))))
                .orElse(Boolean.FALSE);
    }
}
