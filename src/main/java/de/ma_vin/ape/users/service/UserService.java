package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.dao.group.AdminGroupDaoExt;
import de.ma_vin.ape.users.model.dao.group.BaseGroupDaoExt;
import de.ma_vin.ape.users.model.dao.group.CommonGroupDaoExt;
import de.ma_vin.ape.users.model.dao.group.PrivilegeGroupDaoExt;
import de.ma_vin.ape.users.model.domain.group.BaseGroupExt;
import de.ma_vin.ape.users.model.domain.group.PrivilegeGroupExt;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Log4j2
@Data
public class UserService extends AbstractRepositoryService {

    public static final String ADMIN_OR_COMMON_GROUP_LOG_PARAM = "admin or common group";
    public static final String ADMIN_GROUP_LOG_PARAM = "admin group";
    public static final String COMMON_GROUP_LOG_PARAM = "common group";
    public static final String BASE_GROUP_LOG_PARAM = "base group";
    public static final String PRIVILEGE_GROUP_LOG_PARAM = "privilege group";
    public static final String USER_LOG_PARAM = "user";
    public static final String USERS_LOG_PARAM = "users";
    public static final String AVAILABLE_USERS_LOG_PARAM = " available users";

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
    private PrivilegeGroupService privilegeGroupService;
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
     * Count all user children of a parent admin group
     *
     * @param parentIdentification identification of the parent
     * @return number of users
     */
    public Long countUsersAtAdminGroup(String parentIdentification) {
        log.debug(COUNT_START_LOG_MESSAGE, USERS_LOG_PARAM, ADMIN_GROUP_LOG_PARAM, parentIdentification);
        AdminGroupDao parent = new AdminGroupDao();
        parent.setIdentification(parentIdentification);

        long result = countUsers(parent);

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, USERS_LOG_PARAM, ADMIN_GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Count all user children of a parent common group
     *
     * @param parentIdentification identification of the parent
     * @return number of users
     */
    public Long countUsersAtCommonGroup(String parentIdentification) {
        log.debug(COUNT_START_LOG_MESSAGE, USERS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        CommonGroupDao parent = new CommonGroupDao();
        parent.setIdentification(parentIdentification);

        long result = countUsers(parent);

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, USERS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Count all user children of a parent base group
     *
     * @param parentIdentification identification of the parent
     * @return number of users
     */
    public Long countUsersAtBaseGroup(String parentIdentification) {
        log.debug(COUNT_START_LOG_MESSAGE, USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, parentIdentification);
        BaseGroupDao parent = new BaseGroupDao();
        parent.setIdentification(parentIdentification);

        long result = countUsers(parent);

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Counts all available users for a base group
     *
     * @param baseGroupIdentification identification of the base group
     * @return number of users
     */
    public Long countAvailableUsersForBaseGroup(String baseGroupIdentification) {
        log.debug(COUNT_FOR_START_LOG_MESSAGE, AVAILABLE_USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, baseGroupIdentification);

        BaseGroupDao baseGroupDao = baseGroupRepository.getById(IdGenerator.generateId(baseGroupIdentification, BaseGroup.ID_PREFIX));

        long result = baseGroupToUserRepository.countAvailableUsers(baseGroupDao, baseGroupDao.getParentCommonGroup());

        log.debug(COUNT_FOR_RESULT_LOG_MESSAGE, result, AVAILABLE_USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, baseGroupIdentification);
        return Long.valueOf(result);
    }

    /**
     * Count all user children of a parent privilege group
     *
     * @param parentIdentification identification of the parent
     * @return number of users
     */
    public Long countUsersAtPrivilegeGroup(String parentIdentification) {
        return countUsersAtPrivilegeGroup(parentIdentification, null);
    }

    /**
     * Count all user children of a parent privilege group
     *
     * @param parentIdentification identification of the parent
     * @param role                 role which the users to count should have
     * @return number of users
     */
    public Long countUsersAtPrivilegeGroup(String parentIdentification, Role role) {
        log.debug(COUNT_START_LOG_MESSAGE, USERS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);
        PrivilegeGroupDao parent = new PrivilegeGroupDao();
        parent.setIdentification(parentIdentification);

        long result = countUsers(parent, role);

        log.debug(COUNT_RESULT_LOG_MESSAGE, result, USERS_LOG_PARAM, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);
        return Long.valueOf(result);
    }

    /**
     * Searches for all user children of a parent admin group
     *
     * @param parentIdentification identification of the parent
     * @return List of users
     */
    public List<User> findAllUsersAtAdminGroup(String parentIdentification) {
        log.debug(SEARCH_START_LOG_MESSAGE, USERS_LOG_PARAM, ADMIN_GROUP_LOG_PARAM, parentIdentification);

        List<User> result = findAllUsers(new AdminGroupDaoExt(parentIdentification), null, null)
                .stream()
                .map(UserAccessMapper::convertToUser)
                .collect(Collectors.toList());

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), USERS_LOG_PARAM, ADMIN_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all user children of a parent admin group
     *
     * @param parentIdentification identification of the parent
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return List of users
     */
    public List<User> findAllUsersAtAdminGroup(String parentIdentification, Integer page, Integer size) {
        log.debug(SEARCH_START_PAGE_LOG_MESSAGE, USERS_LOG_PARAM, page, size, ADMIN_GROUP_LOG_PARAM, parentIdentification);

        List<User> result = findAllUsers(new AdminGroupDaoExt(parentIdentification), page, size)
                .stream()
                .map(UserAccessMapper::convertToUser)
                .collect(Collectors.toList());

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), USERS_LOG_PARAM, ADMIN_GROUP_LOG_PARAM, parentIdentification, page, size);
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

        List<User> result = findAllUsers(new CommonGroupDaoExt(parentIdentification), null, null)
                .stream()
                .map(UserAccessMapper::convertToUser)
                .collect(Collectors.toList());

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), USERS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all user children of a parent common group
     *
     * @param parentIdentification identification of the parent
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return List of users
     */
    public List<User> findAllUsersAtCommonGroup(String parentIdentification, Integer page, Integer size) {
        log.debug(SEARCH_START_PAGE_LOG_MESSAGE, USERS_LOG_PARAM, page, size, COMMON_GROUP_LOG_PARAM, parentIdentification);

        List<User> result = findAllUsers(new CommonGroupDaoExt(parentIdentification), page, size)
                .stream()
                .map(UserAccessMapper::convertToUser)
                .collect(Collectors.toList());

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), USERS_LOG_PARAM, COMMON_GROUP_LOG_PARAM, parentIdentification, page, size);
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
        log.debug(SEARCH_START_LOG_MESSAGE, USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, parentIdentification);

        List<User> result = new ArrayList<>();
        if (dissolveSubgroups) {
            baseGroupService.findBaseGroupTree(parentIdentification).ifPresent(b -> result.addAll(((BaseGroupExt) b).getAllUsers()));
        } else {
            findAllUsers(new BaseGroupDaoExt(parentIdentification), null, null)
                    .stream()
                    .map(UserAccessMapper::convertToUser)
                    .sorted(Comparator.comparing(User::getIdentification)).forEach(result::add);
        }

        log.debug(SEARCH_RESULT_LOG_MESSAGE, result.size(), USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, parentIdentification);
        return result;
    }

