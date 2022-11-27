package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.controller.auth.*;
import de.ma_vin.ape.users.exceptions.AuthTokenException;
import de.ma_vin.ape.users.exceptions.JwtGeneratingException;
import de.ma_vin.ape.users.properties.AuthClients;
import de.ma_vin.ape.users.security.jwt.Header;
import de.ma_vin.ape.users.security.jwt.JsonWebToken;
import de.ma_vin.ape.users.security.jwt.Payload;
import de.ma_vin.ape.users.security.service.AuthorizeCodeService;
import de.ma_vin.ape.users.security.service.token.TokenIssuerService;
import de.ma_vin.ape.utils.properties.SystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link AuthController} is the class under test
 */
public class AuthControllerTest {

    public static final String USER_ID = "DummyUserId";
    public static final String USER_PWD = "DummyUserPwd";
    public static final String CLIENT_ID = "DummyClientId";
    public static final String CLIENT_SECRET = "DummyClientSecret";
    public static final String ISSUER_URL = "http://localhost/dummy";

    public static final String REDIRECT_START_URL = "http://localhost:8080/";
    public static final String REDIRECT_URL = REDIRECT_START_URL + "/someContext";
    public static final String SCOPE = "DummyScope";
    public static final String STATE = "DummyState";
    public static final String CODE = "DummyCode";
    public static final String TOKEN = "DummyToken";
    public static final String REFRESH_TOKEN = "DummyRefreshToken";

    private AuthController cut;
    private AutoCloseable openMocks;

    private AuthClients authClients;
    private AuthClients.Client client;
    private AuthClients.Redirect redirect;

    @Mock
    private TokenIssuerService tokenIssuerService;
    @Mock
    private AuthorizeCodeService authorizeCodeService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Principal principal;
    @Mock
    private TokenIssuerService.TokenInfo tokenInfo;
    @Mock
    private AuthorizeCodeService.CodeInfo codeInfo;
    @Mock
    private JsonWebToken token;
    @Mock
    private JsonWebToken refreshToken;
    @Mock
    private Payload payload;
    @Mock
    private Payload refreshPayload;

