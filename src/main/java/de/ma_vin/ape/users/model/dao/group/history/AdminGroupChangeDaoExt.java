package de.ma_vin.ape.users.model.dao.group.history;

import de.ma_vin.ape.users.model.dao.user.UserDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.AdminGroupChangeDao;
import de.ma_vin.ape.utils.properties.SystemProperties;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDao;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class AdminGroupChangeDaoExt extends AdminGroupChangeDao {
    /**
     * Constructor which sets the changed group, the editor and the change time
     *
     * @param adminGroupDao group whose changes are logged
     */
    public AdminGroupChangeDaoExt(AdminGroupDao adminGroupDao) {
        setAdminGroup(adminGroupDao);
        setChangeTime(SystemProperties.getSystemDateTime());
    }

    /**
     * Constructor which sets the changed group, the editor and the change time
     *
     * @param adminGroupDao        group whose changes are logged
     * @param editorIdentification identification of the editor
     */
    public AdminGroupChangeDaoExt(AdminGroupDao adminGroupDao, String editorIdentification) {
        this(adminGroupDao);
        setEditor(new UserDaoExt(editorIdentification));
    }
}
