package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.gen.dao.resource.UserResourceDao;
import de.ma_vin.ape.users.model.gen.domain.resource.UserResource;
import de.ma_vin.ape.users.model.gen.mapper.ResourceAccessMapper;
import de.ma_vin.ape.users.persistence.UserResourceRepository;
import de.ma_vin.ape.users.service.context.RepositoryServiceContext;
import de.ma_vin.ape.users.service.history.AbstractChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Log4j2
@Data
public class UserResourceService extends AbstractRepositoryService<UserResourceDao> {
    public static final String USER_RESOURCE_LOG_PARAM = "user resource";

    @Autowired
    private UserResourceRepository userResourceRepository;

    @Override
    protected RepositoryServiceContext<UserResourceDao> createContext(String identification) {
        return new RepositoryServiceContext<>(identification, UserResource.class.getSimpleName(), UserResource.ID_PREFIX, userResourceRepository, UserResourceDao::new);
    }

    protected AbstractChangeService<UserResourceDao> getChangeService() {
        return new AbstractChangeService<>() {

            @SuppressWarnings("java:S1186")
            @Override
            public void saveCreation(UserResourceDao createdObject, String editorIdentification) {
            }

            @SuppressWarnings("java:S1186")
            @Override
            public void saveChange(UserResourceDao updatedObject, UserResourceDao storedObject, String editorIdentification) {
            }

            @SuppressWarnings("java:S1186")
            @Override
            public void delete(UserResourceDao deletedObject, String editorIdentification) {
            }
        };
    }

    /**
     * Deletes a users resource from repository
     *
     * @param userResource resource to delete
     */
    public void delete(UserResource userResource) {
        delete(ResourceAccessMapper.convertToUserResourceDao(userResource));
    }

    /**
     * Deletes a userResourceDao from repository
     *
     * @param userResourceDao User to delete
     */
    void delete(UserResourceDao userResourceDao) {
        log.debug(DELETE_BEGIN_LOG_MESSAGE, USER_RESOURCE_LOG_PARAM, userResourceDao.getIdentification(), userResourceDao.getId());

        userResourceRepository.delete(userResourceDao);

        log.debug(DELETE_END_LOG_MESSAGE, USER_RESOURCE_LOG_PARAM, userResourceDao.getIdentification(), userResourceDao.getId());
    }

    /**
     * Checks whether a users resource exists for a given identification or not
     *
     * @param identification the identification to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    public boolean userResourceExits(String identification) {
        return userResourceDaoExits(IdGenerator.generateId(identification, UserResource.ID_PREFIX));
    }

    /**
     * Checks whether a users resource exists for a given id or not
     *
     * @param id id to check
     * @return {@code true} if an object exists, otherwise {@code false}
     */
    private boolean userResourceDaoExits(Long id) {
        return userResourceRepository.existsById(id);
    }

    /**
     * Searches for a users resource
     *
     * @param identification Id of the users resource which is searched for
     * @return search result
     */
    public Optional<UserResource> findUserResource(String identification) {
        return find(identification, ResourceAccessMapper::convertToUserResource);
    }

    /**
     * Stores a users resource
     *
     * @param userResource users resource which should be stored
     * @return Stored users resource with additional generated ids, if missing before.
     * <br>
     * In case of not existing users resource for given identification, the result will be {@link Optional#empty()}
     */
    public Optional<UserResource> save(UserResource userResource) {
        Optional<UserResourceDao> result = save(ResourceAccessMapper.convertToUserResourceDao(userResource));
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ResourceAccessMapper.convertToUserResource(result.get()));
    }

    /**
     * Stores a users resource
     *
     * @param userResourceDao users resource which should be stored
     * @return Stored users resource with additional generated ids, if missing before.
     * <br>
     * In case of not existing users resource for given identification, the result will be {@link Optional#empty()}
     */
    Optional<UserResourceDao> save(UserResourceDao userResourceDao) {
        if (userResourceDao.getIdentification() == null) {
            log.debug("There is not any identification, so users resource will be stored the first time");
            userResourceDao = userResourceRepository.save(userResourceDao);
            log.debug("The resource was stored with id {} and corresponding identification {}"
                    , userResourceDao.getId(), userResourceDao.getIdentification());

            return Optional.of(userResourceDao);
        }

        Optional<UserResourceDao> storedUserResourceDao = userResourceRepository.findById(userResourceDao.getId());
        if (storedUserResourceDao.isEmpty()) {
            log.debug("Users resource with identification {} and id {} does not exists and could not be saved", userResourceDao.getIdentification(), userResourceDao.getId());
            return Optional.empty();
        }

        userResourceDao = userResourceRepository.save(userResourceDao);
        log.debug("Users resource with identification {} was saved", userResourceDao.getIdentification());

        return Optional.of(userResourceDao);
    }
}
