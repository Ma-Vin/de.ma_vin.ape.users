package de.ma_vin.ape.users.model.dao.group.history;

import de.ma_vin.ape.users.model.dao.user.UserDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.PrivilegeGroupChangeDao;
import de.ma_vin.ape.utils.properties.SystemProperties;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDao;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@ExtendingDao
@NoArgsConstructor
public class PrivilegeGroupChangeDaoExt extends PrivilegeGroupChangeDao {

    /**
     * Constructor which sets the changed group, the editor and the change time
     *
     * @param privilegeGroupDao    group whose changes are logged
     * @param editorIdentification identification of the editor
     */
    public PrivilegeGroupChangeDaoExt(PrivilegeGroupDao privilegeGroupDao, String editorIdentification) {
        setPrivilegeGroup(privilegeGroupDao);
        setEditor(new UserDaoExt(editorIdentification));
        setChangeTime(SystemProperties.getSystemDateTime());
    }
}
