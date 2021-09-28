package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrivilegeGroupRepository extends JpaRepository<PrivilegeGroupDao, Long> {

    List<PrivilegeGroupDao> findByParentCommonGroup(CommonGroupDao parentCommonGroup);

    List<PrivilegeGroupDao> findByParentCommonGroup(CommonGroupDao parentCommonGroup, Pageable pageable);

    long countByParentCommonGroup(CommonGroupDao parentCommonGroup);

    @Query(value = "SELECT pg.parent_common_group_id FROM privilege_groups pg WHERE pg.id = :groupId", nativeQuery = true)
    Optional<Long> getIdOfParentCommonGroup(@Param("groupId") Long groupId);
}
