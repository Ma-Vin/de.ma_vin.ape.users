package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupToBaseGroupDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
