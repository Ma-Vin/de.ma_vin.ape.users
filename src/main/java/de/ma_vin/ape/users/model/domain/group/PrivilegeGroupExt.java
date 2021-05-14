package de.ma_vin.ape.users.model.domain.group;

import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDomain;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@ExtendingDomain
public class PrivilegeGroupExt extends PrivilegeGroup {

    public PrivilegeGroupExt(String groupName) {
        this();
        setGroupName(groupName);
    }
}
