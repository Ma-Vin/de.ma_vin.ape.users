package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeGroupRepository extends JpaRepository<PrivilegeGroupDao, Long> {
}
