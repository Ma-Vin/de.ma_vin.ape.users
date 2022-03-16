package de.ma_vin.ape.users.enums;

import lombok.Getter;

/**
 * Enum which represent general action for historization.
 */
@Getter
public enum ChangeType {
    CREATE("Create"),
    DELETE("Delete"),
    ADD("Add"),
    REMOVE("Remove"),
    MODIFY("Modify"),
    UNKNOWN("Unknown");

    final String typeName;

    /**
     * Constructor
     *
     * @param typeName The name of the change type
     */
    ChangeType(String typeName) {
        this.typeName = typeName;
    }
}
