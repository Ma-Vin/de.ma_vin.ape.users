package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupToBaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BaseToBaseGroupRepository extends JpaRepository<BaseGroupToBaseGroupDao, BaseGroupToBaseGroupDao.BaseGroupToBaseGroupId> {
    @Transactional
    long deleteByBaseGroup(BaseGroupDao baseGroup);

    @Transactional
    long deleteBySubBaseGroup(BaseGroupDao subBaseGroup);

    @Transactional
    long deleteByBaseGroupAndSubBaseGroup(BaseGroupDao baseGroup, BaseGroupDao subBaseGroup);

    List<BaseGroupToBaseGroupDao> findAllByBaseGroup(BaseGroupDao baseGroup);

    List<BaseGroupToBaseGroupDao> findAllByBaseGroup(BaseGroupDao baseGroup, Pageable pageable);

    long countByBaseGroup(BaseGroupDao baseGroup);

    @Query(value = "SELECT b FROM BaseGroupDao b WHERE b.parentCommonGroup=:commonGroup AND b!=:baseGroup"
            + " AND NOT EXISTS(SELECT bb FROM BaseGroupToBaseGroupDao bb WHERE bb.baseGroup=:baseGroup AND bb.subBaseGroup=b)")
    List<BaseGroupDao> findAvailableBaseGroups(@Param("baseGroup") BaseGroupDao baseGroup, @Param("commonGroup") CommonGroupDao commonGroup, Pageable pageable);

    @Query(value = "SELECT b FROM BaseGroupDao b WHERE b.parentCommonGroup=:commonGroup AND b!=:baseGroup"
            + " AND NOT EXISTS(SELECT bb FROM BaseGroupToBaseGroupDao bb WHERE bb.baseGroup=:baseGroup AND bb.subBaseGroup=b)")
    List<BaseGroupDao> findAvailableBaseGroups(@Param("baseGroup") BaseGroupDao baseGroup, @Param("commonGroup") CommonGroupDao commonGroup);

    @Query(value = "SELECT COUNT(b) as UsersCount FROM BaseGroupDao b WHERE b.parentCommonGroup=:commonGroup AND b!=:baseGroup"
            + " AND NOT EXISTS(SELECT bb FROM BaseGroupToBaseGroupDao bb WHERE bb.baseGroup=:baseGroup AND bb.subBaseGroup=b)")
    long countAvailableBaseGroups(@Param("baseGroup") BaseGroupDao baseGroup, @Param("commonGroup") CommonGroupDao commonGroup);
}
