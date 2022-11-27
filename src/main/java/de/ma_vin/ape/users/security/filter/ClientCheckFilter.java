package de.ma_vin.ape.users.security.filter;

import de.ma_vin.ape.users.properties.AuthClients;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@Data
@Log4j2
public class ClientCheckFilter extends OncePerRequestFilter {

    @Autowired
    AuthClients authClients;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String clientId = request.getParameter("client_id");
        String clientSecret = request.getParameter("client_secret");

        if (clientId == null || (clientSecret == null && authClients.isTokenWithClientSecret())) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing client id or secret");
            return;
        }

        Optional<AuthClients.Client> client = authClients.getClients().stream()
                .filter(c -> c.getClientId().equals(clientId)).findFirst();

        if (client.isEmpty()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, String.format("The client with id \"%s\" is not known", clientId));
            return;
        }

        if (authClients.isTokenWithClientSecret() && !client.get().getBase64UrlEncodedSecret().equals(clientSecret)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, String.format("Invalid secret for client with id \"%s\"", clientId));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int code, String messageText) throws IOException {
        log.error("Send error status {} with message: \"{}\"", code, messageText);
        response.sendError(code, messageText);
    }

}
