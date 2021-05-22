package de.ma_vin.ape.users.service;


import de.ma_vin.ape.users.enums.Role;
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
     * Searches for all base groups at a parent common group
     *
     * @param parentIdentification identification of the parent
     * @return List of base groups
     */
    public List<BaseGroup> findAllBaseGroups(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        CommonGroupDao parent = new CommonGroupDao();
        parent.setIdentification(parentIdentification);

        List<BaseGroup> result = new ArrayList<>();
        findAllBaseGroups(parent).forEach(pg -> result.add(GroupAccessMapper.convertToBaseGroup(pg, false)));

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all base groups at an other parent base group
     *
     * @param parentIdentification identification of the parent
     * @return List of base groups
     */
    public List<BaseGroup> findAllBasesAtBaseGroup(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, GROUPS_LOG_PARAM, GROUP_LOG_PARAM, parentIdentification);
        BaseGroupDao parent = new BaseGroupDao();
        parent.setIdentification(parentIdentification);

        List<BaseGroup> result = new ArrayList<>();
        findAllBasesAtBaseGroup(parent).forEach(pg -> result.add(GroupAccessMapper.convertToBaseGroup(pg, false)));

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), GROUPS_LOG_PARAM, GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all base groups
     *
     * @param parent parent common group
     * @return List of base groups
     */
    private List<BaseGroupDao> findAllBaseGroups(CommonGroupDao parent) {
        return baseGroupRepository.findByParentCommonGroup(parent);
    }

    /**
     * Searches for all base groups
     *
     * @param parent parent base group
     * @return List of base groups
     */
    private List<BaseGroupDao> findAllBasesAtBaseGroup(BaseGroupDao parent) {
        return baseToBaseGroupRepository.findAllByBaseGroup(parent).stream().map(BaseGroupToBaseGroupDao::getSubBaseGroup).collect(Collectors.toList());
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
