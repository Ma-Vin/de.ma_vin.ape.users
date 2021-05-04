package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonGroupRepository extends JpaRepository<CommonGroupDao, Long> {
}
