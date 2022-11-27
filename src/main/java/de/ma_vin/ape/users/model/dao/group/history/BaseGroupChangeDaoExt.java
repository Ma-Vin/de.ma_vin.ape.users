package de.ma_vin.ape.users.model.dao.group.history;

import de.ma_vin.ape.users.model.dao.user.UserDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.BaseGroupChangeDao;
import de.ma_vin.ape.utils.properties.SystemProperties;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDao;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class BaseGroupChangeDaoExt extends BaseGroupChangeDao {

    /**
     * Constructor which sets the changed group, the editor and the change time
     *
     * @param baseGroupDao         group whose changes are logged
     * @param editorIdentification identification of the editor
     */
    public BaseGroupChangeDaoExt(BaseGroupDao baseGroupDao, String editorIdentification) {
        setBaseGroup(baseGroupDao);
        setEditor(new UserDaoExt(editorIdentification));
        setChangeTime(SystemProperties.getSystemDateTime());
    }
}
