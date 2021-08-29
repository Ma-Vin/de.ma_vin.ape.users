package de.ma_vin.ape.users.model.dao.group;

import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDao;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class AdminGroupDaoExt extends AdminGroupDao {

    public AdminGroupDaoExt(String identification) {
        this();
        setIdentification(identification);
    }
}
