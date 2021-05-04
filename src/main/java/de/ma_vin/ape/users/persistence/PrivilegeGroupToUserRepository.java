package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupToUserDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeGroupToUserRepository extends JpaRepository<PrivilegeGroupToUserDao, PrivilegeGroupToUserDao.PrivilegeGroupToUserId> {
}
