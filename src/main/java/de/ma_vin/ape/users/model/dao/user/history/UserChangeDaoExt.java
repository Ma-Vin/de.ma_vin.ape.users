package de.ma_vin.ape.users.model.dao.user.history;

import de.ma_vin.ape.users.model.dao.user.UserDaoExt;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.dao.user.history.UserChangeDao;
import de.ma_vin.ape.utils.properties.SystemProperties;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDao;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class UserChangeDaoExt extends UserChangeDao {

    /**
     * Constructor which sets the changed user, the editor and the change time
     *
     * @param userDao              user whose changes are logged
     * @param editorIdentification identification of the editor
     */
    public UserChangeDaoExt(UserDao userDao, String editorIdentification) {
        setUser(userDao);
        setEditor(new UserDaoExt(editorIdentification));
        setChangeTime(SystemProperties.getSystemDateTime());
    }
}
