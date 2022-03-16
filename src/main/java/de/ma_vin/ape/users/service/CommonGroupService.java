package de.ma_vin.ape.users.service;


import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.mapper.GroupAccessMapper;
import de.ma_vin.ape.users.persistence.CommonGroupRepository;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.users.service.history.CommonGroupChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Data
@Log4j2
public class CommonGroupService extends AbstractRepositoryService {
    public static final String GROUP_LOG_PARAM = "common group";
    public static final String GROUPS_LOG_PARAM = "common groups";

    @Autowired
    private CommonGroupRepository commonGroupRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PrivilegeGroupService privilegeGroupService;
    @Autowired
    private BaseGroupService baseGroupService;
    @Autowired
    private CommonGroupChangeService commonGroupChangeService;

    /**
     * Deletes an common group from repository
     *
     * @param commonGroup           common group to delete
     * @param deleterIdentification The identification of the user who is deleting
     */
    public void delete(CommonGroup commonGroup, String deleterIdentification) {
        delete(GroupAccessMapper.convertToCommonGroupDao(commonGroup, false), deleterIdentification);
    }

    /**
     * Deletes an commonGroupDao from repository
     *
     * @param deleterIdentification The identification of the user who is deleting
     * @param commonGroupDao        common group to delete
     */
    private void delete(CommonGroupDao commonGroupDao, String deleterIdentification) {
        log.debug(DELETE_BEGIN_LOG_MESSAGE, GROUP_LOG_PARAM, commonGroupDao.getIdentification(), commonGroupDao.getId());

        List<PrivilegeGroup> privilegeGroups = privilegeGroupService.findAllPrivilegeGroups(commonGroupDao.getIdentification());
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, privilegeGroups.size(), PrivilegeGroupService.GROUP_LOG_PARAM, GROUP_LOG_PARAM
                , commonGroupDao.getIdentification(), commonGroupDao.getId());
        privilegeGroups.forEach(privilegeGroupService::delete);

        List<BaseGroup> baseGroups = baseGroupService.findAllBaseGroups(commonGroupDao.getIdentification());
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, baseGroups.size(), BaseGroupService.GROUP_LOG_PARAM, GROUP_LOG_PARAM
                , commonGroupDao.getIdentification(), commonGroupDao.getId());
        baseGroups.forEach(baseGroupService::delete);

        List<User> users = userService.findAllUsersAtCommonGroup(commonGroupDao.getIdentification());
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, users.size(), UserService.USERS_LOG_PARAM, GROUP_LOG_PARAM
                , commonGroupDao.getIdentification(), commonGroupDao.getId());
        users.forEach(userService::delete);

        commonGroupChangeService.delete(commonGroupDao, deleterIdentification);
        commonGroupRepository.delete(commonGroupDao);

        log.debug(DELETE_END_LOG_MESSAGE, GROUP_LOG_PARAM, commonGroupDao.getIdentification(), commonGroupDao.getId());
    }

    /**
     * Checks whether an common group exists for a given identification or not
     *
     * @param identification the identification to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    public boolean commonGroupExits(String identification) {
        return commonGroupDaoExits(IdGenerator.generateId(identification, CommonGroup.ID_PREFIX));
    }

    /**
     * Checks whether an common group exists for a given id or not
     *
     * @param id id to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    private boolean commonGroupDaoExits(Long id) {
        return commonGroupRepository.existsById(id);
    }

    /**
     * Searches for an common group
     *
     * @param identification Id of the common group which is searched for
     * @return search result
     */
    public Optional<CommonGroup> findCommonGroup(String identification) {
        return find(identification, CommonGroup.ID_PREFIX, CommonGroup.class.getSimpleName(), g -> GroupAccessMapper.convertToCommonGroup(g, false), commonGroupRepository);
    }

    /**
     * Counts all common groups
     *
     * @return number of common groups
     */
    public Long countCommonGroups() {
        log.debug("Search for all common groups");

        long result = commonGroupRepository.count();

        log.debug("{} common groups found", result);
        return Long.valueOf(result);
    }

    /**
     * Searches for all common groups
     *
     * @return List of common groups
     */
    public List<CommonGroup> findAllCommonGroups() {
        log.debug("Search for all common groups");

        List<CommonGroup> result = commonGroupRepository.findAll().stream().map(dao -> GroupAccessMapper.convertToCommonGroup(dao, false)).toList();

        log.debug("{} common groups found", result.size());
        return result;
    }

    /**
     * Searches for all common groups
     *
     * @param page zero-based page index, must not be negative.
     * @param size the size of the page to be returned, must be greater than 0.
     * @return List of common groups
     */
    public List<CommonGroup> findAllCommonGroups(Integer page, Integer size) {
        if (page == null || size == null) {
            return findAllCommonGroups();
        }
        log.debug("Search for all common groups at page {} and size {}", page, size);

        List<CommonGroup> result = commonGroupRepository.findAll(PageRequest.of(page, size))
                .stream().map(dao -> GroupAccessMapper.convertToCommonGroup(dao, false))
                .toList();

        log.debug("{} common groups found  at page {} and size {}, page, size", result.size(), page, size);
        return result;
    }

    /**
     * Determines the parent common group of a given user
     *
     * @param userIdentification Id of user whose parent common group is searched for
     * @return Optional of the found common group. Empty if it could not be determined.
     */
    public Optional<CommonGroup> findParentCommonGroupOfUser(String userIdentification) {
        Optional<Long> parentId = userRepository.getIdOfParentCommonGroup(IdGenerator.generateId(userIdentification, User.ID_PREFIX));
        if (parentId.isEmpty()) {
            return Optional.empty();
        }
        return findCommonGroup(IdGenerator.generateIdentification(parentId.get(), CommonGroup.ID_PREFIX));
    }

    /**
     * Stores an common group
     *
     * @param commonGroup          common group which should be stored
     * @param editorIdentification The identification of the user who is saving
     * @return Stored common group with additional generated ids, if missing before.
     * <br>
     * In case of not existing commonGroup for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<CommonGroup> save(CommonGroup commonGroup, String editorIdentification) {
        CommonGroupDao commonGroupDao = GroupAccessMapper.convertToCommonGroupDao(commonGroup, false);

        if (commonGroupDao.getIdentification() == null) {
            log.debug("There is not any identification, so the common group with name {} will be stored the first time", commonGroupDao.getGroupName());
            commonGroupDao = commonGroupRepository.save(commonGroupDao);
            CommonGroup result = GroupAccessMapper.convertToCommonGroup(commonGroupDao, false);
            log.debug("The common group with name {} was stored with id {} and corresponding identification {}"
                    , commonGroupDao.getGroupName(), commonGroupDao.getId(), result.getIdentification());

            commonGroupChangeService.saveCreation(commonGroupDao, editorIdentification);

            return Optional.of(result);
        }

        Optional<CommonGroupDao> storedCommonGroupDao = commonGroupRepository.findById(commonGroupDao.getId());
        if (storedCommonGroupDao.isEmpty()) {
            log.debug("The common group with identification {} and id {} does not exists and could not be saved", commonGroup.getIdentification(), commonGroupDao.getId());
            return Optional.empty();
        }

        commonGroupDao = commonGroupRepository.save(commonGroupDao);
        CommonGroup result = GroupAccessMapper.convertToCommonGroup(commonGroupDao, false);
        log.debug("The common group with identification {} was saved", result.getIdentification());

        commonGroupChangeService.saveChange(commonGroupDao, storedCommonGroupDao.get(), editorIdentification);

        return Optional.of(result);
    }
}