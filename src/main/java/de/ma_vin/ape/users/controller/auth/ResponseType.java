package de.ma_vin.ape.users.controller.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseType {
    CODE("code"), TOKEN("token"), NOT_SUPPORTED("not_supported");
    private String typeName;

    public static ResponseType getByTypeName(String typeName) {
        for (ResponseType r : ResponseType.values()) {
            if (r.typeName.equals(typeName)) {
                return r;
            }
        }
        return NOT_SUPPORTED;
    }
}
