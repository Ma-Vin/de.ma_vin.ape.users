package de.ma_vin.ape.users.security.expression;

import de.ma_vin.ape.users.security.service.UserPermissionService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;

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
        return getAuthentication().getPrincipal() instanceof DefaultOAuth2AuthenticatedPrincipal
                && userPermissionService.isGlobalAdmin(((DefaultOAuth2AuthenticatedPrincipal) getAuthentication().getPrincipal()).getName());
    }

}
