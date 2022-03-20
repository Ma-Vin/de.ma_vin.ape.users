package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.dao.group.CommonGroupDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.mapper.GroupAccessMapper;
import de.ma_vin.ape.users.persistence.PrivilegeGroupRepository;
import de.ma_vin.ape.users.persistence.PrivilegeGroupToUserRepository;
import de.ma_vin.ape.users.persistence.PrivilegeToBaseGroupRepository;
import de.ma_vin.ape.users.service.context.RepositoryServiceContext;
import de.ma_vin.ape.users.service.context.SavingWithParentRepositoryServiceContext;
import de.ma_vin.ape.users.service.history.AbstractChangeService;
import de.ma_vin.ape.users.service.history.PrivilegeGroupChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Data
@Log4j2
public class PrivilegeGroupService extends AbstractRepositoryService<PrivilegeGroupDao> {
    public static final String GROUP_LOG_PARAM = "privilege group";
    public static final String GROUPS_LOG_PARAM = "privilege groups";
    public static final String COMMON_GROUP_LOG_PARAM = "common group";

    @Autowired
    private PrivilegeGroupRepository privilegeGroupRepository;
    @Autowired
    private PrivilegeGroupToUserRepository privilegeGroupToUserRepository;
    @Autowired
    private PrivilegeToBaseGroupRepository privilegeToBaseGroupRepository;
    @Autowired
    private BaseGroupService baseGroupService;
    @Autowired
    private PrivilegeGroupChangeService privilegeGroupChangeService;

    @Override
    protected AbstractChangeService<PrivilegeGroupDao> getChangeService() {
        return privilegeGroupChangeService;
    }

    @Override
    protected RepositoryServiceContext<PrivilegeGroupDao> createContext(String identification) {
        return new RepositoryServiceContext<>(identification, PrivilegeGroup.class.getSimpleName(), PrivilegeGroup.ID_PREFIX, privilegeGroupRepository, PrivilegeGroupDao::new);
    }


    /**
     * Deletes an privilege group from repository
     *
     * @param privilegeGroup        privilege group to delete
     * @param deleterIdentification The identification of the user who is deleting
     */
    public void delete(PrivilegeGroup privilegeGroup, String deleterIdentification) {
        delete(GroupAccessMapper.convertToPrivilegeGroupDao(privilegeGroup, false), deleterIdentification);
    }

    /**
     * Deletes an privilegeGroupDao from repository
     *
     * @param privilegeGroupDao     privilege group to delete
     * @param deleterIdentification The identification of the user who is deleting
     */
    private void delete(PrivilegeGroupDao privilegeGroupDao, String deleterIdentification) {
        log.debug(DELETE_BEGIN_LOG_MESSAGE, GROUP_LOG_PARAM, privilegeGroupDao.getIdentification(), privilegeGroupDao.getId());

        long numToUserDeleted = privilegeGroupToUserRepository.deleteByPrivilegeGroup(privilegeGroupDao);
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, numToUserDeleted, "connections to user", GROUP_LOG_PARAM
                , privilegeGroupDao.getIdentification(), privilegeGroupDao.getId());

