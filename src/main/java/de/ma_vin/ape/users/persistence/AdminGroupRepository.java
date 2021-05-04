package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminGroupRepository extends JpaRepository<AdminGroupDao, Long> {
}
