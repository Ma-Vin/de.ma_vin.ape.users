package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service which provides additional permissions based on users model
 */
@Service
@Data
public class UserPermissionService {

    @Autowired
    private UserService userService;

    /**
     * Determines whether a user is global admin
     *
     * @param username username to check
     * @return {@true} if there exits a user with the given username exists and is a member of admin group. Otherwise {@code false}
     */
    public boolean isGlobalAdmin(String username) {
        return userService.findUser(username).map(u -> u instanceof UserExt && u.isGlobalAdmin()).orElse(Boolean.FALSE);
    }
}
