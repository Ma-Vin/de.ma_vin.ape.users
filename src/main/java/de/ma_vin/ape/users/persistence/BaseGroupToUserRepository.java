package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupToUserDao;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BaseGroupToUserRepository extends JpaRepository<BaseGroupToUserDao, BaseGroupToUserDao.BaseGroupToUserId> {
    @Transactional
    long deleteByBaseGroup(BaseGroupDao baseGroup);

    @Transactional
    long deleteByBaseGroupAndUser(BaseGroupDao baseGroup, UserDao user);

    List<BaseGroupToUserDao> findAllByBaseGroup(BaseGroupDao baseGroup);

    List<BaseGroupToUserDao> findAllByBaseGroup(BaseGroupDao baseGroup, Pageable pageable);

    long countByBaseGroup(BaseGroupDao baseGroup);

    @Query(value = "SELECT u FROM UserDao u WHERE u.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT bu FROM BaseGroupToUserDao bu WHERE bu.baseGroup=:baseGroup AND bu.user=u)")
    List<UserDao> findAvailableUsers(@Param("baseGroup") BaseGroupDao baseGroup, @Param("commonGroup") CommonGroupDao commonGroup, Pageable pageable);

    @Query(value = "SELECT u FROM UserDao u WHERE u.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT bu FROM BaseGroupToUserDao bu WHERE bu.baseGroup=:baseGroup AND bu.user=u)")
    List<UserDao> findAvailableUsers(@Param("baseGroup") BaseGroupDao baseGroup, @Param("commonGroup") CommonGroupDao commonGroup);

    @Query(value = "SELECT COUNT(u) as UsersCount FROM UserDao u WHERE u.parentCommonGroup=:commonGroup"
            + " AND NOT EXISTS(SELECT bu FROM BaseGroupToUserDao bu WHERE bu.baseGroup=:baseGroup AND bu.user=u)")
    long countAvailableUsers(@Param("baseGroup") BaseGroupDao baseGroup, @Param("commonGroup") CommonGroupDao commonGroup);
}
