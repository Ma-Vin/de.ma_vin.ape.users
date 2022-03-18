package de.ma_vin.ape.users.security.expression;

import de.ma_vin.ape.users.enums.IdentificationType;
import de.ma_vin.ape.users.security.service.UserPermissionService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

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
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as admin. Otherwise {@code false}
     */
    public boolean isAdmin(String identification, IdentificationType identificationType) {
        return userPermissionService.isAdmin(getUsername(), identification, identificationType);
    }

    /**
     * Checks whether the principal has manager permissions
     *
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as manager. Otherwise {@code false}
     */
    public boolean isManager(String identification, IdentificationType identificationType) {
        return userPermissionService.isManager(getUsername(), identification, identificationType);
    }

    /**
     * Checks whether the principal has contributor permissions
     *
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as contributor. Otherwise {@code false}
     */
    public boolean isContributor(String identification, IdentificationType identificationType) {
        return userPermissionService.isContributor(getUsername(), identification, identificationType);
    }

    /**
     * Checks whether the principal has visitor permissions
     *
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as visitor. Otherwise {@code false}
     */
    public boolean isVisitor(String identification, IdentificationType identificationType) {
        return userPermissionService.isVisitor(getUsername(), identification, identificationType);
    }

    /**
     * Checks whether the principal is blocked or not
     *
     * @param identification     identification of the group or user where to start search for permissions
     * @param identificationType type of the group whose identification is given
     * @return {@code true} if the username from oauth2 principal can be identified as blocked. Otherwise {@code false}
     */
    public boolean isBlocked(String identification, IdentificationType identificationType) {
        return userPermissionService.isBlocked(getUsername(), identification, identificationType);
    }

    /**
     * Checks whether the principal has the equal name/id
     *
     * @param userIdentification identification of the user to compare with principal
     * @return {@code true} if the username from oauth2 principal can be identified with the given user identification. Otherwise {@code false}
     */
    public boolean isPrincipalItself(String userIdentification) {
        return getUsername().stream().anyMatch(u -> u.equals(userIdentification)) && !isBlocked(userIdentification, IdentificationType.USER);
    }

    /**
     * Checks whether the principal has an equal or higher worth role
     *
     * @param userIdentification id of the user to compare with
     * @return {@code true} if the principal has the same role or a role which is more worth than the user one. Otherwise {@code false}
     */
    public boolean hasPrincipalEqualOrHigherPrivilege(String userIdentification) {
        return userPermissionService.hasEqualOrHigherRole(getUsername(), userIdentification);
    }

    /**
     * Checks whether the principal has a higher worth role
     *
     * @param userIdentification id of the user to compare with
     * @return {@code true} if the principal has a role which is more worth than the user one. Otherwise {@code false}
     */
    public boolean hasPrincipalHigherPrivilege(String userIdentification) {
        return userPermissionService.hasHigherRole(getUsername(), userIdentification);
    }

    private Optional<String> getUsername() {
        return getAuthentication().getPrincipal() instanceof OAuth2AuthenticatedPrincipal oAuth2AuthenticatedPrincipal ?
                Optional.of(oAuth2AuthenticatedPrincipal.getName()) : Optional.empty();
    }
}
