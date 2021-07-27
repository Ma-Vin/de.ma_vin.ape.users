package de.ma_vin.ape.users.controller.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrantType {
    AUTHORIZATION_CODE("authorization_code"), IMPLICIT("implicit"), PASSWORD("password"),
    CLIENT_CREDENTIALS("client_credentials"), REFRESH_TOKEN("refresh_token"), NOT_SUPPORTED("not_supported");
    private String typeName;

    public static GrantType getByTypeName(String typeName) {
        for (GrantType g : GrantType.values()) {
            if (g.typeName.equals(typeName)) {
                return g;
            }
        }
        return NOT_SUPPORTED;
    }
}
