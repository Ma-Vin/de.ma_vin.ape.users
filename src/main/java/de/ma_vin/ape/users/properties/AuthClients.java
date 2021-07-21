package de.ma_vin.ape.users.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@ConfigurationProperties("auth")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthClients {

    private List<Client> clients = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Client {
        private String clientId;
        private String secret;

        /**
         * @return the secret encoded as URL and Filename safe type base64.
         */
        public String getBase64UrlEncodedSecret() {
            return Base64.getUrlEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
