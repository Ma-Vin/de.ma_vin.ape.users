package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.dao.group.history.AdminGroupChangeDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.ape.users.persistence.history.AdminGroupChangeRepository;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j2
public class AdminGroupChangeService extends AbstractChangeService<AdminGroupDao> {

    @Autowired
    private AdminGroupChangeRepository adminGroupChangeRepository;

    @Override
    public void saveCreation(AdminGroupDao createdObject, String editorIdentification) {
        AdminGroupChangeDaoExt change = new AdminGroupChangeDaoExt(createdObject);
        change.setChangeType(ChangeType.CREATE);
        adminGroupChangeRepository.save(change);
    }

    @Override
    public void saveChange(AdminGroupDao updatedObject, AdminGroupDao storedObject, String editorIdentification) {
        AdminGroupChangeDaoExt change = new AdminGroupChangeDaoExt(updatedObject, editorIdentification);
        determineChanges(updatedObject, storedObject, change);
        adminGroupChangeRepository.save(change);
    }

    @Override
    public void delete(AdminGroupDao deletedObject, String editorIdentification) {
        AdminGroupChangeDaoExt deletion = new AdminGroupChangeDaoExt(null, editorIdentification);
        deletion.setDeletionInformation(deletedObject.getIdentification());
        deletion.setChangeType(ChangeType.DELETE);
        adminGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());
        adminGroupChangeRepository.save(deletion);
    }
}
