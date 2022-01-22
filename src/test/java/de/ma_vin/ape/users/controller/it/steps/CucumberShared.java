package de.ma_vin.ape.users.controller.it.steps;

import de.ma_vin.ape.users.controller.auth.TokenResponse;
import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dto.ITransportable;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
@Component
public class CucumberShared {

    private String principalUserId;
    private String clientId;

    private String principalPassword;
    private String clientSecret;

    private TokenResponse tokenResponse;

    private Map<String, ITransportable> createdObjects = new HashMap<>();

    private ResultActions resultActions;

    private HttpStatus httpStatus;

    private Map<String, List<UserAndRole>> groupsWithUsers = new HashMap<>();

    private List<String> globalAdmins = new ArrayList<>();

    private boolean initAdminGroupFeature = false;

    private int createdAdmins = 0;

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

    public String getClientSecretBase64() {
        return Base64.getUrlEncoder().encodeToString(clientSecret.getBytes(StandardCharsets.UTF_8));
    }

    class UserAndRole {
        String userId;
        Role role;
    }
}
