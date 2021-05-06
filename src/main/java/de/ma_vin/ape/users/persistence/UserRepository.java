package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDao, Long> {

    List<UserDao> findByParentAdminGroup(AdminGroupDao parentAdminGroup);

    List<UserDao> findByParentCommonGroup(CommonGroupDao parentCommonGroup);

    @Query(value = "SELECT u.Parent_Admin_Group_Id FROM Users u WHERE u.id = :userId", nativeQuery = true)
    Optional<Long> getIdOfParentAdminGroup(@Param("userId") Long userId);

    @Query(value = "SELECT u.Parent_Common_Group_Id FROM Users u WHERE u.id = :userId", nativeQuery = true)
    Optional<Long> getIdOfParentCommonGroup(@Param("userId") Long userId);
}
