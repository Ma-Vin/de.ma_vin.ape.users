package de.ma_vin.ape.users.security.filter;

import de.ma_vin.ape.users.properties.AuthClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * {@link ClientCheckFilter} is the class under test
 */
public class ClientCheckFilterTest {
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String CLIENT_URL = "http://localhost:8080/";
    public static final String CLIENT_SECRET_ENCODED = Base64.getUrlEncoder().encodeToString(CLIENT_SECRET.getBytes(StandardCharsets.UTF_8));

    private ClientCheckFilter cut;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private AutoCloseable openMocks;
    private AuthClients authClients;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new ClientCheckFilter();
        authClients = new AuthClients();
        authClients.setTokenWithClientSecret(true);
        authClients.setClients(new ArrayList<>());
        authClients.getClients().add(new AuthClients.Client(CLIENT_ID, CLIENT_SECRET, CLIENT_URL, Collections.emptyList()));
        cut.setAuthClients(authClients);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Do filter successfully")
    @Test
    public void testDoFilterInternalSuccessfully() {
        when(request.getParameter(eq("client_id"))).thenReturn(CLIENT_ID);
        when(request.getParameter(eq("client_secret"))).thenReturn(CLIENT_SECRET_ENCODED);
        try {
            cut.doFilterInternal(request, response, filterChain);
            verify(response, never()).sendError(anyInt(), anyString());
            verify(filterChain).doFilter(eq(request), eq(response));
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Do filter missing client id")
    @Test
    public void testDoFilterInternalMissingClientId() {
        when(request.getParameter(eq("client_secret"))).thenReturn(CLIENT_SECRET_ENCODED);
        try {
            cut.doFilterInternal(request, response, filterChain);
            verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
            verify(filterChain, never()).doFilter(any(), any());
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Do filter missing client secret")
    @Test
    public void testDoFilterInternalMissingClientSecret() {
        when(request.getParameter(eq("client_id"))).thenReturn(CLIENT_ID);
        try {
            cut.doFilterInternal(request, response, filterChain);
            verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
            verify(filterChain, never()).doFilter(any(), any());
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Do filter missing client secret, but not necessary")
    @Test
    public void testDoFilterInternalMissingClientSecretButNotNecessary() {
        authClients.setTokenWithClientSecret(false);
        when(request.getParameter(eq("client_id"))).thenReturn(CLIENT_ID);
        try {
            cut.doFilterInternal(request, response, filterChain);
            verify(response, never()).sendError(anyInt(), anyString());
            verify(filterChain).doFilter(eq(request), eq(response));
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Do filter unknown client id")
    @Test
    public void testDoFilterInternalUnknownClientId() {
        when(request.getParameter(eq("client_id"))).thenReturn(CLIENT_ID + "_unknown");
        when(request.getParameter(eq("client_secret"))).thenReturn(CLIENT_SECRET_ENCODED);
        try {
            cut.doFilterInternal(request, response, filterChain);
            verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
            verify(filterChain, never()).doFilter(any(), any());
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }

    @DisplayName("Do filter invalid client secret")
    @Test
    public void testDoFilterInternalInvalidClientSecret() {
        when(request.getParameter(eq("client_id"))).thenReturn(CLIENT_ID);
        when(request.getParameter(eq("client_secret"))).thenReturn(Base64.getUrlEncoder().encodeToString((CLIENT_SECRET + "_mod").getBytes(StandardCharsets.UTF_8)));
        try {
            cut.doFilterInternal(request, response, filterChain);
            verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
            verify(filterChain, never()).doFilter(any(), any());
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }
}
