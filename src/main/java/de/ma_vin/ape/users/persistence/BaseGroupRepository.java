package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BaseGroupRepository extends JpaRepository<BaseGroupDao, Long> {

    List<BaseGroupDao> findByParentCommonGroup(CommonGroupDao parentCommonGroup);

    @Query(value = "SELECT bg.Parent_Common_Group_Id FROM BaseGroups bg WHERE bg.id = :groupId", nativeQuery = true)
    Optional<Long> getIdOfParentCommonGroup(@Param("groupId") Long groupId);
}
