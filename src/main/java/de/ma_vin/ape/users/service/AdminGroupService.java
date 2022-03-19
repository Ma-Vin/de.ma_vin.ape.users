package de.ma_vin.ape.users.service;


import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.mapper.GroupAccessMapper;
import de.ma_vin.ape.users.persistence.AdminGroupRepository;
import de.ma_vin.ape.users.service.history.AbstractChangeService;
import de.ma_vin.ape.users.service.history.AdminGroupChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Data
@Log4j2
public class AdminGroupService extends AbstractRepositoryService<AdminGroupDao> {
    public static final String GROUP_LOG_PARAM = "admin group";
    public static final String GROUPS_LOG_PARAM = "admin groups";

    @Autowired
    private AdminGroupRepository adminGroupRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private AdminGroupChangeService adminGroupChangeService;

    @Override
    protected AbstractChangeService<AdminGroupDao> getChangeService() {
        return adminGroupChangeService;
    }

    /**
     * Deletes an admin group from repository
     *
     * @param adminGroup            admin group to delete
     * @param deleterIdentification The identification of the user who is deleting
     */
    public void delete(AdminGroup adminGroup, String deleterIdentification) {
        delete(GroupAccessMapper.convertToAdminGroupDao(adminGroup, false), deleterIdentification);
    }

    /**
     * Deletes an adminGroupDao from repository
     *
     * @param adminGroupDao         admin group to delete
     * @param deleterIdentification The identification of the user who is deleting
     */
    private void delete(AdminGroupDao adminGroupDao, String deleterIdentification) {
        log.debug(DELETE_BEGIN_LOG_MESSAGE, GROUP_LOG_PARAM, adminGroupDao.getIdentification(), adminGroupDao.getId());

        List<User> users = userService.findAllUsersAtAdminGroup(adminGroupDao.getIdentification());
        log.debug(DELETE_SUB_ENTITY_LOG_MESSAGE, users.size(), UserService.USERS_LOG_PARAM, GROUP_LOG_PARAM
                , adminGroupDao.getIdentification(), adminGroupDao.getId());
        users.forEach(u -> userService.delete(u, deleterIdentification));

        adminGroupChangeService.delete(adminGroupDao, deleterIdentification);
        adminGroupRepository.delete(adminGroupDao);

        log.debug(DELETE_END_LOG_MESSAGE, GROUP_LOG_PARAM, adminGroupDao.getIdentification(), adminGroupDao.getId());
    }

    /**
     * Checks whether an admin group exists for a given identification or not
     *
     * @param identification the identification to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    public boolean adminGroupExits(String identification) {
        return adminGroupDaoExits(IdGenerator.generateId(identification, AdminGroup.ID_PREFIX));
    }

    /**
     * Checks whether an admin group exists for a given id or not
     *
     * @param id id to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    private boolean adminGroupDaoExits(Long id) {
        return adminGroupRepository.existsById(id);
    }

    /**
     * Searches for an admin group
     *
     * @param identification Id of the admin group which is searched for
     * @return search result
     */
    public Optional<AdminGroup> findAdminGroup(String identification) {
        return find(identification, AdminGroup.ID_PREFIX, AdminGroup.class.getSimpleName(), g -> GroupAccessMapper.convertToAdminGroup(g, false), adminGroupRepository);
    }

    /**
     * Searches for all admin groups
     *
     * @return List of admin groups
     */
    public List<AdminGroup> findAllAdminGroups() {
        log.debug("Search for all admin groups");

        List<AdminGroup> result = adminGroupRepository.findAll().stream().map(dao -> GroupAccessMapper.convertToAdminGroup(dao, false)).toList();

        log.debug("{} admin groups found", result.size());
        return result;
    }


    /**
     * Stores an admin group
     *
     * @param adminGroup           admin group which should be stored
     * @param editorIdentification The identification of the user who is saving
     * @return Stored admin group with additional generated ids, if missing before.
     * <br>
     * In case of not existing adminGroup for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<AdminGroup> save(AdminGroup adminGroup, String editorIdentification) {
        AdminGroupDao adminGroupDao = GroupAccessMapper.convertToAdminGroupDao(adminGroup, false);

        if (adminGroupDao.getIdentification() == null) {
            log.debug("There is not any identification, so the admin group with name {} will be stored the first time", adminGroupDao.getGroupName());
            adminGroupDao = adminGroupRepository.save(adminGroupDao);
            AdminGroup result = GroupAccessMapper.convertToAdminGroup(adminGroupDao, false);
            log.debug("The admin group with name {} was stored with id {} and corresponding identification {}"
                    , adminGroupDao.getGroupName(), adminGroupDao.getId(), result.getIdentification());

            adminGroupChangeService.saveCreation(adminGroupDao, editorIdentification);

            return Optional.of(result);
        }

        Optional<AdminGroupDao> storedAdminGroupDao = adminGroupRepository.findById(adminGroupDao.getId());
        if (storedAdminGroupDao.isEmpty()) {
            log.debug("The admin group with identification {} and id {} does not exists and could not be saved", adminGroup.getIdentification(), adminGroupDao.getId());
            return Optional.empty();
        }

        adminGroupDao = adminGroupRepository.save(adminGroupDao);
        AdminGroup result = GroupAccessMapper.convertToAdminGroup(adminGroupDao, false);
        log.debug("The admin group with identification {} was saved", result.getIdentification());

        adminGroupChangeService.saveChange(adminGroupDao, storedAdminGroupDao.get(), editorIdentification);

        return Optional.of(result);
    }
}