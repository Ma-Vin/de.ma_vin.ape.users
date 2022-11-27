package de.ma_vin.ape.users.model.dao.group;

import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDao;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class PrivilegeGroupDaoExt extends PrivilegeGroupDao {
    public PrivilegeGroupDaoExt(String identification) {
        this();
        setIdentification(identification);
    }
}
