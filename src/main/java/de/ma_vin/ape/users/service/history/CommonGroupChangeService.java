package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.dao.group.history.CommonGroupChangeDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.persistence.history.CommonGroupChangeRepository;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j2
public class CommonGroupChangeService extends AbstractChangeService<CommonGroupDao> {

    @Autowired
    private CommonGroupChangeRepository commonGroupChangeRepository;

    @Override
    public void saveCreation(CommonGroupDao createdObject, String editorIdentification) {
        CommonGroupChangeDaoExt change = new CommonGroupChangeDaoExt(createdObject, editorIdentification);
        change.setChangeType(ChangeType.CREATE);
        commonGroupChangeRepository.save(change);
    }

    @Override
    public void saveChange(CommonGroupDao updatedObject, CommonGroupDao storedObject, String editorIdentification) {
        CommonGroupChangeDaoExt change = new CommonGroupChangeDaoExt(updatedObject, editorIdentification);
        determineChanges(updatedObject, storedObject, change);
        commonGroupChangeRepository.save(change);
    }

    @Override
    public void delete(CommonGroupDao deletedObject, String editorIdentification) {
        CommonGroupChangeDaoExt deletion = new CommonGroupChangeDaoExt(null, editorIdentification);
        deletion.setDeletionInformation(deletedObject.getIdentification());
        deletion.setChangeType(ChangeType.DELETE);
        commonGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());
        commonGroupChangeRepository.save(deletion);
    }
}
