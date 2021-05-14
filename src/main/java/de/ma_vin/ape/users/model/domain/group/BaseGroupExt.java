package de.ma_vin.ape.users.model.domain.group;

import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDomain;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@ExtendingDomain
public class BaseGroupExt extends BaseGroup {

    public BaseGroupExt(String groupName) {
        this();
        setGroupName(groupName);
    }
}
