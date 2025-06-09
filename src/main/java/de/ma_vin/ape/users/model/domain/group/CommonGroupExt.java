package de.ma_vin.ape.users.model.domain.group;

import com.github.ma_vin.util.layer_generator.annotations.model.ExtendingDomain;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@ExtendingDomain
public class CommonGroupExt extends CommonGroup {

    public CommonGroupExt(String groupName){
        this();
        setGroupName(groupName);
    }
}
