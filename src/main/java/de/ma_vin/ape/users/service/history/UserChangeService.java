package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.dao.group.history.AdminGroupChangeDaoExt;
import de.ma_vin.ape.users.model.dao.group.history.BaseGroupChangeDaoExt;
import de.ma_vin.ape.users.model.dao.group.history.CommonGroupChangeDaoExt;
import de.ma_vin.ape.users.model.dao.group.history.PrivilegeGroupChangeDaoExt;
import de.ma_vin.ape.users.model.dao.user.history.UserChangeDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.persistence.history.*;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@Log4j2
public class UserChangeService extends AbstractChildChangeService<UserDao, PrivilegeGroupDao, BaseGroupDao> {

    @Autowired
    private UserChangeRepository userChangeRepository;
    @Autowired
    private AdminGroupChangeRepository adminGroupChangeRepository;
    @Autowired
    private CommonGroupChangeRepository commonGroupChangeRepository;
    @Autowired
    private BaseGroupChangeRepository baseGroupChangeRepository;
    @Autowired
    private PrivilegeGroupChangeRepository privilegeGroupChangeRepository;

    @Override
    public void saveCreation(UserDao createdObject, String editorIdentification) {
        UserChangeDaoExt change = new UserChangeDaoExt(createdObject, editorIdentification);
        change.setChangeType(ChangeType.CREATE);
        userChangeRepository.save(change);

        if (createdObject.getParentCommonGroup() != null) {
            CommonGroupChangeDaoExt parentChange = new CommonGroupChangeDaoExt(createdObject.getParentCommonGroup(), editorIdentification);
            parentChange.setChangeTime(change.getChangeTime());
            parentChange.setChangeType(ChangeType.ADD);
            parentChange.setUser(createdObject);
            commonGroupChangeRepository.save(parentChange);
        }
        if (createdObject.getParentAdminGroup() != null) {
            AdminGroupChangeDaoExt parentChange = new AdminGroupChangeDaoExt(createdObject.getParentAdminGroup(), editorIdentification);
            parentChange.setChangeTime(change.getChangeTime());
            parentChange.setChangeType(ChangeType.ADD);
            parentChange.setAdmin(createdObject);
            adminGroupChangeRepository.save(parentChange);
        }
    }

    @Override
    public void saveChange(UserDao updatedObject, UserDao storedObject, String editorIdentification) {
        UserChangeDaoExt change = new UserChangeDaoExt(updatedObject, editorIdentification);
        determineChanges(updatedObject, storedObject, change);
        userChangeRepository.save(change);
    }

    @Override
    public void delete(UserDao deletedObject, String editorIdentification) {
        UserChangeDaoExt deletion = new UserChangeDaoExt(null, editorIdentification);
        deletion.setDeletionInformation(deletedObject.getIdentification());
        deletion.setChangeType(ChangeType.DELETE);
        userChangeRepository.save(deletion);
        userChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());

        if (deletedObject.getParentCommonGroup() != null) {
            CommonGroupChangeDaoExt parentChange = new CommonGroupChangeDaoExt(deletedObject.getParentCommonGroup(), editorIdentification);
            parentChange.setChangeTime(deletion.getChangeTime());
            parentChange.setChangeType(ChangeType.REMOVE);
            parentChange.setDeletionInformation(deletedObject.getIdentification());
            commonGroupChangeRepository.save(parentChange);
        }
        commonGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());

        if (deletedObject.getParentAdminGroup() != null) {
            AdminGroupChangeDaoExt parentChange = new AdminGroupChangeDaoExt(deletedObject.getParentAdminGroup(), editorIdentification);
            parentChange.setChangeTime(deletion.getChangeTime());
            parentChange.setChangeType(ChangeType.REMOVE);
            parentChange.setDeletionInformation(deletedObject.getIdentification());
            adminGroupChangeRepository.save(parentChange);
        }
        adminGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());

        privilegeGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());
        baseGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());

        String deletedObjectIdentification = deletedObject.getIdentification();
        adminGroupChangeRepository.markedEditorAsDeleted(deletedObject, deletedObjectIdentification);
        commonGroupChangeRepository.markedEditorAsDeleted(deletedObject, deletedObjectIdentification);
        baseGroupChangeRepository.markedEditorAsDeleted(deletedObject, deletedObjectIdentification);
        privilegeGroupChangeRepository.markedEditorAsDeleted(deletedObject, deletedObjectIdentification);
        userChangeRepository.markedEditorAsDeleted(deletedObject, deletedObjectIdentification);
    }

    @Override
    public void addToParentFirstType(UserDao toAddObject, PrivilegeGroupDao parentObject, String editorIdentification) {
        PrivilegeGroupChangeDaoExt addChange = new PrivilegeGroupChangeDaoExt(parentObject, editorIdentification);
        addChange.setChangeType(ChangeType.ADD);
        addChange.setUser(toAddObject);
        privilegeGroupChangeRepository.save(addChange);
    }

    @Override
    public void addToParentSecondType(UserDao toAddObject, BaseGroupDao parentObject, String editorIdentification) {
        BaseGroupChangeDaoExt addChange = new BaseGroupChangeDaoExt(parentObject, editorIdentification);
        addChange.setChangeType(ChangeType.ADD);
        addChange.setUser(toAddObject);
        baseGroupChangeRepository.save(addChange);
    }

    @Override
    public void removeFromParentFirstType(UserDao toRemoveObject, PrivilegeGroupDao parentObject, String editorIdentification) {
        PrivilegeGroupChangeDaoExt removeChange = new PrivilegeGroupChangeDaoExt(parentObject, editorIdentification);
        removeChange.setChangeType(ChangeType.REMOVE);
        removeChange.setUser(toRemoveObject);
        privilegeGroupChangeRepository.save(removeChange);
    }

    @Override
    public void removeFromParentSecondType(UserDao toRemoveObject, BaseGroupDao parentObject, String editorIdentification) {
        BaseGroupChangeDaoExt removeChange = new BaseGroupChangeDaoExt(parentObject, editorIdentification);
        removeChange.setChangeType(ChangeType.REMOVE);
        removeChange.setUser(toRemoveObject);
        baseGroupChangeRepository.save(removeChange);
    }
}
