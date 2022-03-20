package de.ma_vin.ape.users.service;


import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.dao.group.BaseGroupDaoExt;
import de.ma_vin.ape.users.model.dao.group.CommonGroupDaoExt;
import de.ma_vin.ape.users.model.dao.group.PrivilegeGroupDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.*;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.mapper.GroupAccessMapper;
import de.ma_vin.ape.users.persistence.*;
import de.ma_vin.ape.users.service.context.RepositoryServiceContext;
import de.ma_vin.ape.users.service.context.SavingWithParentRepositoryServiceContext;
import de.ma_vin.ape.users.service.history.AbstractChildChangeService;
import de.ma_vin.ape.users.service.history.BaseGroupChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@Data
@Log4j2
public class BaseGroupService extends AbstractChildRepositoryService<BaseGroupDao, PrivilegeGroupDao, BaseGroupDao> {
    public static final String GROUP_LOG_PARAM = "base group";
    public static final String GROUPS_LOG_PARAM = "base groups";
    public static final String AVAILABLE_GROUPS_LOG_PARAM = " available users";
    public static final String COMMON_GROUP_LOG_PARAM = "common group";
    public static final String PRIVILEGE_GROUP_LOG_PARAM = "privilege group";

    @Autowired
    private BaseGroupRepository baseGroupRepository;
    @Autowired
    private BaseToBaseGroupRepository baseToBaseGroupRepository;
    @Autowired
    private BaseGroupToUserRepository baseGroupToUserRepository;
    @Autowired
    private PrivilegeToBaseGroupRepository privilegeToBaseGroupRepository;
    @Autowired
    private PrivilegeGroupRepository privilegeGroupRepository;
    @Autowired
    private BaseGroupChangeService baseGroupChangeService;

    @Override
    protected AbstractChildChangeService<BaseGroupDao, PrivilegeGroupDao, BaseGroupDao> getChangeService() {
        return baseGroupChangeService;
    }

    @Override
    protected RepositoryServiceContext<BaseGroupDao> createContext(String identification) {
        return new RepositoryServiceContext<>(identification, BaseGroup.class.getSimpleName(), BaseGroup.ID_PREFIX, baseGroupRepository, BaseGroupDao::new);
    }

    @Override
    protected RepositoryServiceContext<PrivilegeGroupDao> createParentFirstTypeContext(String identification) {
        return new RepositoryServiceContext<>(identification, PrivilegeGroup.class.getSimpleName(), PrivilegeGroup.ID_PREFIX, privilegeGroupRepository, PrivilegeGroupDao::new);
    }

    @Override
    protected RepositoryServiceContext<BaseGroupDao> createParentSecondTypeContext(String identification) {
        return createContext(identification);
    }

    /**
     * Deletes an base group from repository
     *
     * @param baseGroup             base group to delete
     * @param deleterIdentification The identification of the user who is deleting
     */
    public void delete(BaseGroup baseGroup, String deleterIdentification) {
        delete(GroupAccessMapper.convertToBaseGroupDao(baseGroup, false), deleterIdentification);
    }

    /**
     * Deletes an baseGroupDao from repository
     *
     * @param baseGroupDao          base group to delete
     * @param deleterIdentification The identification of the user who is deleting
     */
    private void delete(BaseGroupDao baseGroupDao, String deleterIdentification) {
        log.debug(DELETE_BEGIN_LOG_MESSAGE, GROUP_LOG_PARAM, baseGroupDao.getIdentification(), baseGroupDao.getId());

        long numToUserDeleted = baseGroupToUserRepository.deleteByBaseGroup(baseGroupDao);
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, numToUserDeleted, "connections to user", GROUP_LOG_PARAM
                , baseGroupDao.getIdentification(), baseGroupDao.getId());

