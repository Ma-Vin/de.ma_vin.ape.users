package de.ma_vin.ape.users.service;


import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.dao.group.BaseGroupDaoExt;
import de.ma_vin.ape.users.model.dao.group.CommonGroupDaoExt;
import de.ma_vin.ape.users.model.dao.group.PrivilegeGroupDaoExt;
import de.ma_vin.ape.users.model.gen.dao.group.*;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.mapper.GroupAccessMapper;
import de.ma_vin.ape.users.persistence.*;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Data
@Log4j2
public class BaseGroupService extends AbstractRepositoryService {
    public static final String GROUP_LOG_PARAM = "base group";
    public static final String GROUPS_LOG_PARAM = "base groups";
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

    /**
     * Deletes an base group from repository
     *
     * @param baseGroup base group to delete
     */
    public void delete(BaseGroup baseGroup) {
        delete(GroupAccessMapper.convertToBaseGroupDao(baseGroup, false));
    }

    /**
     * Deletes an baseGroupDao from repository
     *
     * @param baseGroupDao base group to delete
     */
    private void delete(BaseGroupDao baseGroupDao) {
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
        return find(identification, BaseGroup.ID_PREFIX, BaseGroup.class.getSimpleName(), g -> GroupAccessMapper.convertToBaseGroup(g, false), baseGroupRepository);
    }

