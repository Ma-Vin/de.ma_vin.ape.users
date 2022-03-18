package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.dao.group.history.BaseGroupChangeDaoExt;
import de.ma_vin.ape.users.model.dao.group.history.CommonGroupChangeDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.persistence.history.BaseGroupChangeRepository;
import de.ma_vin.ape.users.persistence.history.CommonGroupChangeRepository;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j2
public class BaseGroupChangeService extends AbstractChangeService<BaseGroupDao> {

    @Autowired
    private BaseGroupChangeRepository baseGroupChangeRepository;
    @Autowired
    private CommonGroupChangeRepository commonGroupChangeRepository;

    @Override
    public void saveCreation(BaseGroupDao createdObject, String editorIdentification) {
        BaseGroupChangeDaoExt change = new BaseGroupChangeDaoExt(createdObject, editorIdentification);
        change.setChangeType(ChangeType.CREATE);
        baseGroupChangeRepository.save(change);

        CommonGroupChangeDaoExt parentChange = new CommonGroupChangeDaoExt(createdObject.getParentCommonGroup(), editorIdentification);
        parentChange.setChangeTime(change.getChangeTime());
        parentChange.setChangeType(ChangeType.ADD);
        parentChange.setBaseGroup(createdObject);
        commonGroupChangeRepository.save(parentChange);
    }

    @Override
    public void saveChange(BaseGroupDao updatedObject, BaseGroupDao storedObject, String editorIdentification) {
        BaseGroupChangeDaoExt change = new BaseGroupChangeDaoExt(updatedObject, editorIdentification);
        change.setChangeType(ChangeType.MODIFY);
        change.setAction(determineDiffAsText(updatedObject, storedObject));
        if (change.getAction().isEmpty()) {
            log.warn("There was tried to store a base group {} where no diff could be determined", updatedObject.getIdentification());
            change.setChangeType(ChangeType.UNKNOWN);
            change.setAction(null);
        }
        baseGroupChangeRepository.save(change);
    }

    @Override
    public void delete(BaseGroupDao deletedObject, String editorIdentification) {
        BaseGroupChangeDaoExt deletion = new BaseGroupChangeDaoExt(null, editorIdentification);
        deletion.setDeletionInformation(deletedObject.getIdentification());
        deletion.setChangeType(ChangeType.DELETE);
        baseGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());
        baseGroupChangeRepository.save(deletion);

        CommonGroupChangeDaoExt parentChange = new CommonGroupChangeDaoExt(deletedObject.getParentCommonGroup(), editorIdentification);
        parentChange.setChangeTime(deletion.getChangeTime());
        parentChange.setChangeType(ChangeType.REMOVE);
        parentChange.setDeletionInformation(deletedObject.getIdentification());
        commonGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());
        commonGroupChangeRepository.save(parentChange);
    }
}
