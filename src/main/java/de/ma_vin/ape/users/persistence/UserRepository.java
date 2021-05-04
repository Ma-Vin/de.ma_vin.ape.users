package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserDao, Long> {
}
