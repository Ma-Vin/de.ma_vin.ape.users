package de.ma_vin.ape.users.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Admin", 98),
    MANAGER("Manager", 20),
    CONTRIBUTOR("Contributor", 10),
    VISITOR("Visitor", 0),
    BLOCKED("Blocked", -99),
    NOT_RELEVANT("not relevant", -1);

    String description;

    int priority;

    Role(String description, int priority) {
        this.description = description;
        this.priority = priority;
    }

}