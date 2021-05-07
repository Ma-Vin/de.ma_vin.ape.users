package de.ma_vin.ape.users.model.domain.group;

import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDomain;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@ExtendingDomain
public class AdminGroupExt extends AdminGroup {

    public AdminGroupExt(String groupName) {
        super();
        setGroupName(groupName);
    }
}
