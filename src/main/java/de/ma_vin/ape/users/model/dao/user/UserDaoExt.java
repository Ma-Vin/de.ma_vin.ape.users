package de.ma_vin.ape.users.model.dao.user;

import com.github.ma_vin.util.layer_generator.annotations.model.ExtendingDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class UserDaoExt extends UserDao {

    public UserDaoExt(String identification) {
        this();
        setIdentification(identification);
    }
}
