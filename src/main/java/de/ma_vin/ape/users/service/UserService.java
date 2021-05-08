package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dao.group.*;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.mapper.UserAccessMapper;
import de.ma_vin.ape.users.persistence.*;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Log4j2
@Data
public class UserService extends AbstractRepositoryService {

    public static final String ADMIN_OR_COMMON_GROUP_LOG_PARAM = "admin or common group";
    public static final String ADMIN_GROUP_LOG_PARAM = "admin group";
    public static final String COMMON_GROUP_LOG_PARAM = "common group";
    public static final String USER_LOG_PARAM = "user";
    public static final String USERS_LOG_PARAM = "users";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PrivilegeGroupRepository privilegeGroupRepository;
    @Autowired
    private PrivilegeGroupToUserRepository privilegeGroupToUserRepository;
    @Autowired
    private BaseGroupRepository baseGroupRepository;
    @Autowired
    private BaseGroupToUserRepository baseGroupToUserRepository;

    /**
     * Deletes a user from repository
     *
     * @param user User to delete
     */
    public void delete(User user) {
        delete(UserAccessMapper.convertToUserDao(user));
    }

    /**
     * Deletes a userDao from repository
     *
     * @param userDao User to delete
     */
    private void delete(UserDao userDao) {
        log.debug(DELETE_BEGIN_LOG_MESSAGE, USER_LOG_PARAM, userDao.getIdentification(), userDao.getId());

        userRepository.delete(userDao);

        log.debug(DELETE_END_LOG_MESSAGE, USER_LOG_PARAM, userDao.getIdentification(), userDao.getId());
    }

    /**
     * Checks whether a user exists for a given identification or not
     *
     * @param identification the identification to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    public boolean userExits(String identification) {
        return userDaoExits(IdGenerator.generateId(identification, User.ID_PREFIX));
    }

    /**
     * Checks whether a user exists for a given id or not
     *
     * @param id id to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    private boolean userDaoExits(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Searches for a user
     *
     * @param identification Id of the user which is searched for
     * @return search result
     */
    public Optional<User> findUser(String identification) {
        return find(identification, User.ID_PREFIX, User.class.getSimpleName(), UserAccessMapper::convertToUser, userRepository);
    }

    /**
     * Searches for all user children of a parent admin group
     *
     * @param parentIdentification identification of the parent
     * @return List of users
     */
    public List<User> findAllUsersAtAdminGroup(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, USERS_LOG_PARAM, ADMIN_GROUP_LOG_PARAM, parentIdentification);
        AdminGroupDao parent = new AdminGroupDao();
        parent.setIdentification(parentIdentification);

