package de.ma_vin.ape.users.model.domain.group;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDomain;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
@ExtendingDomain
public class PrivilegeGroupExt extends PrivilegeGroup {

    public PrivilegeGroupExt(String groupName) {
        this();
        setGroupName(groupName);
    }

    /**
     * Determines all direct and indirect admins of this privilege group
     *
     * @return The set of all users
     */
    public Set<User> getAllAdmins() {
        return getAll(this::getAdminGroups, this::getAdmins, Role.ADMIN);
    }

    /**
     * Determines all direct and indirect managers of this privilege group
     * <br>
     * Every indirect role will be overridden by a direct one
     *
     * @return The set of all users
     */
    public Set<User> getAllManagers() {
        return getAll(this::getManagerGroups, this::getManagers, Role.MANAGER);
    }

    /**
     * Determines all direct and indirect contributors of this privilege group
     * <br>
     * Every indirect role will be overridden by a direct one
     *
     * @return The set of all users
     */
    public Set<User> getAllContributors() {
        return getAll(this::getContributorGroups, this::getContributors, Role.CONTRIBUTOR);
    }

    /**
     * Determines all direct and indirect visitors of this privilege group
     * <br>
     * Every indirect role will be overridden by a direct one
     *
     * @return The set of all users
     */
    public Set<User> getAllVisitors() {
        return getAll(this::getVisitorGroups, this::getVisitors, Role.VISITOR);
    }

    /**
     * Determines all direct and indirect blocked users of this privilege group
     * <br>
     * Every indirect role will be overridden by a direct one
     *
     * @return The set of all users
     */
    public Set<User> getAllBlocks() {
        return getAll(this::getBlockGroups, this::getBlocks, Role.BLOCKED);
    }

    /**
     * Determines all direct and indirect users with a role at this privilege group
     * <br>
     * Every indirect role will be overridden by a direct one
     *
     * @param groupGetter Functional interface to get base groups corresponding to the given role
     * @param userGetter  Functional interface to get user corresponding to the given role
     * @param role        role which is to determine
     * @return The set of all users
     */
    private Set<User> getAll(GroupGetter groupGetter, UserGetter userGetter, Role role) {
        Set<User> result = new TreeSet<>(Comparator.comparing(User::getIdentification));
        groupGetter.get().forEach(ag -> result.addAll(((BaseGroupExt) ag).getAllUsers()));
        result.removeAll(getAllIndirectUsersWithHigherRole(role));

        result.addAll(userGetter.get());
        return result;
    }

    /**
     * Determines all indirect users with a higher role than the given one
     *
     * @param role role for reference
     * @return Set of users who have a higher role than the given one
     */
    private Set<User> getAllIndirectUsersWithHigherRole(Role role) {
        Set<User> result = new TreeSet<>(Comparator.comparing(User::getIdentification));

        addUsersIfRoleIsHigher(this::getAdminGroups, Role.ADMIN, role, result);
        addUsersIfRoleIsHigher(this::getManagerGroups, Role.MANAGER, role, result);
        addUsersIfRoleIsHigher(this::getContributorGroups, Role.CONTRIBUTOR, role, result);
        addUsersIfRoleIsHigher(this::getVisitorGroups, Role.VISITOR, role, result);
        addUsersIfRoleIsHigher(this::getBlockGroups, Role.BLOCKED, role, result);

        return result;
    }

    /**
     * Adds the users of a base group if its role has a higher priority than a given one
     *
     * @param groupGetter  Functional interface to get base groups corresponding to the given role
     * @param roleOfGroups Role of the groups get by the group getter
     * @param givenRole    reference role
     * @param result       Set where to add user to
     */
    private void addUsersIfRoleIsHigher(GroupGetter groupGetter, Role roleOfGroups, Role givenRole, Set<User> result) {
        if (roleOfGroups.getPriority() > givenRole.getPriority()) {
            groupGetter.get().forEach(ag -> result.addAll(((BaseGroupExt) ag).getAllUsers()));
        }
    }

    /**
     * Determines the users of a given role
     *
     * @param role              role to return
     * @param dissolveSubgroups {@code true} if indirect users should also be considered
     * @return Collection of users with the role asked for
     */
    public Collection<User> getUsersByRole(Role role, Boolean dissolveSubgroups) {
        return switch (role) {
            case ADMIN -> Boolean.TRUE.equals(dissolveSubgroups) ? getAllAdmins() : getAdmins();
            case MANAGER -> Boolean.TRUE.equals(dissolveSubgroups) ? getAllManagers() : getManagers();
            case CONTRIBUTOR -> Boolean.TRUE.equals(dissolveSubgroups) ? getAllContributors() : getContributors();
            case VISITOR -> Boolean.TRUE.equals(dissolveSubgroups) ? getAllVisitors() : getVisitors();
            case BLOCKED -> Boolean.TRUE.equals(dissolveSubgroups) ? getAllBlocks() : getBlocks();
            case NOT_RELEVANT -> Collections.emptySet();
        };
    }

    @FunctionalInterface
    private interface GroupGetter {
        Collection<BaseGroup> get();
    }

    @FunctionalInterface
    private interface UserGetter {
        Collection<User> get();
    }
}
