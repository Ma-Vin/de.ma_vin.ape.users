package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;
import de.ma_vin.ape.users.model.gen.domain.IIdentifiable;
import de.ma_vin.ape.users.service.history.AbstractChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Optional;

@Log4j2
public abstract class AbstractRepositoryService<S extends IIdentifiableDao> {

    public static final String GET_PARENT_ID_MISSING_CHILD_ID_LOG_ERROR = "The identification of the parent {} could not be determined without identification of the {}";
    public static final String GET_PARENT_ID_NOT_FOUND_LOG_ERROR = "The identification of the parent {} could not be determined";

    public static final String DELETE_BEGIN_LOG_MESSAGE = "Delete {} with identification \"{}\" and id \"{}\"\"";
    public static final String DELETE_END_LOG_MESSAGE = "{} with identification \"{}\" and id \"{}\" was deleted";
    public static final String DELETE_SUB_ENTITY_LOG_MESSAGE = "{} {} are also to delete by deletion of {} with identification \"{}\" and id \"{}\"";

    public static final String SEARCH_START_LOG_MESSAGE = "Search for {} at {} with identification \"{}\"";
    public static final String SEARCH_FOR_START_LOG_MESSAGE = "Search {} for {} with identification \"{}\"";
    public static final String SEARCH_START_PAGE_LOG_MESSAGE = "Search for {} page {} with size {} at {} with identification \"{}\"";
    public static final String SEARCH_FOR_START_PAGE_LOG_MESSAGE = "Search {} page {} with size {} for {} with identification \"{}\"";
    public static final String SEARCH_RESULT_LOG_MESSAGE = "{} {} are found at {} with identification \"{}\"";
    public static final String SEARCH_FOR_RESULT_LOG_MESSAGE = "{} {} are found for {} with identification \"{}\"";
    public static final String SEARCH_RESULT_PAGE_LOG_MESSAGE = "{} {} are found at {} with identification \"{}\" and page {} with size {}";
    public static final String SEARCH_FOR_RESULT_PAGE_LOG_MESSAGE = "{} {} are found for {} with identification \"{}\" and page {} with size {}";

    public static final String COUNT_START_LOG_MESSAGE = "Count {} at {} with identification \"{}\"";
    public static final String COUNT_FOR_START_LOG_MESSAGE = "Count {} for {} with identification \"{}\"";
    public static final String COUNT_RESULT_LOG_MESSAGE = "{} {} are count at {} with identification \"{}\"";
    public static final String COUNT_FOR_RESULT_LOG_MESSAGE = "{} {} are count for {} with identification \"{}\"";


    /**
     * @return the change service for the class <code>S</code>
     */
    protected AbstractChangeService<S> getChangeService() {
        return new AbstractChangeService<>() {

            @SuppressWarnings("java:S1186")
            @Override
            public void saveCreation(S createdObject, String editorIdentification) {
            }

            @SuppressWarnings("java:S1186")
            @Override
            public void saveChange(S updatedObject, S storedObject, String editorIdentification) {
            }

            @SuppressWarnings("java:S1186")
            @Override
            public void delete(S deletedObject, String editorIdentification) {
            }
        };
    }

