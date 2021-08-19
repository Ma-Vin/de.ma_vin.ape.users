package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupToUserDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PrivilegeGroupToUserRepository extends JpaRepository<PrivilegeGroupToUserDao, PrivilegeGroupToUserDao.PrivilegeGroupToUserId> {
    @Transactional
    long deleteByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    @Transactional
    long deleteByPrivilegeGroupAndUser(PrivilegeGroupDao privilegeGroup, UserDao user);

    List<PrivilegeGroupToUserDao> findAllByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    long countByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    long countByPrivilegeGroupAndFilterRole(PrivilegeGroupDao privilegeGroup, Role filterRole);
}
