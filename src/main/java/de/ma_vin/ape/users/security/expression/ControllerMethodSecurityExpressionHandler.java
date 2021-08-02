package de.ma_vin.ape.users.security.expression;

import de.ma_vin.ape.users.security.service.UserPermissionService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * Provides method security root which contains additional methods based on users model
 */
public class ControllerMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    /**
     * Service which provides additional permissions based on users model
     */
    private UserPermissionService userPermissionService;

    /**
     * Constructor
     * @param userPermissionService Service which provides additional permissions based on users model
     */
    public ControllerMethodSecurityExpressionHandler(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
        ControllerMethodSecurityExpressionRoot root = new ControllerMethodSecurityExpressionRoot(authentication, userPermissionService);

        root.setThis(invocation.getThis());
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(getTrustResolver());
        root.setRoleHierarchy(getRoleHierarchy());
        root.setDefaultRolePrefix(getDefaultRolePrefix());

        return root;
    }
}
