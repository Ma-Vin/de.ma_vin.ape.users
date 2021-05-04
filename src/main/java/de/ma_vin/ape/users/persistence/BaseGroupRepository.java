package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseGroupRepository extends JpaRepository<BaseGroupDao, Long> {
}
