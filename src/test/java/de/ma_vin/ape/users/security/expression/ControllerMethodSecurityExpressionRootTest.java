package de.ma_vin.ape.users.security.expression;

import de.ma_vin.ape.users.enums.IdentificationType;
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
    public static final String OTHER_USER_IDENTIFICATION = "UAA0002";
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
        when(userPermissionService.isBlocked(any(), any(), any())).thenReturn(Boolean.FALSE);
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
        when(userPermissionService.isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isAdmin(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as an admin");

        verify(userPermissionService).isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is an admin, but is not")
    @Test
    public void testIsAdminButIsNot() {
        when(userPermissionService.isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isAdmin(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as an admin");

        verify(userPermissionService).isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is an admin, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsAdminNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isAdmin(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should not be identified as an admin");

        verify(userPermissionService, never()).isAdmin(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a manager")
    @Test
    public void testIsManager() {
        when(userPermissionService.isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isManager(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as a manager");

        verify(userPermissionService).isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a manager, but is not")
    @Test
    public void testIsManagerButIsNot() {
        when(userPermissionService.isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isManager(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as a manager");

        verify(userPermissionService).isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a manager, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsManagerNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isManager(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should not be identified as a manager");

        verify(userPermissionService, never()).isManager(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a contributor")
    @Test
    public void testIsContributor() {
        when(userPermissionService.isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isContributor(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as a contributor");

        verify(userPermissionService).isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a contributor, but is not")
    @Test
    public void testIsContributorButIsNot() {
        when(userPermissionService.isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isContributor(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as a contributor");

        verify(userPermissionService).isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a contributor, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsContributorNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isContributor(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should not be identified as a contributor");

        verify(userPermissionService, never()).isContributor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a visitor")
    @Test
    public void testIsVisitor() {
        when(userPermissionService.isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isVisitor(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as a visitor");

        verify(userPermissionService).isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a visitor, but is not")
    @Test
    public void testIsVisitorButIsNot() {
        when(userPermissionService.isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isVisitor(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as a visitor");

        verify(userPermissionService).isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is a visitor, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsVisitorNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isVisitor(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should not be identified as a visitor");

        verify(userPermissionService, never()).isVisitor(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is blocked")
    @Test
    public void testIsBlocked() {
        when(userPermissionService.isBlocked(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);

        assertTrue(cut.isBlocked(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as a blocked");

        verify(userPermissionService).isBlocked(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is blocked, but is not")
    @Test
    public void testIsBlockedButIsNot() {
        when(userPermissionService.isBlocked(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.FALSE);

        assertFalse(cut.isBlocked(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should be identified as a blocked");

        verify(userPermissionService).isBlocked(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal  is blocked, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsBlockedNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.isBlocked(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE))).thenReturn(Boolean.TRUE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isBlocked(GROUP_IDENTIFICATION, IdentificationType.BASE), "The principal should not be identified as a blocked");

        verify(userPermissionService, never()).isBlocked(eq(Optional.of(PRINCIPAL_NAME)), eq(GROUP_IDENTIFICATION), eq(IdentificationType.BASE));
    }

    @DisplayName("Check if the principal is the given user identification itself")
    @Test
    public void testIsPrincipalItself() {
        assertTrue(cut.isPrincipalItself(PRINCIPAL_NAME), "The user identification should be identified as principal");

        verify(userPermissionService).isBlocked(eq(Optional.of(PRINCIPAL_NAME)), eq(PRINCIPAL_NAME), eq(IdentificationType.USER));
    }

    @DisplayName("Check if the principal is not the given user identification itself")
    @Test
    public void testIsPrincipalItselfButIsNot() {
        principal = new DefaultOAuth2AuthenticatedPrincipal(PRINCIPAL_NAME + "_1", attributes, null);
        when(authentication.getPrincipal()).thenReturn(principal);
        assertFalse(cut.isPrincipalItself(PRINCIPAL_NAME), "The user identification should not be identified as principal");

        verify(userPermissionService, never()).isBlocked(any(), any(), any());
    }

    @DisplayName("Check if the principal is the given user identification itself, but is blocked")
    @Test
    public void testIsPrincipalItselfButBlocked() {
        when(userPermissionService.isBlocked(eq(Optional.of(PRINCIPAL_NAME)), any(), any())).thenReturn(Boolean.TRUE);
        assertFalse(cut.isPrincipalItself(PRINCIPAL_NAME), "The user identification should not be identified as principal");

        verify(userPermissionService).isBlocked(eq(Optional.of(PRINCIPAL_NAME)), eq(PRINCIPAL_NAME), eq(IdentificationType.USER));
    }

    @DisplayName("Check if the principal is the given user identification itself, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testIsPrincipalItselfNotDefaultOAuth2AuthenticatedPrincipal() {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.isPrincipalItself(PRINCIPAL_NAME), "The user identification should not be identified as principal");

        verify(userPermissionService, never()).isBlocked(any(), any(), any());
    }

    @DisplayName("Check if the principal has a more worth role than user")
    @Test
    public void tesHasPrincipalEqualOrHigherPrivilege() {
        when(userPermissionService.hasEqualOrHigherRole(any(), any())).thenReturn(Boolean.TRUE);
        when(userPermissionService.hasEqualOrHigherRole(eq(Optional.empty()), any())).thenReturn(Boolean.FALSE);
        assertTrue(cut.hasPrincipalEqualOrHigherPrivilege(OTHER_USER_IDENTIFICATION), "The Principal should have role which is more worth");

        verify(userPermissionService).hasEqualOrHigherRole(eq(Optional.of(PRINCIPAL_NAME)), eq(OTHER_USER_IDENTIFICATION));
    }

    @DisplayName("Check if the principal has a more worth role than user, but not DefaultOAuth2AuthenticatedPrincipal")
    @Test
    public void testHasPrincipalEqualOrHigherPrivilegeNotDefaultOAuth2AuthenticatedPrincipal() {
        when(userPermissionService.hasEqualOrHigherRole(any(), any())).thenReturn(Boolean.TRUE);
        when(userPermissionService.hasEqualOrHigherRole(eq(Optional.empty()), any())).thenReturn(Boolean.FALSE);
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(cut.hasPrincipalEqualOrHigherPrivilege(OTHER_USER_IDENTIFICATION), "The Principal should have role which is more worth");

        verify(userPermissionService).hasEqualOrHigherRole(eq(Optional.empty()), eq(OTHER_USER_IDENTIFICATION));
    }
}
