package de.ma_vin.ape.users.model.dao.group.history;

import de.ma_vin.ape.users.model.dao.user.UserDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.CommonGroupChangeDao;
import de.ma_vin.ape.utils.properties.SystemProperties;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDao;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class CommonGroupChangeDaoExt extends CommonGroupChangeDao {

    /**
     * Constructor which sets the changed group, the editor and the change time
     *
     * @param commonGroupDao       group whose changes are logged
     * @param editorIdentification identification of the editor
     */
    public CommonGroupChangeDaoExt(CommonGroupDao commonGroupDao, String editorIdentification) {
        setCommonGroup(commonGroupDao);
        setEditor(new UserDaoExt(editorIdentification));
        setChangeTime(SystemProperties.getSystemDateTime());
    }
}