    /**
     * Adds a child to a parent if its not a direct connection
     *
     * @param parentIdentification    identification of the parent
     * @param childIdentification     identification of the child
     * @param parentClassName         simple name of parent class
     * @param childClassName          simple name of child class
     * @param parentPrefix            id prefix of parent
     * @param childPrefix             id prefix of child
     * @param parentRepository        repository of the parent
     * @param childRepository         repository of the child
     * @param parentToChildRepository repository of the connection between parent and child
     * @param connectionCreator       Functional to create the connection. Setting values included
     * @param <P>                     The parent
     * @param <T>                     The connection
     * @param <I>                     The Id of the connection at repository
     * @return {@code true} if the child was added to the parent. Otherwise {@code false}
     */
    protected <P extends IIdentifiableDao, T, I extends Serializable>
    boolean add(String parentIdentification, String childIdentification, String parentClassName, String childClassName
            , String parentPrefix, String childPrefix
            , JpaRepository<P, Long> parentRepository, JpaRepository<S, Long> childRepository, JpaRepository<T, I> parentToChildRepository
            , ConnectionCreator<P, S, T> connectionCreator) {

        log.debug("Add a {} with identification \"{}\" to {} with identification \"{}\""
                , childClassName, childIdentification, parentClassName, parentIdentification);

        Optional<P> parentDao = parentRepository.findById(IdGenerator.generateId(parentIdentification, parentPrefix));
        if (parentDao.isEmpty()) {
            log.error("There is not any {} with identification \"{}\" where to add {} with identification \"{}\""
                    , parentClassName, parentIdentification, childClassName, childIdentification);
            return false;
        }

        Optional<S> childDao = childRepository.findById(IdGenerator.generateId(childIdentification, childPrefix));
        if (childDao.isEmpty()) {
            log.error("There is not any {} with identification \"{}\" which could be add {} with identification \"{}\""
                    , childClassName, childIdentification, parentClassName, parentIdentification);
            return false;
        }

        T connection = connectionCreator.create(parentDao.get(), childDao.get());

        parentToChildRepository.save(connection);
        log.debug("{} with identification \"{}\" was added to {} with identification \"{}\""
                , childClassName, childIdentification, parentClassName, parentIdentification);
        return true;
    }

    /**
     * Searches for a domain object
     *
     * @param identification  Identification of the domain object which is searched for
     * @param idPrefix        prefix to generate ID
     * @param domainClassName Simple name of the domain class
     * @param domainConverter Functional to create the domain from the dao object
     * @param repository      repository where to search at
     * @param <T>             Domain class of the object which should be searched for
     * @return search result
     */
    protected <T extends IIdentifiable> Optional<T> find(String identification, String idPrefix
            , String domainClassName, DomainConverter<T, S> domainConverter, JpaRepository<S, Long> repository) {

        Optional<S> resultDao = find(identification, idPrefix, domainClassName, repository);
        if (resultDao.isPresent()) {
            return Optional.of(domainConverter.convert(resultDao.get()));
        }
        return Optional.empty();
    }

    /**
     * Searches for a dao object
     *
     * @param identification Identification of the domain object which is searched for
     * @param idPrefix       prefix to generate ID
     * @param className      Simple name of the domain class
     * @param repository     repository where to search at
     * @return search result
     */
    protected Optional<S> find(String identification, String idPrefix, String className
            , JpaRepository<S, Long> repository) {

        Long id = IdGenerator.generateId(identification, idPrefix);
        log.debug("search for {} with identification {} and id {}", className, identification, id);
        Optional<S> daoObject = repository.findById(id);
        if (daoObject.isPresent()) {
            log.debug("{} with identification {} and id {} was found", className, identification, id);
            return Optional.of(daoObject.get());
        }
        log.debug("{} with identification {} and id {} was not found", className, identification, id);
        return Optional.empty();
    }

    /**
     * Removes a child from a parent if its not a direct connection
     *
     * @param parentIdentification Identification of the parent
     * @param childIdentification  Identification of the state
     * @param parentClassName      simple name of parent class
     * @param childClassName       simple name of child class
     * @param parentPrefix         id prefix of parent
     * @param childPrefix          id prefix of child
     * @param parentCreator        Functional to create an empty parent dao
     * @param childCreator         Functional to create an empty child dao
     * @param deleter              Function to delete the connection with parent and child dao
     * @param <P>                  The parent
     * @return {@code true} if the child was removed from the parent. Otherwise {@code false}
     */
    protected <P extends IIdentifiableDao>
    boolean remove(String parentIdentification, String childIdentification, String parentClassName, String childClassName
            , String parentPrefix, String childPrefix, DaoCreator<P> parentCreator, DaoCreator<S> childCreator
            , ConnectionDeleter<P, S> deleter) {

        Long parentId = IdGenerator.generateId(parentIdentification, parentPrefix);
        Long childId = IdGenerator.generateId(childIdentification, childPrefix);
        log.debug("Remove {} with identification \"{}\" and id {} from {} with identification \"{}\" and id {}"
                , childClassName, childIdentification, childId, parentClassName, parentIdentification, parentId);

        P parentDao = parentCreator.create();
        parentDao.setId(parentId);
        S childDao = childCreator.create();
        childDao.setId(childId);

        Long numDeleted = deleter.delete(parentDao, childDao);
        switch (numDeleted.intValue()) {
            case 0:
                log.warn("The {} with identification \"{}\" and id {} was not removed from {} with identification \"{}\" and id {}"
                        , childClassName, childIdentification, childId, parentClassName, parentIdentification, parentId);
                return false;
            case 1:
                log.debug("The {} with identification \"{}\" and id {} was removed from {} with identification \"{}\" and id {}"
                        , childClassName, childIdentification, childId, parentClassName, parentIdentification, parentId);
                return true;
            default:
                log.error("{} {} with identification \"{}\" and id {} were removed from {} with identification \"{}\" and id {}"
                        , numDeleted, childClassName, childIdentification, childId, parentClassName, parentIdentification, parentId);
                return false;
        }
    }

