package de.ma_vin.ape.users.security.expression;

import de.ma_vin.ape.users.security.service.UserPermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link ControllerMethodSecurityExpressionRoot} is the class under test
 */
public class ControllerMethodSecurityExpressionRootTest {
    public static final String PRINCIPAL_NAME = "UAA0001";

    private ControllerMethodSecurityExpressionRoot cut;
    private AutoCloseable openMocks;

    @Mock
    private Authentication authentication;
    @Mock
    private UserPermissionService userPermissionService;

    private DefaultOAuth2AuthenticatedPrincipal principal;
    private Map<String, Object> attributes;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new ControllerMethodSecurityExpressionRoot(authentication, userPermissionService);

        attributes = new HashMap<>();
        attributes.put("sub", PRINCIPAL_NAME);
        principal = new DefaultOAuth2AuthenticatedPrincipal(PRINCIPAL_NAME, attributes, null);

        when(authentication.getPrincipal()).thenReturn(principal);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Check if the principal is a global admin")
    @Test
    public void testIsGlobalAdmin() {
        when(userPermissionService.isGlobalAdmin(eq(PRINCIPAL_NAME))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isGlobalAdmin(), "The principal should be identified as an admin");

        verify(userPermissionService).isGlobalAdmin(eq(PRINCIPAL_NAME));
    }

    @DisplayName("Check if the principal is a global admin, but is not")
    @Test
    public void testIsGlobalAdminNotAdmin() {
        when(userPermissionService.isGlobalAdmin(eq(PRINCIPAL_NAME))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isGlobalAdmin(), "The principal should be identified as an admin");

        verify(userPermissionService).isGlobalAdmin(eq(PRINCIPAL_NAME));
    }

    @DisplayName("Check if the principal is a global admin, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsGlobalAdminNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isGlobalAdmin(eq(PRINCIPAL_NAME))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isGlobalAdmin(), "The principal should not be identified as an admin");

        verify(userPermissionService, never()).isGlobalAdmin(eq(PRINCIPAL_NAME));
    }
}