        long numToBaseGroup = baseToBaseGroupRepository.deleteByBaseGroup(baseGroupDao);
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, numToBaseGroup, "connections to other base group", GROUP_LOG_PARAM
                , baseGroupDao.getIdentification(), baseGroupDao.getId());

        long numFromBaseGroup = baseToBaseGroupRepository.deleteBySubBaseGroup(baseGroupDao);
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, numFromBaseGroup, "connections from other base group", GROUP_LOG_PARAM
                , baseGroupDao.getIdentification(), baseGroupDao.getId());

        long numFromPrivilegeGroup = privilegeToBaseGroupRepository.deleteByBaseGroup(baseGroupDao);
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, numFromPrivilegeGroup, "connections from privilege group", GROUP_LOG_PARAM
                , baseGroupDao.getIdentification(), baseGroupDao.getId());

        baseGroupChangeService.delete(baseGroupDao, deleterIdentification);
        baseGroupRepository.delete(baseGroupDao);

        log.debug(DELETE_END_LOG_MESSAGE, GROUP_LOG_PARAM, baseGroupDao.getIdentification(), baseGroupDao.getId());
    }

    /**
     * Checks whether an base group exists for a given identification or not
     *
     * @param identification the identification to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    public boolean baseGroupExits(String identification) {
        return baseGroupDaoExits(IdGenerator.generateId(identification, BaseGroup.ID_PREFIX));
    }

    /**
     * Checks whether an base group exists for a given id or not
     *
     * @param id id to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    private boolean baseGroupDaoExits(Long id) {
        return baseGroupRepository.existsById(id);
    }

    /**
     * Searches for an base group
     *
     * @param identification Id of the base group which is searched for
     * @return search result
     */
    public Optional<BaseGroup> findBaseGroup(String identification) {
        return find(identification, g -> GroupAccessMapper.convertToBaseGroup(g, false));
    }

    /**
     * Searches for an base group and its sub entities
     *
     * @param identification Id of the base group which is searched for
     * @return search result
     */
    public Optional<BaseGroup> findBaseGroupTree(String identification) {
        Optional<BaseGroupDao> root = find(identification);
        if (root.isEmpty()) {
            return Optional.empty();
        }
        loadSubTree(root.get());
        return Optional.of(GroupAccessMapper.convertToBaseGroup(root.get(), true));
    }

    /**
     * Loads all sub entities of the given base group and adds them
     *
     * @param parent base group whose sub entities should be loaded
     */
    void loadSubTree(BaseGroupDao parent) {
        baseToBaseGroupRepository.findAllByBaseGroup(parent).forEach(btb -> {
            parent.getSubBaseGroups().add(btb);
            btb.setBaseGroup(parent);
            loadSubTree(btb.getSubBaseGroup());
        });
        baseGroupToUserRepository.findAllByBaseGroup(parent).forEach(btu -> {
            parent.getUsers().add(btu);
            btu.setBaseGroup(parent);
        });
    }

    /**
     * Counts all base groups at a parent common group
     *
     * @param parentIdentification identification of the parent
     * @return number of base groups
     */
    public Long countBaseGroups(String parentIdentification) {
        log.debug(COUNT_START_LOG_MESSAGE, GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);

        long result = countBaseGroups(new CommonGroupDaoExt(parentIdentification));

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Searches for all base groups at a parent common group
     *
     * @param parentIdentification identification of the parent
     * @return List of base groups
     */
    public List<BaseGroup> findAllBaseGroups(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);

        List<BaseGroup> result = findAllBaseGroups(new CommonGroupDaoExt(parentIdentification), null, null)
                .stream()
                .map(bg -> GroupAccessMapper.convertToBaseGroup(bg, false))
                .toList();

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all base groups at a parent common group
     *
     * @param parentIdentification identification of the parent
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return List of base groups
     */
    public List<BaseGroup> findAllBaseGroups(String parentIdentification, Integer page, Integer size) {
        log.debug(SEARCH_START_PAGE_LOG_MESSAGE, GROUPS_LOG_PARAM, page, size, COMMON_GROUP_LOG_PARAM, parentIdentification);

        List<BaseGroup> result = findAllBaseGroups(new CommonGroupDaoExt(parentIdentification), page, size)
                .stream()
                .map(bg -> GroupAccessMapper.convertToBaseGroup(bg, false))
                .toList();

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification, page, size);
        return result;
    }

    /**
     * Counts all base groups at an other parent base group
     *
     * @param parentIdentification identification of the parent
     * @return number of base groups
     */
    public Long countBasesAtBaseGroup(String parentIdentification) {
        log.debug(COUNT_START_LOG_MESSAGE, GROUPS_LOG_PARAM, GROUP_LOG_PARAM, parentIdentification);

        long result = countBasesAtBaseGroup(new BaseGroupDaoExt(parentIdentification));

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, GROUPS_LOG_PARAM, GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Counts all available base groups for a base group
     *
     * @param baseGroupIdentification identification of the base group
     * @return number of base groups
     */
    public Long countAvailableBasesForBaseGroup(String baseGroupIdentification) {
        log.debug(COUNT_FOR_START_LOG_MESSAGE, AVAILABLE_GROUPS_LOG_PARAM, GROUP_LOG_PARAM, baseGroupIdentification);

        BaseGroupDao baseGroupDao = baseGroupRepository.getById(IdGenerator.generateId(baseGroupIdentification, BaseGroup.ID_PREFIX));

        long result = baseToBaseGroupRepository.countAvailableBaseGroups(baseGroupDao, baseGroupDao.getParentCommonGroup());

        log.debug(COUNT_FOR_RESULT_LOG_MESSAGE, result, AVAILABLE_GROUPS_LOG_PARAM, GROUP_LOG_PARAM, baseGroupIdentification);
        return Long.valueOf(result);
    }

    /**
     * Searches for all base groups at an other parent base group
     *
     * @param parentIdentification identification of the parent
     * @return List of base groups
     */
    public List<BaseGroup> findAllBasesAtBaseGroup(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, GROUPS_LOG_PARAM, GROUP_LOG_PARAM, parentIdentification);

        List<BaseGroup> result = findAllBasesAtBaseGroup(new BaseGroupDaoExt(parentIdentification), null, null)
                .stream()
                .map(bg -> GroupAccessMapper.convertToBaseGroup(bg, false))
                .toList();

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all base groups at an other parent base group
     *
     * @param parentIdentification identification of the parent
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return List of base groups
     */
    public List<BaseGroup> findAllBasesAtBaseGroup(String parentIdentification, Integer page, Integer size) {
        log.debug(SEARCH_START_PAGE_LOG_MESSAGE, GROUPS_LOG_PARAM, page, size, GROUP_LOG_PARAM, parentIdentification);

        List<BaseGroup> result = findAllBasesAtBaseGroup(new BaseGroupDaoExt(parentIdentification), page, size)
                .stream()
                .map(bg -> GroupAccessMapper.convertToBaseGroup(bg, false))
                .toList();

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, GROUP_LOG_PARAM, parentIdentification, page, size);
        return result;
    }

    /**
     * Counts all base groups at a parent privilege group
     *
     * @param parentIdentification identification of the parent
     * @param role                 role of base groups at privilege group
     * @return number of base groups. If the role is given, the result is filtered by this role.
     */
    public Long countBasesAtPrivilegeGroup(String parentIdentification, Role role) {
        log.debug(COUNT_START_LOG_MESSAGE, GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);

        long result = countBasesAtPrivilegeGroup(new PrivilegeGroupDaoExt(parentIdentification), role);

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Counts all available base groups for a privilege group
     *
     * @param privilegeGroupIdentification identification of the privilege group
     * @return number of base groups
     */
    public Long countAvailableBasesForPrivilegeGroup(String privilegeGroupIdentification) {
        log.debug(COUNT_FOR_START_LOG_MESSAGE, AVAILABLE_GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, privilegeGroupIdentification);

        PrivilegeGroupDao privilegeGroupDao = privilegeGroupRepository.getById(IdGenerator.generateId(privilegeGroupIdentification, PrivilegeGroup.ID_PREFIX));

        long result = privilegeToBaseGroupRepository.countAvailableBaseGroups(privilegeGroupDao, privilegeGroupDao.getParentCommonGroup());

        log.debug(COUNT_FOR_RESULT_LOG_MESSAGE, result, AVAILABLE_GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, privilegeGroupIdentification);
        return Long.valueOf(result);
    }

    /**
     * Searches for all base groups at a parent privilege group with a given role
     *
     * @param parentIdentification identification of the parent
     * @param role                 role of base groups at privilege group
     * @return List of base groups. If the role is given, the result is filtered by this role.
     */
    public List<BaseGroup> findAllBaseAtPrivilegeGroup(String parentIdentification, Role role) {
        log.debug(SEARCH_START_LOG_MESSAGE, GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);

        List<BaseGroup> result = findAllBaseAtPrivilegeGroup(new PrivilegeGroupDaoExt(parentIdentification), role, null, null)
                .stream()
                .map(bg -> GroupAccessMapper.convertToBaseGroup(bg, false))
                .toList();

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all base groups at a parent privilege group with a given role
     *
     * @param parentIdentification identification of the
     * @param role                 role of base groups at privilege group
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return List of base groups. If the role is given, the result is filtered by this role.
     */
    public List<BaseGroup> findAllBaseAtPrivilegeGroup(String parentIdentification, Role role, Integer page, Integer size) {
        log.debug(SEARCH_START_PAGE_LOG_MESSAGE, GROUPS_LOG_PARAM, page, size, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);

        List<BaseGroup> result = findAllBaseAtPrivilegeGroup(new PrivilegeGroupDaoExt(parentIdentification), role, page, size)
                .stream()
                .map(bg -> GroupAccessMapper.convertToBaseGroup(bg, false))
                .toList();

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification, page, size);
        return result;
    }

    /**
     * Searches for all available base groups who are not added to the privilege group yet
     *
     * @param privilegeGroupIdentification identification of the privilege group
     * @return List of base groups
     */
    public List<BaseGroup> findAllAvailableBasesForPrivilegeGroup(String privilegeGroupIdentification) {
        log.debug(SEARCH_FOR_START_LOG_MESSAGE, AVAILABLE_GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, privilegeGroupIdentification);

        PrivilegeGroupDao privilegeGroupDao = privilegeGroupRepository.getById(IdGenerator.generateId(privilegeGroupIdentification, PrivilegeGroup.ID_PREFIX));

        List<BaseGroup> result = findAllAvailableBaseGroups(privilegeGroupDao, null, null)
                .stream()
                .map(b -> GroupAccessMapper.convertToBaseGroup(b, false))
                .sorted(Comparator.comparing(BaseGroup::getIdentification)).toList();

        log.debug(SEARCH_FOR_RESULT_LOG_MESSAGE, result.size(), AVAILABLE_GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, privilegeGroupIdentification);
        return result;
    }

    /**
     * Searches for all available base groups who are not added to the privilege group yet
     *
     * @param privilegeGroupIdentification identification of the privilege group
     * @param page                         zero-based page index, must not be negative.
     * @param size                         the size of the page to be returned, must be greater than 0.
     * @return List of base groups
     */
    public List<BaseGroup> findAllAvailableBasesForPrivilegeGroup(String privilegeGroupIdentification, Integer page, Integer size) {
        log.debug(SEARCH_FOR_START_PAGE_LOG_MESSAGE, AVAILABLE_GROUPS_LOG_PARAM, page, size, PRIVILEGE_GROUP_LOG_PARAM, privilegeGroupIdentification);

        PrivilegeGroupDao privilegeGroupDao = privilegeGroupRepository.getById(IdGenerator.generateId(privilegeGroupIdentification, PrivilegeGroup.ID_PREFIX));

        List<BaseGroup> result = findAllAvailableBaseGroups(privilegeGroupDao, page, size)
                .stream()
                .map(b -> GroupAccessMapper.convertToBaseGroup(b, false))
                .sorted(Comparator.comparing(BaseGroup::getIdentification)).toList();

        log.debug(SEARCH_FOR_RESULT_PAGE_LOG_MESSAGE, result.size(), AVAILABLE_GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, privilegeGroupIdentification, page, size);
        return result;
    }


    /**
     * Count all base groups
     *
     * @param parent parent common group
     * @return number of base groups
     */
    private long countBaseGroups(CommonGroupDao parent) {
        return baseGroupRepository.countByParentCommonGroup(parent);
    }

    /**
     * Searches for all base groups
     *
     * @param parent parent common group
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of base groups. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private List<BaseGroupDao> findAllBaseGroups(CommonGroupDao parent, Integer page, Integer size) {
        if (page == null || size == null) {
            return baseGroupRepository.findByParentCommonGroup(parent);
        }
        return baseGroupRepository.findByParentCommonGroup(parent, PageRequest.of(page, size));
    }

    /**
     * Searches for all available base groups which are not added to the privilege group yet
     *
     * @param privilegeGroupDao privilege group
     * @param page              zero-based page index, must not be negative.
     * @param size              the size of the page to be returned, must be greater than 0.
     * @return List of users. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private List<BaseGroupDao> findAllAvailableBaseGroups(PrivilegeGroupDao privilegeGroupDao, Integer page, Integer size) {
        if (page == null || size == null) {
            return privilegeToBaseGroupRepository.findAvailableBaseGroups(privilegeGroupDao, privilegeGroupDao.getParentCommonGroup());
        }
        return privilegeToBaseGroupRepository.findAvailableBaseGroups(privilegeGroupDao, privilegeGroupDao.getParentCommonGroup(), PageRequest.of(page, size));
    }

    /**
     * Count all base groups
     *
     * @param parent parent base group
     * @return number of base groups
     */
    private long countBasesAtBaseGroup(BaseGroupDao parent) {
        return baseToBaseGroupRepository.countByBaseGroup(parent);
    }

    /**
     * Searches for all base groups
     *
     * @param parent parent base
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of base groups
     */
    private List<BaseGroupDao> findAllBasesAtBaseGroup(BaseGroupDao parent, Integer page, Integer size) {
        if (page == null || size == null) {
            return baseToBaseGroupRepository.findAllByBaseGroup(parent).stream()
                    .map(BaseGroupToBaseGroupDao::getSubBaseGroup)
                    .toList();
        }
        return baseToBaseGroupRepository.findAllByBaseGroup(parent, PageRequest.of(page, size)).stream()
                .map(BaseGroupToBaseGroupDao::getSubBaseGroup)
                .toList();
    }

    /**
     * Searches for all available base groups who are not added to the other base group yet
     *
     * @param baseGroupIdentification identification of the base group
     * @return List of base groups
     */
    public List<BaseGroup> findAllAvailableBasesForBaseGroup(String baseGroupIdentification) {
        log.debug(SEARCH_FOR_START_LOG_MESSAGE, AVAILABLE_GROUPS_LOG_PARAM, GROUP_LOG_PARAM, baseGroupIdentification);

        BaseGroupDao baseGroupDao = baseGroupRepository.getById(IdGenerator.generateId(baseGroupIdentification, BaseGroup.ID_PREFIX));

        List<BaseGroup> result = findAllAvailableBaseGroups(baseGroupDao, null, null)
                .stream()
                .map(b -> GroupAccessMapper.convertToBaseGroup(b, false))
                .sorted(Comparator.comparing(BaseGroup::getIdentification)).toList();

        log.debug(SEARCH_FOR_RESULT_LOG_MESSAGE, result.size(), AVAILABLE_GROUPS_LOG_PARAM, GROUP_LOG_PARAM, baseGroupIdentification);
        return result;
    }

    /**
     * Searches for all available base groups who are not added to the other base group yet
     *
     * @param baseGroupIdentification identification of the base group
     * @param page                    zero-based page index, must not be negative.
     * @param size                    the size of the page to be returned, must be greater than 0.
     * @return List of base groups
     */
    public List<BaseGroup> findAllAvailableBasesForBaseGroup(String baseGroupIdentification, Integer page, Integer size) {
        log.debug(SEARCH_FOR_START_PAGE_LOG_MESSAGE, AVAILABLE_GROUPS_LOG_PARAM, page, size, GROUP_LOG_PARAM, baseGroupIdentification);

        BaseGroupDao baseGroupDao = baseGroupRepository.getById(IdGenerator.generateId(baseGroupIdentification, BaseGroup.ID_PREFIX));

        List<BaseGroup> result = findAllAvailableBaseGroups(baseGroupDao, page, size)
                .stream()
                .map(b -> GroupAccessMapper.convertToBaseGroup(b, false))
                .sorted(Comparator.comparing(BaseGroup::getIdentification)).toList();

        log.debug(SEARCH_FOR_RESULT_PAGE_LOG_MESSAGE, result.size(), AVAILABLE_GROUPS_LOG_PARAM, GROUP_LOG_PARAM, baseGroupIdentification, page, size);
        return result;
    }

    /**
     * Searches for all available base groups which are not added to the other base group yet
     *
     * @param baseGroupDao base group
     * @param page         zero-based page index, must not be negative.
     * @param size         the size of the page to be returned, must be greater than 0.
     * @return List of users. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private List<BaseGroupDao> findAllAvailableBaseGroups(BaseGroupDao baseGroupDao, Integer page, Integer size) {
        if (page == null || size == null) {
            return baseToBaseGroupRepository.findAvailableBaseGroups(baseGroupDao, baseGroupDao.getParentCommonGroup());
        }
        return baseToBaseGroupRepository.findAvailableBaseGroups(baseGroupDao, baseGroupDao.getParentCommonGroup(), PageRequest.of(page, size));
    }

    /**
     * Count all base groups
     *
     * @param parent parent privilege group
     * @param role   role of base groups at privilege group
     * @return number of base groups. If the role is given, the result is filtered by this role.
     */
    private long countBasesAtPrivilegeGroup(PrivilegeGroupDao parent, Role role) {
        return role == null ? privilegeToBaseGroupRepository.countByPrivilegeGroup(parent)
                : privilegeToBaseGroupRepository.countByPrivilegeGroupAndFilterRole(parent, role);
    }

    /**
     * Searches for all base groups
     *
     * @param parent parent privilege group
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of base groups
     */
    private List<BaseGroupDao> findAllBaseAtPrivilegeGroup(PrivilegeGroupDao parent, Integer page, Integer size) {
        if (page == null || size == null) {
            return privilegeToBaseGroupRepository.findAllByPrivilegeGroup(parent).stream()
                    .map(PrivilegeGroupToBaseGroupDao::getBaseGroup)
                    .toList();
        }
        return privilegeToBaseGroupRepository.findAllByPrivilegeGroup(parent, PageRequest.of(page, size)).stream()
                .map(PrivilegeGroupToBaseGroupDao::getBaseGroup)
                .toList();
    }

    /**
     * Searches for all base groups with a given role
     *
     * @param parent parent privilege group
     * @param role   role of base groups at privilege group
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of base groups
     */
    private List<BaseGroupDao> findAllBaseAtPrivilegeGroup(PrivilegeGroupDao parent, Role role, Integer page, Integer size) {
        if (role == null) {
            return findAllBaseAtPrivilegeGroup(parent, page, size);
        }
        if (page == null || size == null) {
            return privilegeToBaseGroupRepository.findAllByPrivilegeGroupAndFilterRole(parent, role).stream()
                    .map(PrivilegeGroupToBaseGroupDao::getBaseGroup)
                    .toList();
        }
        return privilegeToBaseGroupRepository.findAllByPrivilegeGroupAndFilterRole(parent, role, PageRequest.of(page, size)).stream()
                .map(PrivilegeGroupToBaseGroupDao::getBaseGroup)
                .toList();
    }

    /**
     * Stores a base group
     *
     * @param baseGroup            base group which should be stored
     * @param editorIdentification The identification of the user who is saving
     * @return Stored base group with additional generated ids, if missing before.
     * <br>
     * In case of not existing baseGroup for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<BaseGroup> save(BaseGroup baseGroup, String editorIdentification) {
        if (baseGroup.getIdentification() == null) {
            log.error(GET_PARENT_ID_MISSING_CHILD_ID_LOG_ERROR, COMMON_GROUP_LOG_PARAM, GROUP_LOG_PARAM);
            return Optional.empty();
        }
        Optional<Long> commonGroupId = baseGroupRepository.getIdOfParentCommonGroup(IdGenerator.generateId(baseGroup.getIdentification(), BaseGroup.ID_PREFIX));
        if (commonGroupId.isPresent()) {
            return save(baseGroup, IdGenerator.generateIdentification(commonGroupId.get(), CommonGroup.ID_PREFIX), editorIdentification);
        }
        log.error(GET_PARENT_ID_NOT_FOUND_LOG_ERROR, COMMON_GROUP_LOG_PARAM);
        return Optional.empty();
    }

    /**
     * Stores a base group at a common group
     *
     * @param baseGroup            base group which should be stored
     * @param groupIdentification  identification of the parent common group
     * @param editorIdentification The identification of the user who is saving
     * @return Stored base group with additional generated ids, if missing before.
     * <br>
     * In case of not existing baseGroup for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<BaseGroup> save(BaseGroup baseGroup, String groupIdentification, String editorIdentification) {
        SavingWithParentRepositoryServiceContext<BaseGroup, BaseGroupDao, CommonGroupDao> context = new SavingWithParentRepositoryServiceContext<BaseGroup, BaseGroupDao, CommonGroupDao>(
                baseGroup
                , editorIdentification
                , baseGroupDomainObject -> GroupAccessMapper.convertToBaseGroupDao(baseGroupDomainObject, false)
                , baseGroupDao -> GroupAccessMapper.convertToBaseGroup(baseGroupDao, false)
                , baseGroupRepository)
                .config(BaseGroup::getGroupName)
                .config(groupIdentification
                        , () -> {
                            CommonGroupDao res = new CommonGroupDao();
                            res.setBasics(new ArrayList<>());
                            return res;
                        }
                        , (baseGroupDomainObject, parentDao) -> GroupAccessMapper.convertToBaseGroupDao(baseGroupDomainObject, false, parentDao));

        return save(context);
    }

    /**
     * Adds a base to a privilege group
     *
     * @param privilegeGroupIdentification identification of the privilege group
     * @param baseGroupIdentification      identification of the base group to add
     * @param role                         role of the user at privilege group
     * @param editorIdentification         The identification of the user who is adding
     * @return {@code true} if the base group was added to the privilege group, otherwise {@code false}
     */
    public boolean addBaseToPrivilegeGroup(String privilegeGroupIdentification, String baseGroupIdentification, Role role, String editorIdentification) {
        return add(privilegeGroupIdentification, baseGroupIdentification, privilegeToBaseGroupRepository
                , (privilegeGroup, baseGroup) -> {
                    PrivilegeGroupToBaseGroupDao connection = new PrivilegeGroupToBaseGroupDao();
                    connection.setPrivilegeGroup(privilegeGroup);
                    connection.setBaseGroup(baseGroup);
                    connection.setFilterRole(role);
                    return connection;
                }
                , baseGroupChangeService::addToParentFirstType
                , editorIdentification);
    }

    /**
     * Removes a base from a privilege group
     *
     * @param privilegeGroupIdentification Identification of the privilege group
     * @param baseGroupIdentification      Identification of the base group to remove
     * @param editorIdentification         The identification of the user who is removing
     * @return {@code true} if the base group was removed from the privilege group. Otherwise {@code false}
     */
    public boolean removeBaseFromPrivilegeGroup(String privilegeGroupIdentification, String baseGroupIdentification, String editorIdentification) {
        return remove(privilegeGroupIdentification, baseGroupIdentification, privilegeToBaseGroupRepository::deleteByPrivilegeGroupAndBaseGroup
                , baseGroupChangeService::removeFromParentFirstType
                , editorIdentification);
    }


    /**
     * Adds a base to another base group
     *
     * @param parentGroupIdentification identification of the parent base group
     * @param baseGroupIdentification   identification of the base group to add
     * @param editorIdentification      The identification of the user who is adding
     * @return {@code true} if the base group was added to the other base group, otherwise {@code false}
     */
    public boolean addBaseToBaseGroup(String parentGroupIdentification, String baseGroupIdentification, String editorIdentification) {
        return add(createParentSecondTypeContext(parentGroupIdentification), createContext(baseGroupIdentification), baseToBaseGroupRepository
                , (baseGroup, subBaseGroup) -> {
                    BaseGroupToBaseGroupDao connection = new BaseGroupToBaseGroupDao();
                    connection.setBaseGroup(baseGroup);
                    connection.setSubBaseGroup(subBaseGroup);
                    return connection;
                }
                , baseGroupChangeService::addToParentSecondType
                , editorIdentification);
    }

    /**
     * Removes a base from another base group
     *
     * @param parentGroupIdentification Identification of the parent base group
     * @param baseGroupIdentification   Identification of the base group to remove
     * @param editorIdentification      The identification of the user who is removing
     * @return {@code true} if the base group was removed from the other base group. Otherwise {@code false}
     */
    public boolean removeBaseFromBaseGroup(String parentGroupIdentification, String baseGroupIdentification, String editorIdentification) {
        return remove(createParentSecondTypeContext(parentGroupIdentification), createContext(baseGroupIdentification)
                , baseToBaseGroupRepository::deleteByBaseGroupAndSubBaseGroup
                , baseGroupChangeService::removeFromParentSecondType
                , editorIdentification);
    }
}