    /**
     * @param domainObject         Domain object which should be stored
     * @param parentIdentification identification of the parent domain object
     * @param editorIdentification identification of the user who is saving
     * @param parentDaoCreator     Functional to create an empty parent Dao
     * @param daoConverter         Functional to to convert the domain object to a dao one
     * @param domainConverter      Functional to convert the dao object to a domain one
     * @param repository           Repository where to store the object
     * @param <T>                  Domain class of the object which should be stored
     * @param <P>                  Dao class of the parent
     * @return Stored domain object with additional generated ids, if missing before.
     * <br>
     * In case of not existing domain object for given identification, the result will be {@link Optional#empty()}
     */
    protected <T extends IIdentifiable, P extends IIdentifiableDao> Optional<T> save(T domainObject
            , String parentIdentification
            , String editorIdentification
            , DaoCreator<P> parentDaoCreator
            , DaoConverter<T, S, P> daoConverter
            , DomainConverter<T, S> domainConverter
            , JpaRepository<S, Long> repository) {
        return save(domainObject
                , parentIdentification
                , editorIdentification
                , d -> ""
                , parentDaoCreator
                , daoConverter
                , domainConverter
                , repository
                , (dao, storedDao) -> dao);
    }

    /**
     * @param domainObject         Domain object which should be stored
     * @param parentIdentification identification of the parent domain object
     * @param editorIdentification identification of the user who is saving
     * @param parentDaoCreator     Functional to create an empty parent Dao
     * @param daoConverter         Functional to to convert the domain object to a dao one
     * @param domainConverter      Functional to convert the dao object to a domain one
     * @param repository           Repository where to store the object
     * @param adoption             Functional to take over values from persisted dao before saving
     * @param <T>                  Domain class of the object which should be stored
     * @param <P>                  Dao class of the parent
     * @return Stored domain object with additional generated ids, if missing before.
     * <br>
     * In case of not existing domain object for given identification, the result will be {@link Optional#empty()}
     */
    protected <T extends IIdentifiable, P extends IIdentifiableDao> Optional<T> save(T domainObject
            , String parentIdentification
            , String editorIdentification
            , DaoCreator<P> parentDaoCreator
            , DaoConverter<T, S, P> daoConverter
            , DomainConverter<T, S> domainConverter
            , JpaRepository<S, Long> repository
            , Adoption<S> adoption) {
        return save(domainObject
                , parentIdentification
                , editorIdentification
                , d -> ""
                , parentDaoCreator
                , daoConverter
                , domainConverter
                , repository
                , adoption);
    }

    /**
     * @param domainObject         Domain object which should be stored
     * @param parentIdentification identification of the parent domain object
     * @param editorIdentification identification of the user who is saving
     * @param nameable             Functional to access the name of t
     * @param parentDaoCreator     Functional to create an empty parent Dao
     * @param daoConverter         Functional to to convert the domain object to a dao one
     * @param domainConverter      Functional to convert the dao object to a domain one
     * @param repository           Repository where to store the object
     * @param <T>                  Domain class of the object which should be stored
     * @param <P>                  Dao class of the parent
     * @return Stored domain object with additional generated ids, if missing before.
     * <br>
     * In case of not existing domain object for given identification, the result will be {@link Optional#empty()}
     */
    protected <T extends IIdentifiable, P extends IIdentifiableDao> Optional<T> save(T domainObject
            , String parentIdentification
            , String editorIdentification
            , Nameable<T> nameable
            , DaoCreator<P> parentDaoCreator
            , DaoConverter<T, S, P> daoConverter
            , DomainConverter<T, S> domainConverter
            , JpaRepository<S, Long> repository) {
        return save(domainObject
                , parentIdentification
                , editorIdentification
                , nameable
                , parentDaoCreator
                , daoConverter
                , domainConverter
                , repository
                , (dao, storedDao) -> dao);
    }

