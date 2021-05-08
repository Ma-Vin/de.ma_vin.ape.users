package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupToUserDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface BaseGroupToUserRepository extends JpaRepository<BaseGroupToUserDao, BaseGroupToUserDao.BaseGroupToUserId> {
    @Transactional
    long deleteByBaseGroup(BaseGroupDao baseGroup);

    @Transactional
    long deleteByBaseGroupAndUser(BaseGroupDao baseGroup, UserDao user);
}