    /**
     * Searches for all user children of a parent base group. This method does not dissolve subgroups
     *
     * @param parentIdentification identification of the parent
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return List of users
     */
    public List<User> findAllUsersAtBaseGroup(String parentIdentification, Integer page, Integer size) {
        log.debug(SEARCH_START_PAGE_LOG_MESSAGE, USERS_LOG_PARAM, page, size, BASE_GROUP_LOG_PARAM, parentIdentification);

        List<User> result = findAllUsers(new BaseGroupDaoExt(parentIdentification), page, size)
                .stream()
                .map(UserAccessMapper::convertToUser)
                .sorted(Comparator.comparing(User::getIdentification)).collect(Collectors.toList());

        log.debug(SEARCH_RESULT_PAGE_LOG_MESSAGE, result.size(), USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, parentIdentification, page, size);
        return result;
    }

    /**
     * Searches for all available user who are not added to the base group yet
     *
     * @param baseGroupIdentification identification of the base group
     * @return List of users
     */
    public List<User> findAllAvailableUsersForBaseGroup(String baseGroupIdentification) {
        log.debug(SEARCH_FOR_START_LOG_MESSAGE, AVAILABLE_USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, baseGroupIdentification);

        BaseGroupDao baseGroupDao = baseGroupRepository.getById(IdGenerator.generateId(baseGroupIdentification, BaseGroup.ID_PREFIX));

        List<User> result = findAllAvailableUsers(baseGroupDao, null, null)
                .stream()
                .map(UserAccessMapper::convertToUser)
                .sorted(Comparator.comparing(User::getIdentification)).collect(Collectors.toList());

        log.debug(SEARCH_FOR_RESULT_LOG_MESSAGE, result.size(), AVAILABLE_USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, baseGroupIdentification);
        return result;
    }