    /**
     * Searches for an base group and its sub entities
     *
     * @param identification Id of the base group which is searched for
     * @return search result
     */
    public Optional<BaseGroup> findBaseGroupTree(String identification) {
        Optional<BaseGroupDao> root = find(identification, BaseGroup.ID_PREFIX, BaseGroup.class.getSimpleName(), baseGroupRepository);
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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, GROUP_LOG_PARAM, parentIdentification, page, size);
        return result;
    }

    /**
     * Counts all base groups at a parent privilege group
     *
     * @param parentIdentification identification of the parent
     * @return number of base groups
     */
    public Long countBasesAtPrivilegeGroup(String parentIdentification) {
        log.debug(COUNT_START_LOG_MESSAGE, GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);

        long result = countBasesAtPrivilegeGroup(new PrivilegeGroupDaoExt(parentIdentification));

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Searches for all base groups at a parent privilege group
     *
     * @param parentIdentification identification of the parent
     * @return List of base groups
     */
    public List<BaseGroup> findAllBaseAtPrivilegeGroup(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);

        List<BaseGroup> result = findAllBaseAtPrivilegeGroup(new PrivilegeGroupDaoExt(parentIdentification), null, null)
                .stream()
                .map(bg -> GroupAccessMapper.convertToBaseGroup(bg, false))
                .collect(Collectors.toList());

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all base groups at a parent privilege group
     *
     * @param parentIdentification identification of the
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return List of base groups
     */
    public List<BaseGroup> findAllBaseAtPrivilegeGroup(String parentIdentification, Integer page, Integer size) {
        log.debug(SEARCH_START_PAGE_LOG_MESSAGE, GROUPS_LOG_PARAM, page, size, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);

        List<BaseGroup> result = findAllBaseAtPrivilegeGroup(new PrivilegeGroupDaoExt(parentIdentification), page, size)
                .stream()
                .map(bg -> GroupAccessMapper.convertToBaseGroup(bg, false))
                .collect(Collectors.toList());

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification, page, size);
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
                    .collect(Collectors.toList());
        }
        return baseToBaseGroupRepository.findAllByBaseGroup(parent, PageRequest.of(page, size)).stream()
                .map(BaseGroupToBaseGroupDao::getSubBaseGroup)
                .collect(Collectors.toList());
    }

    /**
     * Count all base groups
     *
     * @param parent parent privilege group
     * @return number of base groups
     */
    private long countBasesAtPrivilegeGroup(PrivilegeGroupDao parent) {
        return privilegeToBaseGroupRepository.countByPrivilegeGroup(parent);
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
                    .collect(Collectors.toList());
        }
        return privilegeToBaseGroupRepository.findAllByPrivilegeGroup(parent, PageRequest.of(page, size)).stream()
                .map(PrivilegeGroupToBaseGroupDao::getBaseGroup)
                .collect(Collectors.toList());
    }

    /**
     * Stores a base group
     *
     * @param baseGroup base group which should be stored
     * @return Stored base group with additional generated ids, if missing before.
     * <br>
     * In case of not existing baseGroup for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<BaseGroup> save(BaseGroup baseGroup) {
        if (baseGroup.getIdentification() == null) {
            log.error(GET_PARENT_ID_MISSING_CHILD_ID_LOG_ERROR, COMMON_GROUP_LOG_PARAM, GROUP_LOG_PARAM);
            return Optional.empty();
        }
        Optional<Long> commonGroupId = baseGroupRepository.getIdOfParentCommonGroup(IdGenerator.generateId(baseGroup.getIdentification(), BaseGroup.ID_PREFIX));
        if (commonGroupId.isPresent()) {
            return save(baseGroup, IdGenerator.generateIdentification(commonGroupId.get(), CommonGroup.ID_PREFIX));
        }
        log.error(GET_PARENT_ID_NOT_FOUND_LOG_ERROR, COMMON_GROUP_LOG_PARAM);
        return Optional.empty();
    }

    /**
     * Stores a base group at a common group
     *
     * @param baseGroup           base group which should be stored
     * @param groupIdentification identification of the parent common group
     * @return Stored base group with additional generated ids, if missing before.
     * <br>
     * In case of not existing baseGroup for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<BaseGroup> save(BaseGroup baseGroup, String groupIdentification) {
        return save(baseGroup, groupIdentification
                , BaseGroup::getGroupName
                , () -> {
                    CommonGroupDao res = new CommonGroupDao();
                    res.setBasics(new ArrayList<>());
                    return res;
                }
                , (baseGroupDomainObject, parentDao) -> GroupAccessMapper.convertToBaseGroupDao(baseGroupDomainObject, false, parentDao)
                , baseGroupDao -> GroupAccessMapper.convertToBaseGroup(baseGroupDao, false)
                , baseGroupRepository);
    }

    /**
     * Adds a base to a privilege group
     *
     * @param privilegeGroupIdentification identification of the privilege group
     * @param baseGroupIdentification      identification of the base group to add
     * @param role                         role of the user at privilege group
     * @return {@code true} if the base group was added to the privilege group, otherwise {@code false}
     */
    public boolean addBaseToPrivilegeGroup(String privilegeGroupIdentification, String baseGroupIdentification, Role role) {
        return add(privilegeGroupIdentification, baseGroupIdentification, PrivilegeGroup.class.getSimpleName(), BaseGroup.class.getSimpleName()
                , PrivilegeGroup.ID_PREFIX, BaseGroup.ID_PREFIX
                , privilegeGroupRepository, baseGroupRepository, privilegeToBaseGroupRepository
                , (privilegeGroup, baseGroup) -> {
                    PrivilegeGroupToBaseGroupDao connection = new PrivilegeGroupToBaseGroupDao();
                    connection.setPrivilegeGroup(privilegeGroup);
                    connection.setBaseGroup(baseGroup);
                    connection.setFilterRole(role);
                    return connection;
                });
    }

    /**
     * Removes an base from a privilege group
     *
     * @param privilegeGroupIdentification Identification of the privilege group
     * @param baseGroupIdentification      Identification of the base group to remove
     * @return {@code true} if the base group was removed from the privilege group. Otherwise {@code false}
     */
    public boolean removeBaseFromPrivilegeGroup(String privilegeGroupIdentification, String baseGroupIdentification) {
        return remove(privilegeGroupIdentification, baseGroupIdentification, PrivilegeGroup.class.getSimpleName(), BaseGroup.class.getSimpleName()
                , PrivilegeGroup.ID_PREFIX, BaseGroup.ID_PREFIX, PrivilegeGroupDao::new, BaseGroupDao::new
                , privilegeToBaseGroupRepository::deleteByPrivilegeGroupAndBaseGroup);
    }


    /**
     * Adds a base to an other base group
     *
     * @param parentGroupIdentification identification of the parent base group
     * @param baseGroupIdentification   identification of the base group to add
     * @return {@code true} if the base group was added to the other base group, otherwise {@code false}
     */
    public boolean addBaseToBaseGroup(String parentGroupIdentification, String baseGroupIdentification) {
        return add(parentGroupIdentification, baseGroupIdentification, BaseGroup.class.getSimpleName(), BaseGroup.class.getSimpleName()
                , BaseGroup.ID_PREFIX, BaseGroup.ID_PREFIX
                , baseGroupRepository, baseGroupRepository, baseToBaseGroupRepository
                , (baseGroup, subBaseGroup) -> {
                    BaseGroupToBaseGroupDao connection = new BaseGroupToBaseGroupDao();
                    connection.setBaseGroup(baseGroup);
                    connection.setSubBaseGroup(subBaseGroup);
                    return connection;
                });
    }

    /**
     * Removes an base from an other base group
     *
     * @param parentGroupIdentification Identification of the parent base group
     * @param baseGroupIdentification   Identification of the base group to remove
     * @return {@code true} if the base group was removed from the other base group. Otherwise {@code false}
     */
    public boolean removeBaseFromBaseGroup(String parentGroupIdentification, String baseGroupIdentification) {
        return remove(parentGroupIdentification, baseGroupIdentification, BaseGroup.class.getSimpleName(), BaseGroup.class.getSimpleName()
                , BaseGroup.ID_PREFIX, BaseGroup.ID_PREFIX, BaseGroupDao::new, BaseGroupDao::new
                , baseToBaseGroupRepository::deleteByBaseGroupAndSubBaseGroup);
    }
}
