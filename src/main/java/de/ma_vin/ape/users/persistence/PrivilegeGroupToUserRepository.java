package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupToUserDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PrivilegeGroupToUserRepository extends JpaRepository<PrivilegeGroupToUserDao, PrivilegeGroupToUserDao.PrivilegeGroupToUserId> {
    @Transactional
    long deleteByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    @Transactional
    long deleteByUser(UserDao user);

    @Transactional
    long deleteByPrivilegeGroupAndUser(PrivilegeGroupDao privilegeGroup, UserDao user);

    List<PrivilegeGroupToUserDao> findAllByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    List<PrivilegeGroupToUserDao> findAllByPrivilegeGroup(PrivilegeGroupDao privilegeGroup, Pageable pageable);

    List<PrivilegeGroupToUserDao> findAllByPrivilegeGroupAndFilterRole(PrivilegeGroupDao privilegeGroup, Role filterRole);

    List<PrivilegeGroupToUserDao> findAllByPrivilegeGroupAndFilterRole(PrivilegeGroupDao privilegeGroup, Role filterRole, Pageable pageable);

    long countByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    long countByPrivilegeGroupAndFilterRole(PrivilegeGroupDao privilegeGroup, Role filterRole);

    @Query(value = "SELECT u FROM UserDao u WHERE u.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT pu FROM PrivilegeGroupToUserDao pu WHERE pu.privilegeGroup=:privilegeGroup AND pu.user=u)")
    List<UserDao> findAvailableUsers(@Param("privilegeGroup") PrivilegeGroupDao privilegeGroup, @Param("commonGroup") CommonGroupDao commonGroup, Pageable pageable);

    @Query(value = "SELECT u FROM UserDao u WHERE u.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT pu FROM PrivilegeGroupToUserDao pu WHERE pu.privilegeGroup=:privilegeGroup AND pu.user=u)")
    List<UserDao> findAvailableUsers(@Param("privilegeGroup") PrivilegeGroupDao privilegeGroup, @Param("commonGroup") CommonGroupDao commonGroup);

    @Query(value = "SELECT COUNT(u) as UsersCount FROM UserDao u WHERE u.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT pu FROM PrivilegeGroupToUserDao pu WHERE pu.privilegeGroup=:privilegeGroup AND pu.user=u)")
    long countAvailableUsers(@Param("privilegeGroup") PrivilegeGroupDao privilegeGroup, @Param("commonGroup") CommonGroupDao commonGroup);
}