        long numToBaseGroupDeleted = privilegeToBaseGroupRepository.deleteByPrivilegeGroup(privilegeGroupDao);
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, numToBaseGroupDeleted, "connections to base group", GROUP_LOG_PARAM
                , privilegeGroupDao.getIdentification(), privilegeGroupDao.getId());

        privilegeGroupChangeService.delete(privilegeGroupDao, deleterIdentification);
        privilegeGroupRepository.delete(privilegeGroupDao);

        log.debug(DELETE_END_LOG_MESSAGE, GROUP_LOG_PARAM, privilegeGroupDao.getIdentification(), privilegeGroupDao.getId());
    }

    /**
     * Checks whether an privilege group exists for a given identification or not
     *
     * @param identification the identification to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    public boolean privilegeGroupExits(String identification) {
        return privilegeGroupDaoExits(IdGenerator.generateId(identification, PrivilegeGroup.ID_PREFIX));
    }

    /**
     * Checks whether an privilege group exists for a given id or not
     *
     * @param id id to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    private boolean privilegeGroupDaoExits(Long id) {
        return privilegeGroupRepository.existsById(id);
    }

    /**
     * Searches for an privilege group
     *
     * @param identification Id of the privilege group which is searched for
     * @return search result
     */
    public Optional<PrivilegeGroup> findPrivilegeGroup(String identification) {
        return find(identification, g -> GroupAccessMapper.convertToPrivilegeGroup(g, false));
    }

    /**
     * Searches for an privilege group and its sub entities
     *
     * @param identification Id of the base group which is searched for
     * @return search result
     */
    public Optional<PrivilegeGroup> findPrivilegeGroupTree(String identification) {
        Optional<PrivilegeGroupDao> root = find(identification);
        if (root.isEmpty()) {
            return Optional.empty();
        }
        loadSubTree(root.get());
        return Optional.of(GroupAccessMapper.convertToPrivilegeGroup(root.get(), true));
    }

    /**
     * Loads all sub entities of the given privilege group and adds them
     *
     * @param parent privilege group whose sub entities should be loaded
     */
    private void loadSubTree(PrivilegeGroupDao parent) {
        privilegeToBaseGroupRepository.findAllByPrivilegeGroup(parent).forEach(btb -> {
            parent.getAggBaseGroup().add(btb);
            btb.setPrivilegeGroup(parent);
            baseGroupService.loadSubTree(btb.getBaseGroup());
        });
        privilegeGroupToUserRepository.findAllByPrivilegeGroup(parent).forEach(btu -> {
            parent.getAggUser().add(btu);
            btu.setPrivilegeGroup(parent);
        });
    }

    /**
     * Counts all privilege groups at a parent common group
     *
     * @param parentIdentification identification of the parent
     * @return number of privilege groups
     */
    public Long countPrivilegeGroups(String parentIdentification) {
        log.debug(COUNT_START_LOG_MESSAGE, GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        CommonGroupDao parent = new CommonGroupDao();
        parent.setIdentification(parentIdentification);

        long result = countPrivilegeGroups(parent);

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Counts all privilege groups
     *
     * @param parent parent common group
     * @return List of privilege groups
     */
    private long countPrivilegeGroups(CommonGroupDao parent) {
        return privilegeGroupRepository.countByParentCommonGroup(parent);
    }

    /**
     * Searches for all privilege groups at a parent common group
     *
     * @param parentIdentification identification of the parent
     * @return List of privilege groups
     */
    public List<PrivilegeGroup> findAllPrivilegeGroups(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);

        List<PrivilegeGroup> result = findAllPrivilegeGroups(new CommonGroupDaoExt(parentIdentification), null, null)
                .stream()
                .map(pg -> GroupAccessMapper.convertToPrivilegeGroup(pg, false))
                .toList();

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all privilege groups at a parent common group
     *
     * @param parentIdentification identification of the parent
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return List of privilege groups
     */
    public List<PrivilegeGroup> findAllPrivilegeGroups(String parentIdentification, Integer page, Integer size) {
        log.debug(SEARCH_START_PAGE_LOG_MESSAGE, GROUPS_LOG_PARAM, page, size, COMMON_GROUP_LOG_PARAM, parentIdentification);

        List<PrivilegeGroup> result = findAllPrivilegeGroups(new CommonGroupDaoExt(parentIdentification), page, size)
                .stream()
                .map(pg -> GroupAccessMapper.convertToPrivilegeGroup(pg, false))
                .toList();

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification, page, size);
        return result;
    }

    /**
     * Searches for all privilege groups
     *
     * @param parent parent common group
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of privilege groups
     */
    private List<PrivilegeGroupDao> findAllPrivilegeGroups(CommonGroupDao parent, Integer page, Integer size) {
        if (page == null || size == null) {
            return privilegeGroupRepository.findByParentCommonGroup(parent);
        }
        return privilegeGroupRepository.findByParentCommonGroup(parent, PageRequest.of(page, size));
    }

    /**
     * Stores a privilege group
     *
     * @param privilegeGroup       privilege group which should be stored
     * @param editorIdentification The identification of the user who is saving
     * @return Stored privilege group with additional generated ids, if missing before.
     * <br>
     * In case of not existing privilegeGroup for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<PrivilegeGroup> save(PrivilegeGroup privilegeGroup, String editorIdentification) {
        if (privilegeGroup.getIdentification() == null) {
            log.error(GET_PARENT_ID_MISSING_CHILD_ID_LOG_ERROR, COMMON_GROUP_LOG_PARAM, GROUP_LOG_PARAM);
            return Optional.empty();
        }
        Optional<Long> commonGroupId = privilegeGroupRepository.getIdOfParentCommonGroup(IdGenerator.generateId(privilegeGroup.getIdentification(), PrivilegeGroup.ID_PREFIX));
        if (commonGroupId.isPresent()) {
            return save(privilegeGroup, IdGenerator.generateIdentification(commonGroupId.get(), CommonGroup.ID_PREFIX), editorIdentification);
        }
        log.error(GET_PARENT_ID_NOT_FOUND_LOG_ERROR, COMMON_GROUP_LOG_PARAM);
        return Optional.empty();
    }

    /**
     * Stores a privilege group at a common group
     *
     * @param privilegeGroup       privilege group which should be stored
     * @param groupIdentification  identification of the parent common group
     * @param editorIdentification The identification of the user who is saving
     * @return Stored privilege group with additional generated ids, if missing before.
     * <br>
     * In case of not existing privilegeGroup for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<PrivilegeGroup> save(PrivilegeGroup privilegeGroup, String groupIdentification, String editorIdentification) {
        SavingWithParentRepositoryServiceContext<PrivilegeGroup, PrivilegeGroupDao, CommonGroupDao> context = new SavingWithParentRepositoryServiceContext<PrivilegeGroup, PrivilegeGroupDao, CommonGroupDao>(
                privilegeGroup
                , editorIdentification
                , privilegeGroupDomainObject -> GroupAccessMapper.convertToPrivilegeGroupDao(privilegeGroupDomainObject, false)
                , privilegeGroupDao -> GroupAccessMapper.convertToPrivilegeGroup(privilegeGroupDao, false)
                , privilegeGroupRepository)
                .config(PrivilegeGroup::getGroupName)
                .config(groupIdentification
                        , () -> {
                            CommonGroupDao res = new CommonGroupDao();
                            res.setPrivileges(new ArrayList<>());
                            return res;
                        }
                        , (privilegeGroupDomainObject, parentDao) -> GroupAccessMapper.convertToPrivilegeGroupDao(privilegeGroupDomainObject, false, parentDao));

        return save(context);
    }
}
