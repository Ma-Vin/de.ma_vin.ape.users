package de.ma_vin.ape.users.security;

import de.ma_vin.ape.users.security.expression.ControllerMethodSecurityExpressionHandler;
import de.ma_vin.ape.users.security.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Autowired
    private UserPermissionService userPermissionService;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new ControllerMethodSecurityExpressionHandler(userPermissionService);
    }
}
