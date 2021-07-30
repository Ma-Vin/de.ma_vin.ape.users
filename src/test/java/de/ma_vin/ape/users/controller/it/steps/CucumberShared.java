package de.ma_vin.ape.users.controller.it.steps;

import de.ma_vin.ape.users.controller.auth.TokenResponse;
import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dto.ITransportable;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
public class CucumberShared {

    private String principalUserId;

    private String principalPassword;

    private TokenResponse tokenResponse;

    private Map<String, ITransportable> createdObjects = new HashMap<>();

    private ResultActions resultActions;

    private Map<String, List<UserAndRole>> groupsWithUsers = new HashMap<>();

    private List<String> globalAdmins = new ArrayList<>();

    public void addUser(String privilegeGroup, String userId, Role role) {
        if (!groupsWithUsers.containsKey(privilegeGroup)) {
            groupsWithUsers.put(privilegeGroup, new ArrayList<>());
        }
        UserAndRole userAndRole = new UserAndRole();
        userAndRole.userId = userId;
        userAndRole.role = role;
        groupsWithUsers.get(privilegeGroup).add(userAndRole);
    }

    public void addGroup(String privilegeGroup) {
        if (!groupsWithUsers.containsKey(privilegeGroup)) {
            groupsWithUsers.put(privilegeGroup, new ArrayList<>());
        }
    }

    public String getAccessToken() {
        return tokenResponse == null ? null : tokenResponse.getAccessToken();
    }

    public boolean containsKey(String key) {
        return createdObjects.containsKey(key);
    }

    public ITransportable get(String key) {
        return createdObjects.get(key);
    }

    public void put(String key, ITransportable value) {
        createdObjects.put(key, value);
    }

    class UserAndRole {
        String userId;
        Role role;
    }
}
