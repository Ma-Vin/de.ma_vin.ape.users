package de.ma_vin.ape.users.security.expression;

import de.ma_vin.ape.users.security.service.UserPermissionService;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link ControllerMethodSecurityExpressionHandler} is the class under test
 */
public class ControllerMethodSecurityExpressionHandlerTest {
    private ControllerMethodSecurityExpressionHandler cut;
    private AutoCloseable openMocks;

    @Mock
    private UserPermissionService userPermissionService;
    @Mock
    private Authentication authentication;
    @Mock
    private MethodInvocation invocation;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new ControllerMethodSecurityExpressionHandler(userPermissionService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    public void testCreateSecurityExpressionRoot() {
        MethodSecurityExpressionOperations result = cut.createSecurityExpressionRoot(authentication, invocation);

        assertNotNull(result, "There should be any result");
        assertEquals(ControllerMethodSecurityExpressionRoot.class.getSimpleName(), result.getClass().getSimpleName()
                , "The result is not the expected class");
    }
}
