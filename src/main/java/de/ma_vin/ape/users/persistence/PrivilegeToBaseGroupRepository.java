package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupToBaseGroupDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeToBaseGroupRepository extends JpaRepository<PrivilegeGroupToBaseGroupDao, PrivilegeGroupToBaseGroupDao.PrivilegeGroupToBaseGroupId> {
}