        List<User> result = new ArrayList<>();
        findAllUsers(parent).forEach(s -> result.add(UserAccessMapper.convertToUser(s)));

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), USERS_LOG_PARAM, ADMIN_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }


    /**
     * Searches for all user children of a parent common group
     *
     * @param parentIdentification identification of the parent
     * @return List of users
     */
    public List<User> findAllUsersAtCommonGroup(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, USERS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        CommonGroupDao parent = new CommonGroupDao();
        parent.setIdentification(parentIdentification);

        List<User> result = new ArrayList<>();
        findAllUsers(parent).forEach(s -> result.add(UserAccessMapper.convertToUser(s)));

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), USERS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all user children of a parent admin group
     *
     * @param parent parent
     * @return List of users
     */
    private List<UserDao> findAllUsers(AdminGroupDao parent) {
        return userRepository.findByParentAdminGroup(parent);
    }

    /**
     * Searches for all user children of a parent common group
     *
     * @param parent parent
     * @return List of users
     */
    private List<UserDao> findAllUsers(CommonGroupDao parent) {
        return userRepository.findByParentCommonGroup(parent);
    }

    /**
     * Stores a user
     *
     * @param user user which should be stored
     * @return Stored user with additional generated ids, if missing before.
     * <br>
     * In case of not existing user for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<User> save(User user) {
        if (user.getIdentification() == null) {
            log.error(GET_PARENT_ID_MISSING_CHILD_ID_LOG_ERROR, ADMIN_OR_COMMON_GROUP_LOG_PARAM, USER_LOG_PARAM);
            return Optional.empty();
        }
        Optional<Long> adminGroupId = userRepository.getIdOfParentAdminGroup(IdGenerator.generateId(user.getIdentification(), User.ID_PREFIX));
        if (adminGroupId.isPresent()) {
            return saveAtAdminGroup(user, IdGenerator.generateIdentification(adminGroupId.get(), AdminGroup.ID_PREFIX));
        }
        Optional<Long> commonGroupId = userRepository.getIdOfParentCommonGroup(IdGenerator.generateId(user.getIdentification(), User.ID_PREFIX));
        if (commonGroupId.isPresent()) {
            return saveAtCommonGroup(user, IdGenerator.generateIdentification(commonGroupId.get(), CommonGroup.ID_PREFIX));
        }
        log.error(GET_PARENT_ID_NOT_FOUND_LOG_ERROR, ADMIN_OR_COMMON_GROUP_LOG_PARAM);
        return Optional.empty();
    }

    /**
     * Stores a user at a admin group
     *
     * @param user                user which should be stored
     * @param groupIdentification identification of the parent admin group
     * @return Stored user with additional generated ids, if missing before.
     * <br>
     * In case of not existing user for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<User> saveAtAdminGroup(User user, String groupIdentification) {
        return save(user, groupIdentification
                , userDomainObject -> String.format("(%s, %s)", userDomainObject.getFirstName(), userDomainObject.getLastName())
                , () -> {
                    AdminGroupDao res = new AdminGroupDao();
                    res.setAdmins(new ArrayList<>());
                    return res;
                }
                , UserAccessMapper::convertToUserDao
                , UserAccessMapper::convertToUser
                , userRepository);
    }

    /**
     * Stores a user at a common group
     *
     * @param user                user which should be stored
     * @param groupIdentification identification of the parent common group
     * @return Stored user with additional generated ids, if missing before.
     * <br>
     * In case of not existing user for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<User> saveAtCommonGroup(User user, String groupIdentification) {
        return save(user, groupIdentification
                , userDomainObject -> String.format("(%s, %s)", userDomainObject.getFirstName(), userDomainObject.getLastName())
                , () -> {
                    CommonGroupDao res = new CommonGroupDao();
                    res.setAggUser(new ArrayList<>());
                    return res;
                }
                , UserAccessMapper::convertToUserDao
                , UserAccessMapper::convertToUser
                , userRepository);
    }

    /**
     * Adds an user to a privilege group
     *
     * @param privilegeGroupIdentification identification of the privilege group
     * @param userIdentification           identification of the user to add
     * @param role                         role of the user at privilege group
     * @return {@code true} if the user was added to the privilege group, otherwise {@code false}
     */
    public boolean addUserToPrivilegeGroup(String privilegeGroupIdentification, String userIdentification, Role role) {
        return add(privilegeGroupIdentification, userIdentification, PrivilegeGroup.class.getSimpleName(), User.class.getSimpleName()
                , PrivilegeGroup.ID_PREFIX, User.ID_PREFIX
                , privilegeGroupRepository, userRepository, privilegeGroupToUserRepository
                , (privilegeGroup, user) -> {
                    PrivilegeGroupToUserDao connection = new PrivilegeGroupToUserDao();
                    connection.setPrivilegeGroup(privilegeGroup);
                    connection.setUser(user);
                    connection.setFilterRole(role);
                    return connection;
                });
    }

    /**
     * Adds an user to a base group
     *
     * @param baseGroupIdentification identification of the base group
     * @param userIdentification      identification of the user to add
     * @return {@code true} if the user was added to the base group, otherwise {@code false}
     */
    public boolean addUserToBaseGroup(String baseGroupIdentification, String userIdentification) {
        return add(baseGroupIdentification, userIdentification, BaseGroup.class.getSimpleName(), User.class.getSimpleName()
                , BaseGroup.ID_PREFIX, User.ID_PREFIX
                , baseGroupRepository, userRepository, baseGroupToUserRepository
                , (baseGroup, user) -> {
                    BaseGroupToUserDao connection = new BaseGroupToUserDao();
                    connection.setBaseGroup(baseGroup);
                    connection.setUser(user);
                    return connection;
                });
    }

    /**
     * Removes an user from a privilege group
     *
     * @param privilegeGroupIdentification Identification of the privilege group
     * @param userIdentification           Identification of the user to remove
     * @return {@code true} if the user was removed from the privilege group. Otherwise {@code false}
     */
    public boolean removeUserFromPrivilegeGroup(String privilegeGroupIdentification, String userIdentification) {
        return remove(privilegeGroupIdentification, userIdentification, PrivilegeGroup.class.getSimpleName(), User.class.getSimpleName()
                , PrivilegeGroup.ID_PREFIX, User.ID_PREFIX, PrivilegeGroupDao::new, UserDao::new
                , privilegeGroupToUserRepository::deleteByPrivilegeGroupAndUser);
    }

    /**
     * Removes an user from a base group
     *
     * @param baseGroupIdentification Identification of the base group
     * @param userIdentification      Identification of the user to remove
     * @return {@code true} if the user was removed from the base group. Otherwise {@code false}
     */
    public boolean removeUserFromBaseGroup(String baseGroupIdentification, String userIdentification) {
        return remove(baseGroupIdentification, userIdentification, BaseGroup.class.getSimpleName(), User.class.getSimpleName()
                , BaseGroup.ID_PREFIX, User.ID_PREFIX, BaseGroupDao::new, UserDao::new, baseGroupToUserRepository::deleteByBaseGroupAndUser);
    }
}
