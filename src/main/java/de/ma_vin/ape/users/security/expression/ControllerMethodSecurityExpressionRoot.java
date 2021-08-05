package de.ma_vin.ape.users.security.expression;

import de.ma_vin.ape.users.enums.GroupType;
import de.ma_vin.ape.users.security.service.UserPermissionService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;

import java.util.Optional;

/**
 * Provides additional method security based on users model
 */
@Getter
@Setter
public class ControllerMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    /**
     * Service which provides additional permissions based on users model
     */
    private UserPermissionService userPermissionService;

    private Object filterObject;
    private Object returnObject;
    private Object target;

    /**
     * Creates a new instance
     *
     * @param authentication        the {@link Authentication} to use. Cannot be null.
     * @param userPermissionService Service which provides additional permissions based on users model
     */
    public ControllerMethodSecurityExpressionRoot(Authentication authentication, UserPermissionService userPermissionService) {
        super(authentication);
        this.userPermissionService = userPermissionService;
    }

    @Override
    public Object getThis() {
        return target;
    }

    void setThis(Object target) {
        this.target = target;
    }

    /**
     * @return {@code true} if the username from oauth2 principal can be identified as global admin. Otherwise {@code false}
     */
    public boolean isGlobalAdmin() {
        return userPermissionService.isGlobalAdmin(getUsername());
    }

    /**
     * Checks whether the principal has admin permissions
     *
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as admin. Otherwise {@code false}
     */
    public boolean isAdmin(String groupIdentification, GroupType groupType) {
        return userPermissionService.isAdmin(getUsername(), groupIdentification, groupType);
    }

    /**
     * Checks whether the principal has manager permissions
     *
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as manager. Otherwise {@code false}
     */
    public boolean isManager(String groupIdentification, GroupType groupType) {
        return userPermissionService.isManager(getUsername(), groupIdentification, groupType);
    }

    /**
     * Checks whether the principal has contributor permissions
     *
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as contributor. Otherwise {@code false}
     */
    public boolean isContributor(String groupIdentification, GroupType groupType) {
        return userPermissionService.isContributor(getUsername(), groupIdentification, groupType);
    }

    /**
     * Checks whether the principal has visitor permissions
     *
     * @param groupIdentification identification of the group where to start search for permissions
     * @param groupType           type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as visitor. Otherwise {@code false}
     */
    public boolean isVisitor(String groupIdentification, GroupType groupType) {
        return userPermissionService.isVisitor(getUsername(), groupIdentification, groupType);
    }

    private Optional<String> getUsername() {
        return getAuthentication().getPrincipal() instanceof DefaultOAuth2AuthenticatedPrincipal ?
                Optional.of(((DefaultOAuth2AuthenticatedPrincipal) getAuthentication().getPrincipal()).getName()) : Optional.empty();
    }
}
