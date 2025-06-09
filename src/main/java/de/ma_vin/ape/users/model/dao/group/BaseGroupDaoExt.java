package de.ma_vin.ape.users.model.dao.group;

import com.github.ma_vin.util.layer_generator.annotations.model.ExtendingDao;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class BaseGroupDaoExt extends BaseGroupDao {

    public BaseGroupDaoExt(String identification) {
        this();
        setIdentification(identification);
    }
}
