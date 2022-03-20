package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.dao.group.history.BaseGroupChangeDaoExt;
import de.ma_vin.ape.users.model.dao.group.history.CommonGroupChangeDaoExt;
import de.ma_vin.ape.users.model.dao.group.history.PrivilegeGroupChangeDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.persistence.history.BaseGroupChangeRepository;
import de.ma_vin.ape.users.persistence.history.CommonGroupChangeRepository;
import de.ma_vin.ape.users.persistence.history.PrivilegeGroupChangeRepository;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j2
public class BaseGroupChangeService extends AbstractChildChangeService<BaseGroupDao, PrivilegeGroupDao, BaseGroupDao> {

    @Autowired
    private BaseGroupChangeRepository baseGroupChangeRepository;
    @Autowired
    private CommonGroupChangeRepository commonGroupChangeRepository;
    @Autowired
    private PrivilegeGroupChangeRepository privilegeGroupChangeRepository;


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
        determineChanges(updatedObject, storedObject, change);
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

        privilegeGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());
        baseGroupChangeRepository.markedSubAsDeleted(deletedObject, deletedObject.getIdentification());
    }

    @Override
    public void addToParentFirstType(BaseGroupDao toAddObject, PrivilegeGroupDao parentObject, String editorIdentification) {
        PrivilegeGroupChangeDaoExt addChange = new PrivilegeGroupChangeDaoExt(parentObject, editorIdentification);
        addChange.setChangeType(ChangeType.ADD);
        addChange.setBaseGroup(toAddObject);
        privilegeGroupChangeRepository.save(addChange);
    }

    @Override
    public void addToParentSecondType(BaseGroupDao toAddObject, BaseGroupDao parentObject, String editorIdentification) {
        BaseGroupChangeDaoExt removeChange = new BaseGroupChangeDaoExt(parentObject, editorIdentification);
        removeChange.setChangeType(ChangeType.ADD);
        removeChange.setSubBaseGroup(toAddObject);
        baseGroupChangeRepository.save(removeChange);
    }

    @Override
    public void removeFromParentFirstType(BaseGroupDao toRemoveObject, PrivilegeGroupDao parentObject, String editorIdentification) {
        PrivilegeGroupChangeDaoExt addChange = new PrivilegeGroupChangeDaoExt(parentObject, editorIdentification);
        addChange.setChangeType(ChangeType.REMOVE);
        addChange.setBaseGroup(toRemoveObject);
        privilegeGroupChangeRepository.save(addChange);
    }

    @Override
    public void removeFromParentSecondType(BaseGroupDao toRemoveObject, BaseGroupDao parentObject, String editorIdentification) {
        BaseGroupChangeDaoExt removeChange = new BaseGroupChangeDaoExt(parentObject, editorIdentification);
        removeChange.setChangeType(ChangeType.REMOVE);
        removeChange.setSubBaseGroup(toRemoveObject);
        baseGroupChangeRepository.save(removeChange);
    }
}
