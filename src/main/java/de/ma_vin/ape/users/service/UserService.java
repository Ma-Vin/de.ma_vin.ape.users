package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.group.BaseGroupExt;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.dao.group.*;
import de.ma_vin.ape.users.model.gen.dao.resource.UserResourceDao;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

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
    private UserResourceService userResourceService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PrivilegeGroupRepository privilegeGroupRepository;
    @Autowired
    private PrivilegeGroupToUserRepository privilegeGroupToUserRepository;
    @Autowired
    private BaseGroupRepository baseGroupRepository;
    @Autowired
    private BaseToBaseGroupRepository baseToBaseGroupRepository;
    @Autowired
    private BaseGroupToUserRepository baseGroupToUserRepository;
    @Autowired
    private BaseGroupService baseGroupService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${db.user.initUserWithDefaultPwd}")
    private boolean initUserWithDefaultPwd;

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

        findUser(userDao.getIdentification()).ifPresent(u -> {
            if (u.getImage() != null) {
                log.debug("Delete image with identification \"{}\" at user \"{}\"", u.getImage().getIdentification(), userDao.getIdentification());
                userResourceService.delete(u.getImage());
            }
            if (u.getSmallImage() != null) {
                log.debug("Delete small image with identification \"{}\" at user \"{}\"", u.getImage().getIdentification(), userDao.getIdentification());
                userResourceService.delete(u.getSmallImage());
            }
        });

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
     * Searches for all user children of a parent base group
     *
     * @param parentIdentification identification of the parent
     * @param dissolveSubgroups    indicator if the users of subgroups should also be added
     * @return List of users
     */
    public List<User> findAllUsersAtBaseGroup(String parentIdentification, boolean dissolveSubgroups) {
        log.debug(SEARCH_START_LOG_MESSAGE, USERS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        BaseGroupDao parent = new BaseGroupDao();
        parent.setIdentification(parentIdentification);

        List<User> result = new ArrayList<>();
        if (dissolveSubgroups) {
            baseGroupService.findBaseGroupTree(parentIdentification).ifPresent(b -> result.addAll(((BaseGroupExt) b).getAllUsers()));
        } else {
            baseGroupToUserRepository.findAllByBaseGroup(parent).stream()
                    .map(btu -> UserAccessMapper.convertToUser(btu.getUser()))
                    .sorted(Comparator.comparing(User::getIdentification)).forEach(result::add);
        }

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
                , userRepository
                , createUserAdoption());
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
                , userRepository
                , createUserAdoption());
    }

    private Adoption<UserDao> createUserAdoption() {
        return (dao, storedDao) -> {
            if (isImageToDelete(dao.getImage(), storedDao.getImage())) {
                log.debug("Image at user \"{}\" has changed: \"{}\" -> \"{}\"", dao.getIdentification()
                        , getLogIdentification(storedDao.getImage()), getLogIdentification(dao.getImage()));
                userResourceService.delete(storedDao.getImage());

                if (isImageToSve(dao.getImage())) {
                    log.debug("Image at user \"{}\" is new and has to stored", dao.getIdentification());
                    userResourceService.save(dao.getImage()).ifPresent(dao::setImage);
                }
            }
            if (isImageToDelete(dao.getSmallImage(), storedDao.getSmallImage())) {
                log.debug("Small image at user \"{}\" has changed: \"{}\" -> \"{}\"", dao.getIdentification()
                        , getLogIdentification(storedDao.getSmallImage()), getLogIdentification(dao.getSmallImage()));
                userResourceService.delete(storedDao.getSmallImage());

                if (isImageToSve(dao.getSmallImage())) {
                    log.debug("Small image at user \"{}\" is new and has to stored", dao.getIdentification());
                    userResourceService.save(dao.getSmallImage()).ifPresent(dao::setSmallImage);
                }
            }
            return dao;
        };
    }

    boolean isImageToDelete(UserResourceDao userResourceDao, UserResourceDao storedUserResourceDao) {
        return storedUserResourceDao != null && (userResourceDao == null || !storedUserResourceDao.getIdentification().equals(userResourceDao.getIdentification()));
    }

    boolean isImageToSve(UserResourceDao userResourceDao) {
        return userResourceDao != null && userResourceDao.getId() == null;
    }

    private String getLogIdentification(UserResourceDao userResourceDao) {
        return userResourceDao == null ? "null" : userResourceDao.getIdentification();
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

    /**
     * Set the password of an user
     */
    public boolean setPassword(String userIdentification, String rawPassword) {
        Optional<User> user = findUser(userIdentification);
        if (user.isEmpty()) {
            log.error("The user with identification {} does not exists. The password could not be set", userIdentification);
            return false;
        }
        if (!isPasswordRequirementFulfilled(rawPassword, user.get().getPassword(), userIdentification)) {
            log.error("The password for user {} does not fulfill requirements", userIdentification);
            return false;
        }
        ((UserExt) user.get()).setRawPassword(passwordEncoder, rawPassword);
        if (save(user.get()).isPresent()) {
            log.debug("The password for user {} was changed", userIdentification);
            return true;
        }
        log.error("The password for user {} was not changed", userIdentification);
        return false;
    }

    /**
     * Checks if the password fulfill the requirements
     *
     * @param rawPassword        the password to check
     * @param oldPassword        the previous password
     * @param userIdentification Identification of the user whose password is set
     * @return {@code true} if the password contains a lower and upper alphabet character, a special sign, a number,
     * has at least 10 characters and does not equal the previous one. Otherwise {@code false}.
     */
    private boolean isPasswordRequirementFulfilled(String rawPassword, String oldPassword, String userIdentification) {
        final String ALPHABET_LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String ALPHABET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String SPECIAL_SIGNS = "!#$%()*+,-./:;=?@[]^_{|}~";
        final String NUMBERS = "0123456789";
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            log.debug("The password for user {} is empty", userIdentification);
            return false;
        }
        if (rawPassword.length() < 10) {
            log.debug("The password for user {} is to short", userIdentification);
            return false;
        }
        if (!containsAtLeastOne(rawPassword, ALPHABET_LOWER)) {
            log.debug("The password for user {} should contain at least one lower alphabet character", userIdentification);
            return false;
        }
        if (!containsAtLeastOne(rawPassword, ALPHABET_UPPER)) {
            log.debug("The password for user {} should contain at least one upper alphabet character", userIdentification);
            return false;
        }
        if (!containsAtLeastOne(rawPassword, SPECIAL_SIGNS)) {
            log.debug("The password for user {} should contain at least one special sign character", userIdentification);
            return false;
        }
        if (!containsAtLeastOne(rawPassword, NUMBERS)) {
            log.debug("The password for user {} should contain at least one number", userIdentification);
            return false;
        }
        if (oldPassword != null && passwordEncoder.matches(rawPassword, oldPassword)) {
            log.debug("The password for user {} equals the old onw", userIdentification);
            return false;
        }
        return true;
    }

    /**
     * Checks whether given characters are contained at a given text
     *
     * @param textToCheck                     text which should contain at least one of the character set
     * @param atLeastOneCharShouldBeContained the character set
     * @return {@code true} if at least character was found. Otherwise {@code false}
     */
    private boolean containsAtLeastOne(String textToCheck, String atLeastOneCharShouldBeContained) {
        for (int i = 0; i < atLeastOneCharShouldBeContained.length(); i++) {
            if (textToCheck.contains(atLeastOneCharShouldBeContained.substring(i, i + 1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the role of an user at common group
     *
     * @param userIdentification identification of user whose role should be changed
     * @param role               role to set
     */
    public boolean setRole(String userIdentification, Role role) {
        Optional<User> user = findUser(userIdentification);
        if (user.isEmpty()) {
            log.debug("The user {} could not be found and the role {} was not set", userIdentification, role.getDescription());
            return false;
        }
        if (role.equals(user.get().getRole())) {
            log.debug("The user {} gets the same role {} again", userIdentification, role.getDescription());
            return true;
        }
        user.get().setRole(role);
        if (save(user.get()).isEmpty()) {
            log.error("The role {} was set not at user {}", role.getDescription(), userIdentification);
            return false;
        }
        log.debug("The role {} was set at user {}", role.getDescription(), userIdentification);
        return true;
    }
}
