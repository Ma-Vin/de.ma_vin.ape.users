package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.dao.group.PrivilegeGroupDaoExt;
import de.ma_vin.ape.users.model.dao.group.history.CommonGroupChangeDaoExt;
import de.ma_vin.ape.users.model.dao.group.history.PrivilegeGroupChangeDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.history.PrivilegeGroupChange;
import de.ma_vin.ape.users.model.gen.mapper.GroupHistoryAccessMapper;
import de.ma_vin.ape.users.persistence.history.CommonGroupChangeRepository;
import de.ma_vin.ape.users.persistence.history.PrivilegeGroupChangeRepository;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@Log4j2
public class PrivilegeGroupChangeService extends AbstractChangeService<PrivilegeGroupDao, PrivilegeGroupChange> {

    @Autowired
    private PrivilegeGroupChangeRepository privilegeGroupChangeRepository;
    @Autowired
    private CommonGroupChangeRepository commonGroupChangeRepository;

    @Override
    public void saveCreation(PrivilegeGroupDao createdObject, String editorIdentification) {
        PrivilegeGroupChangeDaoExt change = new PrivilegeGroupChangeDaoExt(createdObject, editorIdentification);
        change.setChangeType(ChangeType.CREATE);
        privilegeGroupChangeRepository.save(change);

        CommonGroupChangeDaoExt parentChange = new CommonGroupChangeDaoExt(createdObject.getParentCommonGroup(), editorIdentification);
        parentChange.setChangeTime(change.getChangeTime());
        parentChange.setChangeType(ChangeType.ADD);
        parentChange.setPrivilegeGroup(createdObject);
        commonGroupChangeRepository.save(parentChange);
    }

    @Override
    public void saveChange(PrivilegeGroupDao updatedObject, PrivilegeGroupDao storedObject, String editorIdentification) {
        PrivilegeGroupChangeDaoExt change = new PrivilegeGroupChangeDaoExt(updatedObject, editorIdentification);
        determineChanges(updatedObject, storedObject, change);
        privilegeGroupChangeRepository.save(change);
    }

    @Override
    public void delete(PrivilegeGroupDao deletedObject, String editorIdentification) {
        PrivilegeGroupChangeDaoExt deletion = new PrivilegeGroupChangeDaoExt(null, editorIdentification);
        deletion.setDeletionInformation(deletedObject.getIdentification());
        deletion.setChangeType(ChangeType.DELETE);
        privilegeGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());
        privilegeGroupChangeRepository.save(deletion);

        CommonGroupChangeDaoExt parentChange = new CommonGroupChangeDaoExt(deletedObject.getParentCommonGroup(), editorIdentification);
        parentChange.setChangeTime(deletion.getChangeTime());
        parentChange.setChangeType(ChangeType.REMOVE);
        parentChange.setDeletionInformation(deletedObject.getIdentification());
        commonGroupChangeRepository.markedAsDeleted(deletedObject, deletedObject.getIdentification());
        commonGroupChangeRepository.save(parentChange);
    }

    @Override
    public List<PrivilegeGroupChange> loadChanges(String identification) {
        return privilegeGroupChangeRepository.findByPrivilegeGroup(new PrivilegeGroupDaoExt(identification)).stream()
                .map(pgc -> GroupHistoryAccessMapper.convertToPrivilegeGroupChange(pgc, false))
                .toList();
    }
}
