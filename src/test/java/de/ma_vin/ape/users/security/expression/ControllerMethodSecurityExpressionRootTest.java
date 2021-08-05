package de.ma_vin.ape.users.security.expression;

import de.ma_vin.ape.users.enums.GroupType;
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
import java.util.Optional;

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
    public static final String GROUP_IDENTIFICATION = "GAA0001";

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
        when(userPermissionService.isGlobalAdmin(eq(Optional.of(PRINCIPAL_NAME)))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isGlobalAdmin(), "The principal should be identified as an global admin");

        verify(userPermissionService).isGlobalAdmin(eq(Optional.of(PRINCIPAL_NAME)));
    }

    @DisplayName("Check if the principal is a global admin, but is not")
    @Test
    public void testIsGlobalAdminButIsNot() {
        when(userPermissionService.isGlobalAdmin(eq(Optional.of(PRINCIPAL_NAME)))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isGlobalAdmin(), "The principal should be identified as an global admin");

        verify(userPermissionService).isGlobalAdmin(eq(Optional.of(PRINCIPAL_NAME)));
    }

    @DisplayName("Check if the principal is a global admin, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsGlobalAdminNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isGlobalAdmin(eq(Optional.of(PRINCIPAL_NAME)))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isGlobalAdmin(), "The principal should not be identified as an global admin");

        verify(userPermissionService, never()).isGlobalAdmin(eq(Optional.of(PRINCIPAL_NAME)));
    }

    @DisplayName("Check if the principal is an admin")
    @Test
    public void testIsAdmin() {
        when(userPermissionService.isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isAdmin(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should be identified as an admin");

        verify(userPermissionService).isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is an admin, but is not")
    @Test
    public void testIsAdminButIsNot() {
        when(userPermissionService.isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isAdmin(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should be identified as an admin");

        verify(userPermissionService).isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is an admin, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsAdminNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isAdmin(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should not be identified as an admin");

        verify(userPermissionService, never()).isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a manager")
    @Test
    public void testIsManager() {
        when(userPermissionService.isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isManager(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should be identified as a manager");

        verify(userPermissionService).isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a manager, but is not")
    @Test
    public void testIsManagerButIsNot() {
        when(userPermissionService.isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isManager(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should be identified as a manager");

        verify(userPermissionService).isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a manager, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsManagerNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isManager(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should not be identified as a manager");

        verify(userPermissionService, never()).isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a contributor")
    @Test
    public void testIsContributor() {
        when(userPermissionService.isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isContributor(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should be identified as a contributor");

        verify(userPermissionService).isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a contributor, but is not")
    @Test
    public void testIsContributorButIsNot() {
        when(userPermissionService.isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isContributor(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should be identified as a contributor");

        verify(userPermissionService).isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a contributor, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsContributorNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isContributor(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should not be identified as a contributor");

        verify(userPermissionService, never()).isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a visitor")
    @Test
    public void testisVisitor() {
        when(userPermissionService.isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isVisitor(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should be identified as a visitor");

        verify(userPermissionService).isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a visitor, but is not")
    @Test
    public void testisVisitorButIsNot() {
        when(userPermissionService.isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isVisitor(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should be identified as a visitor");

        verify(userPermissionService).isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }

    @DisplayName("Check if the principal is a visitor, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testisVisitorNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isVisitor(GROUP_IDENTIFICATION, GroupType.BASE), "The principal should not be identified as a visitor");

        verify(userPermissionService, never()).isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(GroupType.BASE));
    }
}
