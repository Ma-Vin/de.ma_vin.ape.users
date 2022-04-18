package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupToBaseGroupDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<PrivilegeGroupToBaseGroupDao> findAllByPrivilegeGroupAndFilterRole(PrivilegeGroupDao privilegeGroup, Role filterRole);

    List<PrivilegeGroupToBaseGroupDao> findAllByPrivilegeGroupAndFilterRole(PrivilegeGroupDao privilegeGroup, Role filterRole, Pageable pageable);

    List<PrivilegeGroupToBaseGroupDao> findAllByBaseGroup(BaseGroupDao baseGroup);

    long countByPrivilegeGroup(PrivilegeGroupDao privilegeGroup);

    long countByPrivilegeGroupAndFilterRole(PrivilegeGroupDao privilegeGroup, Role filterRole);

    @Query(value = "SELECT b FROM BaseGroupDao b WHERE b.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT pb FROM PrivilegeGroupToBaseGroupDao pb WHERE pb.privilegeGroup=:privilegeGroup AND pb.baseGroup=b)")
    List<BaseGroupDao> findAvailableBaseGroups(@Param("privilegeGroup") PrivilegeGroupDao privilegeGroup, @Param("commonGroup") CommonGroupDao commonGroup, Pageable pageable);

    @Query(value = "SELECT b FROM BaseGroupDao b WHERE b.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT pb FROM PrivilegeGroupToBaseGroupDao pb WHERE pb.privilegeGroup=:privilegeGroup AND pb.baseGroup=b)")
    List<BaseGroupDao> findAvailableBaseGroups(@Param("privilegeGroup") PrivilegeGroupDao privilegeGroup, @Param("commonGroup") CommonGroupDao commonGroup);

    @Query(value = "SELECT COUNT(b) as UsersCount FROM BaseGroupDao b WHERE b.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT pb FROM PrivilegeGroupToBaseGroupDao pb WHERE pb.privilegeGroup=:privilegeGroup AND pb.baseGroup=b)")
    long countAvailableBaseGroups(@Param("privilegeGroup") PrivilegeGroupDao privilegeGroup, @Param("commonGroup") CommonGroupDao commonGroup);

}
