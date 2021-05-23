package de.ma_vin.ape.users.model.domain.group;

import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDomain;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

@NoArgsConstructor
@ExtendingDomain
public class BaseGroupExt extends BaseGroup {

    public BaseGroupExt(String groupName) {
        this();
        setGroupName(groupName);
    }

    /**
     * Determines all direct and indirect users of this base group
     *
     * @return The set of all users
     */
    public Set<User> getAllUsers() {
        Set<User> result = new TreeSet<>(Comparator.comparing(User::getIdentification));
        getSubBaseGroups().forEach(b -> result.addAll(((BaseGroupExt) b).getAllUsers()));
        result.addAll(getUsers());
        return result;
    }
}
