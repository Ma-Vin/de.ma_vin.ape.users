package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupToBaseGroupDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PrivilegeToBaseGroupRepository extends JpaRepository<PrivilegeGroupToBaseGroupDao, PrivilegeGroupToBaseGroupDao.PrivilegeGroupToBaseGroupId> {
    @Transactional
    long deleteByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    @Transactional
    long deleteByBaseGroup(BaseGroupDao baseGroup);

    @Transactional
    long deleteByPrivilegeGroupAndBaseGroup(PrivilegeGroupDao privilegeGroup, BaseGroupDao baseGroup);

    List<PrivilegeGroupToBaseGroupDao> findAllByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    List<PrivilegeGroupToBaseGroupDao> findAllByPrivilegeGroup(PrivilegeGroupDao privilegeGroup, Pageable pageable);

    long countByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);
}
