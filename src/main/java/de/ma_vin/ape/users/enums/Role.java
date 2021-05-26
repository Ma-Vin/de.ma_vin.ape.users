package de.ma_vin.ape.users.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Admin", 98, 98),
    MANAGER("Manager", 20, 20),
    CONTRIBUTOR("Contributor", 10, 10),
    VISITOR("Visitor", 0, 0),
    BLOCKED("Blocked", 90, -99),
    NOT_RELEVANT("not relevant", -1, -1);

    String description;

    int priority;
    int level;

    /**
     * Constructor
     *
     * @param description Descriptional name
     * @param priority    higher priority role should override role with a lower one
     * @param level       privilege level of the role
     */
    Role(String description, int priority, int level) {
        this.description = description;
        this.priority = priority;
        this.level = level;
    }

}