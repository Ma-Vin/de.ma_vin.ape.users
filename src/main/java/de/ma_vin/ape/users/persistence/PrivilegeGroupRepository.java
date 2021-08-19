package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrivilegeGroupRepository extends JpaRepository<PrivilegeGroupDao, Long> {

    List<PrivilegeGroupDao> findByParentCommonGroup(CommonGroupDao parentCommonGroup);

    long countByParentCommonGroup(CommonGroupDao parentCommonGroup);

    @Query(value = "SELECT pg.Parent_Common_Group_Id FROM Privilege_Groups pg WHERE pg.id = :groupId", nativeQuery = true)
    Optional<Long> getIdOfParentCommonGroup(@Param("groupId") Long groupId);
}