    @BeforeEach
    public void setUp() {
        SystemProperties.getInstance().setTestingDateTime(LocalDateTime.of(2021, 7, 1, 12, 0));
        SystemProperties.setZoneId("Europe/Berlin");

        openMocks = openMocks(this);

        cut = new AuthController();
        cut.setAuthorizeCodeService(authorizeCodeService);
        cut.setTokenIssuerService(tokenIssuerService);

        when(principal.getName()).thenReturn(USER_ID);

        when(request.getRequestURL()).thenReturn(new StringBuffer(ISSUER_URL));

        when(tokenInfo.getToken()).thenReturn(token);
        when(tokenInfo.getRefreshToken()).thenReturn(refreshToken);
        when(tokenInfo.getScope()).thenReturn(SCOPE);

        try {
            when(token.getPayload()).thenReturn(payload);
            when(token.getEncodedToken()).thenReturn(TOKEN);
            when(token.getHeader()).thenReturn(new Header());
            when(refreshToken.getPayload()).thenReturn(refreshPayload);
            when(refreshToken.getEncodedToken()).thenReturn(REFRESH_TOKEN);
            when(refreshToken.getHeader()).thenReturn(new Header());
        } catch (JwtGeneratingException e) {
            fail(e.getMessage());
        }

        when(payload.getExp()).thenReturn(Payload.getLocalDateTimeToLong(SystemProperties.getSystemDateTime().plus(10L, ChronoUnit.SECONDS)
                , SystemProperties.getZoneId()));
        when(payload.getSub()).thenReturn(USER_ID);
        when(refreshPayload.getExp()).thenReturn(Payload.getLocalDateTimeToLong(SystemProperties.getSystemDateTime().plus(20L, ChronoUnit.SECONDS)
                , SystemProperties.getZoneId()));

        when(codeInfo.getUserId()).thenReturn(USER_ID);
        when(codeInfo.getCode()).thenReturn(CODE);
        when(codeInfo.getClientId()).thenReturn(CLIENT_ID);
        when(codeInfo.getScope()).thenReturn(SCOPE);
        when(codeInfo.getExpiresAt()).thenReturn(SystemProperties.getSystemDateTime().plus(5L, ChronoUnit.SECONDS));

        authClients = new AuthClients();
        client = new AuthClients.Client();
        client.setClientId(CLIENT_ID);
        client.setSecret(CLIENT_SECRET);
        authClients.getClients().add(client);
        redirect = new AuthClients.Redirect(REDIRECT_START_URL);
        client.getRedirects().add(redirect);
        cut.setAuthClients(authClients);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Authorize a code response type")
    @Test
    public void testAuthorizeCode() {
        when(authorizeCodeService.issue(any(), any(), any())).thenReturn(Optional.of(CODE));

        Object result = cut.authorize(principal, request, response, ResponseType.CODE.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, STATE);

        assertNotNull(result, "There should be any result");
        assertTrue(result instanceof AuthorizationResponse, "Result should be an instance of AuthorizationResponse");
        AuthorizationResponse authorizationResponse = (AuthorizationResponse) result;
        assertNotNull(authorizationResponse.getCode(), "There should be any code");
        assertEquals(CODE, authorizationResponse.getCode(), "Wrong code");

        verify(authorizeCodeService).issue(eq(USER_ID), eq(CLIENT_ID), eq(SCOPE));
        verify(tokenIssuerService, never()).issueImplicit(any(), any(), any());
        try {
            verify(response).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type without redirection")
    @Test
    public void testAuthorizeCodeNoRedirection() {
        when(authorizeCodeService.issue(any(), any(), any())).thenReturn(Optional.of(CODE));

        Object result = cut.authorize(principal, request, response, ResponseType.CODE.getTypeName(), CLIENT_ID, null, SCOPE, STATE);

        assertNotNull(result, "There should be any result");
        assertTrue(result instanceof AuthorizationResponse, "Result should be an instance of AuthorizationResponse");
        AuthorizationResponse authorizationResponse = (AuthorizationResponse) result;
        assertNotNull(authorizationResponse.getCode(), "There should be any code");
        assertEquals(CODE, authorizationResponse.getCode(), "Wrong code");

        verify(authorizeCodeService).issue(eq(USER_ID), eq(CLIENT_ID), eq(SCOPE));
        verify(tokenIssuerService, never()).issueImplicit(any(), any(), any());
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type with empty redirection")
    @Test
    public void testAuthorizeCodeEmptyRedirection() {
        when(authorizeCodeService.issue(any(), any(), any())).thenReturn(Optional.of(CODE));

        Object result = cut.authorize(principal, request, response, ResponseType.CODE.getTypeName(), CLIENT_ID, "", SCOPE, STATE);

        assertNotNull(result, "There should be any result");
        assertTrue(result instanceof AuthorizationResponse, "Result should be an instance of AuthorizationResponse");
        AuthorizationResponse authorizationResponse = (AuthorizationResponse) result;
        assertNotNull(authorizationResponse.getCode(), "There should be any code");
        assertEquals(CODE, authorizationResponse.getCode(), "Wrong code");

        verify(authorizeCodeService).issue(eq(USER_ID), eq(CLIENT_ID), eq(SCOPE));
        verify(tokenIssuerService, never()).issueImplicit(any(), any(), any());
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type not successful")
    @Test
    public void testAuthorizeCodeNotSuccessful() {
        when(authorizeCodeService.issue(any(), any(), any())).thenReturn(Optional.empty());

        try {
            cut.authorize(principal, request, response, ResponseType.CODE.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, STATE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).issue(eq(USER_ID), eq(CLIENT_ID), eq(SCOPE));
        verify(tokenIssuerService, never()).issueImplicit(any(), any(), any());
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type but not successful redirection")
    @Test
    public void testAuthorizeCodeRedirectionNotSuccessful() {
        when(authorizeCodeService.issue(any(), any(), any())).thenReturn(Optional.of(CODE));
        try {
            doThrow(new IOException("redirection")).when(response).sendRedirect(any());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            cut.authorize(principal, request, response, ResponseType.CODE.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, STATE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).issue(eq(USER_ID), eq(CLIENT_ID), eq(SCOPE));
        verify(tokenIssuerService, never()).issueImplicit(any(), any(), any());
        try {
            verify(response).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type but not allowed target of redirection")
    @Test
    public void testAuthorizeCodeRedirectionTargetNotAllowed() {
        when(authorizeCodeService.issue(any(), any(), any())).thenReturn(Optional.of(CODE));
        redirect.setRedirectStart("http://somethingElse:8080/");

        try {
            cut.authorize(principal, request, response, ResponseType.CODE.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, STATE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_FORBIDDEN, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).issue(eq(USER_ID), eq(CLIENT_ID), eq(SCOPE));
        verify(tokenIssuerService, never()).issueImplicit(any(), any(), any());
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a token response type")
    @Test
    public void testAuthorizeImplicit() {
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        Object result = cut.authorize(principal, request, response, ResponseType.TOKEN.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, null);

        assertNotNull(result, "There should be any result");
        assertTrue(result instanceof TokenResponse, "Result should be an instance of AuthorizationResponse");
        TokenResponse tokenResponse = (TokenResponse) result;
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(authorizeCodeService, never()).issue(any(), any(), any());
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a token response type without redirection")
    @Test
    public void testAuthorizeImplicitNoRedirection() {
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        Object result = cut.authorize(principal, request, response, ResponseType.TOKEN.getTypeName(), CLIENT_ID, null, SCOPE, null);

        assertNotNull(result, "There should be any result");
        assertTrue(result instanceof TokenResponse, "Result should be an instance of AuthorizationResponse");
        TokenResponse tokenResponse = (TokenResponse) result;
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(authorizeCodeService, never()).issue(any(), any(), any());
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a token response type with empty redirection")
    @Test
    public void testAuthorizeImplicitEmptyRedirection() {
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        Object result = cut.authorize(principal, request, response, ResponseType.TOKEN.getTypeName(), CLIENT_ID, "", SCOPE, null);

        assertNotNull(result, "There should be any result");
        assertTrue(result instanceof TokenResponse, "Result should be an instance of AuthorizationResponse");
        TokenResponse tokenResponse = (TokenResponse) result;
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(authorizeCodeService, never()).issue(any(), any(), any());
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type not successful")
    @Test
    public void testAuthorizeImplicitNotSuccessful() {
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.empty());

        try {
            cut.authorize(principal, request, response, ResponseType.TOKEN.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService, never()).issue(any(), any(), any());
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type but not successful redirection")
    @Test
    public void testAuthorizeImplicitRedirectionNotSuccessful() {
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));
        try {
            doThrow(new IOException("redirection")).when(response).sendRedirect(any());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            cut.authorize(principal, request, response, ResponseType.TOKEN.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService, never()).issue(any(), any(), any());
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type but not allowed target of redirection")
    @Test
    public void testAuthorizeImplicitRedirectionTargetNotAllowed() {
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));
        redirect.setRedirectStart("http://somethingElse:8080/");

        try {
            cut.authorize(principal, request, response, ResponseType.TOKEN.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_FORBIDDEN, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService, never()).issue(any(), any(), any());
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a code response type not successful token encoding")
    @Test
    public void testAuthorizeImplicitTokenEncodingNotSuccessful() {
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));
        try {
            when(token.getEncodedToken()).thenThrow(new JwtGeneratingException("EncodedToken"));
        } catch (JwtGeneratingException e) {
            fail(e.getMessage());
        }

        try {
            cut.authorize(principal, request, response, ResponseType.TOKEN.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService, never()).issue(any(), any(), any());
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Authorize a not supported response type")
    @Test
    public void testAuthorizeNotSupported() {
        try {
            cut.authorize(principal, request, response, ResponseType.NOT_SUPPORTED.getTypeName(), CLIENT_ID, REDIRECT_URL, SCOPE, STATE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService, never()).issue(any(), any(), any());
        verify(tokenIssuerService, never()).issueImplicit(any(), any(), any());
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type")
    @Test
    public void testTokenAuthorizationCode() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.TRUE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.of(codeInfo));
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        TokenResponse tokenResponse = cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                , REDIRECT_URL, CLIENT_ID, null, null, null);

        assertNotNull(tokenResponse, "There should be any result");
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService).getCodeInfo(eq(CODE));
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type without redirection")
    @Test
    public void testTokenAuthorizationCodeNoRedirection() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.TRUE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.of(codeInfo));
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        TokenResponse tokenResponse = cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                , null, CLIENT_ID, null, null, null);

        assertNotNull(tokenResponse, "There should be any result");
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService).getCodeInfo(eq(CODE));
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(any());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type with empty redirection")
    @Test
    public void testTokenAuthorizationCodeEmptyRedirection() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.TRUE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.of(codeInfo));
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        TokenResponse tokenResponse = cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                , "", CLIENT_ID, null, null, null);

        assertNotNull(tokenResponse, "There should be any result");
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService).getCodeInfo(eq(CODE));
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(any());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type but invalid")
    @Test
    public void testTokenAuthorizationCodeInvalid() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.FALSE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.of(codeInfo));
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        try {
            cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                    , REDIRECT_URL, CLIENT_ID, null, null, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService, never()).getCodeInfo(eq(CODE));
        verify(tokenIssuerService, never()).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type but code info not found")
    @Test
    public void testTokenAuthorizationCodeNotFound() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.TRUE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.empty());
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        try {
            cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                    , REDIRECT_URL, CLIENT_ID, null, null, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService).getCodeInfo(eq(CODE));
        verify(tokenIssuerService, never()).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type but not successful")
    @Test
    public void testTokenAuthorizationCodeNotSuccessful() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.TRUE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.of(codeInfo));
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.empty());

        try {
            cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                    , REDIRECT_URL, CLIENT_ID, null, null, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService).getCodeInfo(eq(CODE));
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type but not successful redirection")
    @Test
    public void testTokenAuthorizationCodeRedirectionNotSuccessful() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.TRUE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.of(codeInfo));
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        try {
            doThrow(new IOException("redirection")).when(response).sendRedirect(any());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                    , REDIRECT_URL, CLIENT_ID, null, null, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService).getCodeInfo(eq(CODE));
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type but not allowed target of redirection")
    @Test
    public void testTokenAuthorizationCodeRedirectionTargetNotAllowed() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.TRUE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.of(codeInfo));
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));
        redirect.setRedirectStart("http://somethingElse:8080/");

        try {
            cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                    , REDIRECT_URL, CLIENT_ID, null, null, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_FORBIDDEN, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService).getCodeInfo(eq(CODE));
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of authorization code grant type but not successful encoding")
    @Test
    public void testTokenAuthorizationCodeEncodingNotSuccessful() {
        when(authorizeCodeService.isValid(any())).thenReturn(Boolean.TRUE);
        when(authorizeCodeService.getCodeInfo(any())).thenReturn(Optional.of(codeInfo));
        when(tokenIssuerService.issueImplicit(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        try {
            when(token.getEncodedToken()).thenThrow(new JwtGeneratingException("EncodedToken"));
        } catch (JwtGeneratingException e) {
            fail(e.getMessage());
        }

        try {
            cut.token(request, response, GrantType.AUTHORIZATION_CODE.getTypeName(), CODE, null
                    , REDIRECT_URL, CLIENT_ID, null, null, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatus(), "Wrong error status");
        }

        verify(authorizeCodeService).isValid(eq(CODE));
        verify(authorizeCodeService).getCodeInfo(eq(CODE));
        verify(tokenIssuerService).issueImplicit(eq(ISSUER_URL), eq(USER_ID), eq(SCOPE));
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Issue token of password grant type")
    @Test
    public void testTokenPassword() {
        when(tokenIssuerService.issue(any(), any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        TokenResponse tokenResponse = cut.token(request, response, GrantType.PASSWORD.getTypeName(), null, null
                , null, CLIENT_ID, USER_ID, USER_PWD, SCOPE);

        assertNotNull(tokenResponse, "There should be any result");
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(tokenIssuerService).issue(eq(ISSUER_URL), eq(USER_ID), eq(USER_PWD), eq(SCOPE));
    }


    @DisplayName("Issue token of password grant type but not successful")
    @Test
    public void testTokenPasswordNotSuccessful() {
        when(tokenIssuerService.issue(any(), any(), any(), any())).thenReturn(Optional.empty());

        try {
            cut.token(request, response, GrantType.PASSWORD.getTypeName(), null, null
                    , null, CLIENT_ID, USER_ID, USER_PWD, SCOPE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(tokenIssuerService).issue(eq(ISSUER_URL), eq(USER_ID), eq(USER_PWD), eq(SCOPE));
    }

    @DisplayName("Issue token of password grant type but not successful encoding")
    @Test
    public void testTokenPasswordEncodingNotSuccessful() {
        when(tokenIssuerService.issue(any(), any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        try {
            when(token.getEncodedToken()).thenThrow(new JwtGeneratingException("EncodedToken"));
        } catch (JwtGeneratingException e) {
            fail(e.getMessage());
        }

        try {
            cut.token(request, response, GrantType.PASSWORD.getTypeName(), null, null
                    , null, CLIENT_ID, USER_ID, USER_PWD, SCOPE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatus(), "Wrong error status");
        }

        verify(tokenIssuerService).issue(eq(ISSUER_URL), eq(USER_ID), eq(USER_PWD), eq(SCOPE));
    }

    @DisplayName("Issue token of client grant type")
    @Test
    public void testTokenClient() {
        when(tokenIssuerService.issueClient(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        TokenResponse tokenResponse = cut.token(request, response, GrantType.CLIENT_CREDENTIALS.getTypeName(), null, null
                , null, CLIENT_ID, null, null, SCOPE);

        assertNotNull(tokenResponse, "There should be any result");
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(tokenIssuerService).issueClient(eq(ISSUER_URL), eq(CLIENT_ID), eq(SCOPE));
    }


    @DisplayName("Issue token of client grant type but not successful")
    @Test
    public void testTokenClientNotSuccessful() {
        when(tokenIssuerService.issueClient(any(), any(), any())).thenReturn(Optional.empty());

        try {
            cut.token(request, response, GrantType.CLIENT_CREDENTIALS.getTypeName(), null, null
                    , null, CLIENT_ID, null, null, SCOPE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(tokenIssuerService).issueClient(eq(ISSUER_URL), eq(CLIENT_ID), eq(SCOPE));
    }

    @DisplayName("Issue token of client grant type but not successful encoding")
    @Test
    public void testTokenClientEncodingNotSuccessful() {
        when(tokenIssuerService.issueClient(any(), any(), any())).thenReturn(Optional.of(tokenInfo));

        try {
            when(token.getEncodedToken()).thenThrow(new JwtGeneratingException("EncodedToken"));
        } catch (JwtGeneratingException e) {
            fail(e.getMessage());
        }

        try {
            cut.token(request, response, GrantType.CLIENT_CREDENTIALS.getTypeName(), null, null
                    , null, CLIENT_ID, null, null, SCOPE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatus(), "Wrong error status");
        }

        verify(tokenIssuerService).issueClient(eq(ISSUER_URL), eq(CLIENT_ID), eq(SCOPE));
    }

    @DisplayName("Issue token of refresh grant type")
    @Test
    public void testTokenRefresh() {
        when(tokenIssuerService.refresh(any())).thenReturn(Optional.of(tokenInfo));

        TokenResponse tokenResponse = cut.token(request, response, GrantType.REFRESH_TOKEN.getTypeName(), null, REFRESH_TOKEN
                , null, CLIENT_ID, null, null, null);

        assertNotNull(tokenResponse, "There should be any result");
        assertNotNull(tokenResponse.getAccessToken(), "There should be any access token");
        assertEquals(TOKEN, tokenResponse.getAccessToken(), "Wrong access token");
        assertNotNull(tokenResponse.getRefreshToken(), "There should be any refresh token");
        assertEquals(REFRESH_TOKEN, tokenResponse.getRefreshToken(), "Wrong access token");
        assertNotNull(tokenResponse.getScope(), "There should be any scope");
        assertEquals(SCOPE, tokenResponse.getScope(), "Wrong scope");
        assertNotNull(tokenResponse.getExpiresIn(), "There should be any expiration");
        assertEquals(Long.valueOf(10), tokenResponse.getExpiresIn(), "Wrong expiration");

        verify(tokenIssuerService).refresh(eq(REFRESH_TOKEN));
    }

    @DisplayName("Issue token of refresh grant type but not successful")
    @Test
    public void testTokenRefreshNotSuccessful() {
        when(tokenIssuerService.refresh(any())).thenReturn(Optional.empty());

        try {
            cut.token(request, response, GrantType.REFRESH_TOKEN.getTypeName(), null, REFRESH_TOKEN
                    , null, CLIENT_ID, null, null, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(tokenIssuerService).refresh(eq(REFRESH_TOKEN));
    }

    @DisplayName("Issue token of refresh grant type but not successful encoding")
    @Test
    public void testTokenRefreshEncodingNotSuccessful() {
        when(tokenIssuerService.refresh(any())).thenReturn(Optional.of(tokenInfo));

        try {
            when(token.getEncodedToken()).thenThrow(new JwtGeneratingException("EncodedToken"));
        } catch (JwtGeneratingException e) {
            fail(e.getMessage());
        }

        try {
            cut.token(request, response, GrantType.REFRESH_TOKEN.getTypeName(), null, REFRESH_TOKEN
                    , null, CLIENT_ID, null, null, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatus(), "Wrong error status");
        }

        verify(tokenIssuerService).refresh(eq(REFRESH_TOKEN));
    }


    @DisplayName("Issue token a not supported grand type")
    @Test
    public void testTokenNotSupported() {
        try {
            cut.token(request, response, GrantType.NOT_SUPPORTED.getTypeName(), CODE, REFRESH_TOKEN
                    , REDIRECT_URL, CLIENT_ID, USER_ID, USER_PWD, SCOPE);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, e.getHttpStatus(), "Wrong error status");
        }

        verify(tokenIssuerService, never()).issueImplicit(any(), any(), any());
        verify(tokenIssuerService, never()).refresh(any());
        verify(tokenIssuerService, never()).issueClient(any(), any(), any());
        verify(tokenIssuerService, never()).issue(any(), any(), any(), any());
        try {
            verify(response, never()).sendRedirect(eq(REDIRECT_URL));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Introspect a valid token")
    @Test
    public void testIntrospection() {
        when(tokenIssuerService.getToken(eq(TOKEN))).thenReturn(Optional.of(token));

        IntrospectionResponse result = cut.introspection(TOKEN, null);

        assertNotNull(result, "There should be any result");
        assertEquals(USER_ID, result.getUsername(), "Wrong username");
        assertEquals(USER_ID, result.getSub(), "Wrong sub");
        assertEquals(SystemProperties.getSystemDateTime().plus(10L, ChronoUnit.SECONDS)
                .atZone(SystemProperties.getZoneId()).toEpochSecond(), result.getExp(), "Wrong expiration");

        verify(tokenIssuerService).getToken(eq(TOKEN));
    }

    @DisplayName("Introspect an invalid token")
    @Test
    public void testIntrospectionInvalid() {
        when(tokenIssuerService.getToken(eq(TOKEN))).thenReturn(Optional.empty());

        try {
            cut.introspection(TOKEN, null);
            fail("There should be an AuthTokenException");
        } catch (AuthTokenException e) {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getHttpStatus(), "Wrong error status");
        }

        verify(tokenIssuerService).getToken(eq(TOKEN));
    }
}
