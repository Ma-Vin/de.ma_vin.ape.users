package de.ma_vin.ape.users.model.domain.group;

import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDomain;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@ExtendingDomain
public class CommonGroupExt extends CommonGroup {

    public CommonGroupExt(String groupName){
        this();
        setGroupName(groupName);
    }
}
