package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.enums.IdentificationType;
import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.service.BaseGroupService;
import de.ma_vin.ape.users.service.PrivilegeGroupService;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.utils.properties.SystemProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
     * @return {@code true} if there exits a user with the given username exists and is a member of admin group. Otherwise {@code false}
     */
    public boolean isGlobalAdmin(Optional<String> username) {
        return username.isPresent() && userService.findUser(username.get()).map(User::isGlobalAdmin).orElse(Boolean.FALSE);
    }

    /**
     * Checks whether the principal has admin permissions
     *
     * @param username           username to check
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as admin. Otherwise {@code false}
     */
    public boolean isAdmin(Optional<String> username, String identification, IdentificationType identificationType) {
        return hasUserRole(username, identification, identificationType, Role.ADMIN);
    }

    /**
     * Checks whether the principal has manager permissions
     *
     * @param username           username to check
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as manager. Otherwise {@code false}
     */
    public boolean isManager(Optional<String> username, String identification, IdentificationType identificationType) {
        return hasUserRole(username, identification, identificationType, Role.MANAGER);
    }

    /**
     * Checks whether the principal has contributor permissions
     *
     * @param username           username to check
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as contributor. Otherwise {@code false}
     */
    public boolean isContributor(Optional<String> username, String identification, IdentificationType identificationType) {
        return hasUserRole(username, identification, identificationType, Role.CONTRIBUTOR);
    }

    /**
     * Checks whether the principal has visitor permissions
     *
     * @param username           username to check
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as visitor. Otherwise {@code false}
     */
    public boolean isVisitor(Optional<String> username, String identification, IdentificationType identificationType) {
        return hasUserRole(username, identification, identificationType, Role.VISITOR);
    }

    /**
     * Checks whether the principal has a given role permissions
     *
     * @param username           username to check
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @param role               role to check
     * @return {@code true} if the username from oauth2 principal can be identified as owner of the given role. Otherwise {@code false}
     */
    private boolean hasUserRole(Optional<String> username, String identification, IdentificationType identificationType, Role role) {
        if (username.isEmpty()) {
            return false;
        }
        Optional<String> commonGroupIdentification = getCommonGroupIdentification(identification, identificationType);

        return userService.findUser(username.get())
                .map(u -> role.getLevel() <= u.getRole().getLevel()
                        && (u.isGlobalAdmin() || (commonGroupIdentification.isPresent() && commonGroupIdentification.get().equals(u.getCommonGroupId())))
                        && isUserValid(u))
                .orElse(Boolean.FALSE);
    }

    /**
     * Determines whether the principal has an equal or higher worth role
     *
     * @param username              username of the principal
     * @param identificationOfOther id of the user to compare with
     * @return {@code true} if the principal has the same role or a role which is more worth than the user one. Otherwise {@code false}
     */
    public boolean hasEqualOrHigherRole(Optional<String> username, String identificationOfOther) {
        Optional<User> otherUser = userService.findUser(identificationOfOther);
        if (otherUser.isEmpty()) {
            return false;
        }
        return hasUserRole(username, identificationOfOther, IdentificationType.USER, otherUser.get().getRole());
    }

    /**
     * Checks whether the principal is blocked
     *
     * @param username           username to check
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as blocked. Otherwise {@code false}
     */
    public boolean isBlocked(Optional<String> username, String identification, IdentificationType identificationType) {
        if (username.isEmpty()) {
            return true;
        }
        Optional<String> commonGroupIdentification = getCommonGroupIdentification(identification, identificationType);
        return userService.findUser(username.get())
                .map(u -> Role.BLOCKED.equals(u.getRole()) || commonGroupIdentification.isEmpty()
                        || !commonGroupIdentification.get().equals(u.getCommonGroupId()))
                .orElse(Boolean.TRUE);
    }

    /**
     * Determines the parent common group identification
     *
     * @param identification     identification of the group or user where to start search for common group
     * @param identificationType type of the group whose identification is given
     * @return identification of common group. Might be empty if not found.
     */
    private Optional<String> getCommonGroupIdentification(String identification, IdentificationType identificationType) {
        return switch (identificationType) {
            case COMMON -> Optional.of(identification);
            case BASE -> baseGroupService.findBaseGroup(identification).map(BaseGroup::getCommonGroupId);
            case ADMIN -> Optional.empty();
            case PRIVILEGE -> privilegeGroupService.findPrivilegeGroup(identification).map(PrivilegeGroup::getCommonGroupId);
            case USER -> userService.findUser(identification).map(User::getCommonGroupId);
        };
    }

    /**
     * Checks the valid from and to date times of a user regarding to the system date time
     *
     * @param user user whose valid dates should be checked
     * @return {@code true} if the system date time is within the interval {@link User#getValidFrom()} and {@link User#getValidTo()}.
     * Otherwise {@code false}
     */
    private boolean isUserValid(User user) {
        LocalDateTime now = SystemProperties.getSystemDateTime();
        return (user.getValidFrom() == null || !user.getValidFrom().isAfter(now)) && (user.getValidTo() == null || !user.getValidTo().isBefore(now));
    }
}