    /**
     * @param domainObject         Domain object which should be stored
     * @param parentIdentification identification of the parent domain object
     * @param editorIdentification identification of the user who is saving
     * @param nameable             Functional to access the name of t
     * @param parentDaoCreator     Functional to create an empty parent Dao
     * @param daoConverter         Functional to to convert the domain object to a dao one
     * @param domainConverter      Functional to convert the dao object to a domain one
     * @param repository           Repository where to store the object
     * @param adoption             Functional to take over values from persisted dao before saving
     * @param <T>                  Domain class of the object which should be stored
     * @param <P>                  Dao class of the parent
     * @return Stored domain object with additional generated ids, if missing before.
     * <br>
     * In case of not existing domain object for given identification, the result will be {@link Optional#empty()}
     */
    protected <T extends IIdentifiable, P extends IIdentifiableDao> Optional<T> save(T domainObject
            , String parentIdentification
            , String editorIdentification
            , Nameable<T> nameable
            , DaoCreator<P> parentDaoCreator
            , DaoConverter<T, S, P> daoConverter
            , DomainConverter<T, S> domainConverter
            , JpaRepository<S, Long> repository
            , Adoption<S> adoption) {
        if (parentIdentification == null) {
            log.error("There is no identification of the parent");
            return Optional.empty();
        }

        String simpleClassName = domainObject.getClass().getSimpleName();

        P parent = parentDaoCreator.create();
        parent.setIdentification(parentIdentification);

        S daoObject = daoConverter.convert(domainObject, parent);
        T result;

        if (domainObject.getIdentification() == null) {
            log.debug("There is not any identification, so the {} with name {} will be stored the first time", simpleClassName, nameable.getName(domainObject));
            daoObject = repository.save(daoObject);
            result = domainConverter.convert(daoObject);
            log.debug("The {} with name {} was stored with id {} and corresponding identification {}"
                    , simpleClassName, nameable.getName(domainObject), daoObject.getId(), result.getIdentification());

            getChangeService().saveCreation(daoObject, editorIdentification != null ? editorIdentification : daoObject.getIdentification());

            return Optional.of(result);
        }

        Optional<S> storedDao = repository.findById(daoObject.getId());
        if (storedDao.isEmpty()) {
            log.debug("The {} with identification {} and id {} does not exists and could not be saved", simpleClassName, parentIdentification, daoObject.getId());
            return Optional.empty();
        }

        daoObject = adoption.takeOver(daoObject, storedDao.get());

        daoObject = repository.save(daoObject);
        getChangeService().saveChange(daoObject, storedDao.get(), editorIdentification);

        result = domainConverter.convert(daoObject);
        log.debug("The {} with identification {} was saved", simpleClassName, result.getIdentification());

        return Optional.of(result);
    }

    @FunctionalInterface
    protected interface Nameable<T extends IIdentifiable> {
        String getName(T t);
    }

    @FunctionalInterface
    protected interface Adoption<T extends IIdentifiableDao> {
        T takeOver(T toPersist, T persisted);
    }

    @FunctionalInterface
    protected interface DaoCreator<T extends IIdentifiableDao> {
        T create();
    }

    @FunctionalInterface
    protected interface ConnectionCreator<P extends IIdentifiableDao, S extends IIdentifiableDao, T> {
        T create(P parent, S child);
    }

    @FunctionalInterface
    protected interface ConnectionDeleter<P extends IIdentifiableDao, S extends IIdentifiableDao> {
        Long delete(P parent, S child);
    }

    @FunctionalInterface
    protected interface DaoConverter<T extends IIdentifiable, S extends IIdentifiableDao, P extends IIdentifiableDao> {
        S convert(T toConvert, P parent);
    }

    @FunctionalInterface
    protected interface DomainConverter<T extends IIdentifiable, S extends IIdentifiableDao> {
        T convert(S toConvert);
    }
}