    /**
     * Searches for all available user who are not added to the base group yet
     *
     * @param baseGroupIdentification identification of the base group
     * @param page                    zero-based page index, must not be negative.
     * @param size                    the size of the page to be returned, must be greater than 0.
     * @return List of users
     */
    public List<User> findAllAvailableUsersForBaseGroup(String baseGroupIdentification, Integer page, Integer size) {
        log.debug(SEARCH_FOR_START_PAGE_LOG_MESSAGE, AVAILABLE_USERS_LOG_PARAM, page, size, BASE_GROUP_LOG_PARAM, baseGroupIdentification);

        BaseGroupDao baseGroupDao = baseGroupRepository.getById(IdGenerator.generateId(baseGroupIdentification, BaseGroup.ID_PREFIX));

        List<User> result = findAllAvailableUsers(baseGroupDao, page, size)
                .stream()
                .map(UserAccessMapper::convertToUser)
                .sorted(Comparator.comparing(User::getIdentification)).collect(Collectors.toList());

        log.debug(SEARCH_FOR_RESULT_PAGE_LOG_MESSAGE, result.size(), AVAILABLE_USERS_LOG_PARAM, BASE_GROUP_LOG_PARAM, baseGroupIdentification, page, size);
        return result;
    }


    /**
     * Searches for all user children of a parent privilege group
     *
     * @param parentIdentification identification of the parent
     * @param role                 role of the users. If {@code null} or {@link Role#NOT_RELEVANT} all roles will be loaded
     * @param dissolveSubgroups    indicator if the users of subgroups should also be added
     * @return Map of roles and their users
     */
    public Map<Role, List<User>> findAllUsersAtPrivilegeGroup(String parentIdentification, Role role, boolean dissolveSubgroups) {
        log.debug("Search for users with role {} with at {} with identification \"{}\""
                , role != null ? role.getDescription() : "null", PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);

        Map<Role, List<User>> result;

        if (dissolveSubgroups) {
            result = findAllDissolvedUsersAtPrivilegeGroup(parentIdentification, role);
        } else {
            result = findAllUsersAtPrivilegeGroup(new PrivilegeGroupDaoExt(parentIdentification), role, null, null);
        }

        result.forEach((r, l) -> log.debug("{} users with role {} are found at {} with identification \"{}\""
                , l.size(), r.getDescription(), PRIVILEGE_GROUP_LOG_PARAM, parentIdentification));

        return result;
    }

    /**
     * Searches for all user children of a parent privilege group. This method does not dissolve subgroups
     *
     * @param parentIdentification identification of the parent
     * @param role                 role of the users. If {@code null} or {@link Role#NOT_RELEVANT} all roles will be loaded
     * @param page                 zero-based page index, must not be negative.
     * @param size                 the size of the page to be returned, must be greater than 0.
     * @return Map of roles and their users
     */
    public Map<Role, List<User>> findAllUsersAtPrivilegeGroup(String parentIdentification, Role role, Integer page, Integer size) {
        log.debug("Search for users with role {} with page {} and size {} at {} with identification \"{}\""
                , role != null ? role.getDescription() : "null", page, size, PRIVILEGE_GROUP_LOG_PARAM, parentIdentification);

        Map<Role, List<User>> result = findAllUsersAtPrivilegeGroup(new PrivilegeGroupDaoExt(parentIdentification), role, page, size);

        result.forEach((r, l) -> log.debug("{} users with role {} are found at {} with identification \"{}\""
                , l.size(), r.getDescription(), PRIVILEGE_GROUP_LOG_PARAM, parentIdentification));

        return result;
    }

