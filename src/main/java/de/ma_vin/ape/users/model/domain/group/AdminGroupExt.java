package de.ma_vin.ape.users.model.domain.group;

import com.github.ma_vin.util.layer_generator.annotations.model.ExtendingDomain;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@ExtendingDomain
public class AdminGroupExt extends AdminGroup {

    public AdminGroupExt(String groupName) {
        super();
        setGroupName(groupName);
    }
}
