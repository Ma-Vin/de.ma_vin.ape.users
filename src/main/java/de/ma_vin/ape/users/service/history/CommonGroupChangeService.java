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

    /**
     * Store a creation event of a common group
     *
     * @param commonGroupDao       the common group which was created
     * @param editorIdentification the identification of the creator
     */
    public void saveCreation(CommonGroupDao commonGroupDao, String editorIdentification) {
        CommonGroupChangeDaoExt change = new CommonGroupChangeDaoExt(commonGroupDao, editorIdentification);
        change.setChangeType(ChangeType.CREATE);
        commonGroupChangeRepository.save(change);
    }

    /**
     * Stores a modification of an existing common group
     *
     * @param updatedCommonGroupDao the common group after changes
     * @param storedCommonGroupDao  the common group before changes
     * @param editorIdentification  the identification of the modifier
     */
    public void saveChange(CommonGroupDao updatedCommonGroupDao, CommonGroupDao storedCommonGroupDao, String editorIdentification) {
        CommonGroupChangeDaoExt change = new CommonGroupChangeDaoExt(updatedCommonGroupDao, editorIdentification);
        change.setChangeType(ChangeType.MODIFY);
        change.setAction(determineDiffAsText(updatedCommonGroupDao, storedCommonGroupDao));
        if (change.getAction().isEmpty()) {
            log.warn("There was tried to store a common group {} where no diff could be determined", updatedCommonGroupDao.getIdentification());
            change.setChangeType(ChangeType.UNKNOWN);
            change.setAction(null);
        }
        commonGroupChangeRepository.save(change);
    }

    /**
     * Stores a deletion of an existing common group and removes references to it
     *
     * @param deletedCommonGroupDao the common group to delete
     * @param editorIdentification  the identification of the deleter
     */
    public void delete(CommonGroupDao deletedCommonGroupDao, String editorIdentification) {
        CommonGroupChangeDaoExt deletion = new CommonGroupChangeDaoExt(null, editorIdentification);
        deletion.setChangeType(ChangeType.DELETE);
        commonGroupChangeRepository.markedAsDeleted(deletedCommonGroupDao, deletedCommonGroupDao.getIdentification());
        commonGroupChangeRepository.save(deletion);
    }
}
