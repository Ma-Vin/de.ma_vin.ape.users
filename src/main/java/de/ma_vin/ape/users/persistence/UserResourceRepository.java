package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.resource.UserResourceDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserResourceRepository extends JpaRepository<UserResourceDao, Long> {
}
