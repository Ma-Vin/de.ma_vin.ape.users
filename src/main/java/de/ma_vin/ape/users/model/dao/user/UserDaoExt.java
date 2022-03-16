package de.ma_vin.ape.users.model.dao.user;

import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDao;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class UserDaoExt extends UserDao {

    public UserDaoExt(String identification) {
        this();
        setIdentification(identification);
    }
}
