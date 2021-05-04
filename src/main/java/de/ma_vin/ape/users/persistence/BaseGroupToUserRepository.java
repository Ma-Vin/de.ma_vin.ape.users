package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupToUserDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseGroupToUserRepository extends JpaRepository<BaseGroupToUserDao, BaseGroupToUserDao.BaseGroupToUserId> {
}