    /**
     * Searches for all user children of a parent privilege group. This method does not dissolve subgroups
     *
     * @param parent privilege group
     * @param role   role of the users. If {@code null} or {@link Role#NOT_RELEVANT} all roles will be loaded
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return Map of roles and their users
     */
    private Map<Role, List<User>> findAllUsersAtPrivilegeGroup(PrivilegeGroupDao parent, Role role, Integer page, Integer size) {
        Map<Role, List<User>> result = new EnumMap<>(Role.class);
        if (role == null || Role.NOT_RELEVANT.equals(role)) {
            findAllUsers(parent, page, size).entrySet()
                    .forEach(e ->
                            result.computeIfAbsent(e.getKey(), r -> new ArrayList<>())
                                    .addAll(e.getValue().stream()
                                            .map(UserAccessMapper::convertToUser)
                                            .collect(Collectors.toList())
                                    )
                    );
        } else {
            result.put(role, findAllUsers(parent, role, page, size).stream()
                    .map(UserAccessMapper::convertToUser)
                    .sorted(Comparator.comparing(User::getIdentification))
                    .collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * Searches for all user children of a parent privilege group. This method does dissolve subgroups
     *
     * @param parentIdentification identification of the parent
     * @param role                 role of the users. If {@code null} or {@link Role#NOT_RELEVANT} all roles will be loaded
     * @return Map of roles and their users
     */
    private Map<Role, List<User>> findAllDissolvedUsersAtPrivilegeGroup(String parentIdentification, Role role) {
        Map<Role, List<User>> result = new EnumMap<>(Role.class);

        Optional<PrivilegeGroup> privilegeGroup = privilegeGroupService.findPrivilegeGroupTree(parentIdentification);
        if (privilegeGroup.isEmpty() || !(privilegeGroup.get() instanceof PrivilegeGroupExt)) {
            log.error("Could not find all dissolved users at privilege group {}, because the loaded subtree was empty", parentIdentification);
            return Collections.emptyMap();
        }
        if (role == null || Role.NOT_RELEVANT.equals(role)) {
            for (Role r : Role.values()) {
                if (Role.NOT_RELEVANT.equals(r)) {
                    continue;
                }
                result.computeIfAbsent(r, k -> new ArrayList<>()).addAll(((PrivilegeGroupExt) privilegeGroup.get()).getUsersByRole(r, true));
            }
        } else {
            result.computeIfAbsent(role, k -> new ArrayList<>()).addAll(((PrivilegeGroupExt) privilegeGroup.get()).getUsersByRole(role, true));
        }

        return result;
    }

    /**
     * Count all user children of a parent admin group
     *
     * @param parent parent
     * @return number of users
     */
    private long countUsers(AdminGroupDao parent) {
        return userRepository.countByParentAdminGroup(parent);
    }

    /**
     * Count all user children of a parent admin group
     *
     * @param parent parent
     * @return number of users
     */
    private long countUsers(CommonGroupDao parent) {
        return userRepository.countByParentCommonGroup(parent);
    }

    /**
     * Count all user children of a parent base group
     *
     * @param parent parent
     * @return number of users
     */
    private long countUsers(BaseGroupDao parent) {
        return baseGroupToUserRepository.countByBaseGroup(parent);
    }

    /**
     * Count all user children of a parent privilege group
     *
     * @param parent parent
     * @param role   role which the users to count should have
     * @return number of users
     */
    private long countUsers(PrivilegeGroupDao parent, Role role) {
        return role == null || Role.NOT_RELEVANT.equals(role)
                ? privilegeGroupToUserRepository.countByPrivilegeGroup(parent)
                : privilegeGroupToUserRepository.countByPrivilegeGroupAndFilterRole(parent, role);
    }

    /**
     * Searches for all user children of a parent admin group
     *
     * @param parent parent
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of users. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private List<UserDao> findAllUsers(AdminGroupDao parent, Integer page, Integer size) {
        if (page == null || size == null) {
            return userRepository.findByParentAdminGroup(parent);
        }
        return userRepository.findByParentAdminGroup(parent, PageRequest.of(page, size));
    }

    /**
     * Searches for all user children of a parent common group
     *
     * @param parent parent
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of users. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private List<UserDao> findAllUsers(CommonGroupDao parent, Integer page, Integer size) {
        if (page == null || size == null) {
            return userRepository.findByParentCommonGroup(parent);
        }
        return userRepository.findByParentCommonGroup(parent, PageRequest.of(page, size));
    }

    /**
     * Searches for all user children of a parent base group
     *
     * @param parent parent
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of users. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private List<UserDao> findAllUsers(BaseGroupDao parent, Integer page, Integer size) {
        if (page == null || size == null) {
            return baseGroupToUserRepository.findAllByBaseGroup(parent).stream()
                    .map(BaseGroupToUserDao::getUser)
                    .collect(Collectors.toList());
        }
        return baseGroupToUserRepository.findAllByBaseGroup(parent, PageRequest.of(page, size)).stream()
                .map(BaseGroupToUserDao::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Searches for all user children of a parent base group
     *
     * @param parent
     * @param role   role of the users. not null
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of users. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private List<UserDao> findAllUsers(PrivilegeGroupDao parent, Role role, Integer page, Integer size) {
        if (page == null || size == null) {
            return privilegeGroupToUserRepository.findAllByPrivilegeGroupAndFilterRole(parent, role).stream()
                    .map(PrivilegeGroupToUserDao::getUser)
                    .collect(Collectors.toList());
        }
        return privilegeGroupToUserRepository.findAllByPrivilegeGroupAndFilterRole(parent, role, PageRequest.of(page, size)).stream()
                .map(PrivilegeGroupToUserDao::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Searches for all user children of a parent base group
     *
     * @param parent
     * @param page   zero-based page index, must not be negative.
     * @param size   the size of the page to be returned, must be greater than 0.
     * @return List of users. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private Map<Role, List<UserDao>> findAllUsers(PrivilegeGroupDao parent, Integer page, Integer size) {
        Map<Role, List<UserDao>> result = new EnumMap<>(Role.class);
        if (page == null || size == null) {
            privilegeGroupToUserRepository.findAllByPrivilegeGroup(parent)
                    .forEach(ptu -> result.computeIfAbsent(ptu.getFilterRole(), r -> new ArrayList<>()).add(ptu.getUser()));
        } else {
            privilegeGroupToUserRepository.findAllByPrivilegeGroup(parent, PageRequest.of(page, size)).stream()
                    .forEach(ptu -> result.computeIfAbsent(ptu.getFilterRole(), r -> new ArrayList<>()).add(ptu.getUser()));
        }
        return result;
    }

    /**
     * Searches for all available user who are not added to the base group yet
     *
     * @param baseGroupDao base group
     * @param page         zero-based page index, must not be negative.
     * @param size         the size of the page to be returned, must be greater than 0.
     * @return List of users. If {@code page} or {@code size} are {@code null} everything will be loaded
     */
    private List<UserDao> findAllAvailableUsers(BaseGroupDao baseGroupDao, Integer page, Integer size) {
        if (page == null || size == null) {
            return baseGroupToUserRepository.findAvailableUsers(baseGroupDao, baseGroupDao.getParentCommonGroup());
        }
        return baseGroupToUserRepository.findAvailableUsers(baseGroupDao, baseGroupDao.getParentCommonGroup(), PageRequest.of(page, size));
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
     *
     * @param userIdentification identification of the user
     * @param rawPassword        the password to set
     * @param isGlobalAdminCheck indicator whether an global admin is to check or not
     * @return {@code true} if the password was set. Otherwise {@code false}
     */
    public boolean setPassword(String userIdentification, String rawPassword, boolean isGlobalAdminCheck) {
        Optional<User> user = findUser(userIdentification);
        if (user.isEmpty()) {
            log.error("The user with identification {} does not exists. The password could not be set", userIdentification);
            return false;
        }
        if (isGlobalAdminCheck && !user.get().isGlobalAdmin()) {
            log.error("The user with identification {} is not an admin, but it was tried to set the password as if. The password could not be set", userIdentification);
            return false;
        }
        if (!isGlobalAdminCheck && user.get().isGlobalAdmin()) {
            log.error("The user with identification {} is  an admin, but it was tried to set the password like for a normal user. The password could not be set", userIdentification);
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
