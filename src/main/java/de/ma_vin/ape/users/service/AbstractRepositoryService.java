package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;
import de.ma_vin.ape.users.model.gen.domain.IIdentifiable;
import de.ma_vin.ape.users.service.context.RepositoryServiceContext;
import de.ma_vin.ape.users.service.context.SavingWithParentRepositoryServiceContext;
import de.ma_vin.ape.users.service.history.AbstractChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.extern.log4j.Log4j2;

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
    protected abstract AbstractChangeService<S> getChangeService();

    /**
     * Creates the context for the child object
     *
     * @param identification the identification of the child object
     * @return the created context
     */
    protected abstract RepositoryServiceContext<S> createContext(String identification);

    /**
     * Searches for a domain object
     *
     * @param identification  Identification of the domain object which is searched for
     * @param domainConverter Functional to create the domain from the dao object
     * @param <T>             Domain class of the object which should be searched for
     * @return search result
     */
    protected <T extends IIdentifiable> Optional<T> find(String identification, DomainConverter<T, S> domainConverter) {

        Optional<S> resultDao = find(identification);
        if (resultDao.isPresent()) {
            return Optional.of(domainConverter.convert(resultDao.get()));
        }
        return Optional.empty();
    }

    /**
     * Searches for a dao object
     *
     * @param identification identification of the dao object
     * @return search result
     */
    protected Optional<S> find(String identification) {
        return find(createContext(identification));
    }

    /**
     * Searches for a dao object
     *
     * @param context context of the dao object
     * @return search result
     */
    protected Optional<S> find(RepositoryServiceContext<S> context) {

        Long id = IdGenerator.generateId(context.getIdentification(), context.getPrefix());
        log.debug("search for {} with identification {} and id {}", context.getClassName(), context.getIdentification(), id);
        Optional<S> daoObject = context.getRepository().findById(id);
        if (daoObject.isPresent()) {
            log.debug("{} with identification {} and id {} was found", context.getClassName(), context.getIdentification(), id);
            return Optional.of(daoObject.get());
        }
        log.debug("{} with identification {} and id {} was not found", context.getClassName(), context.getIdentification(), id);
        return Optional.empty();
    }

    /**
     * @param context context of the saving
     * @param <T>     domain class of the object which should be stored
     * @param <P>     Dao class of the parent
     * @return Stored domain object with additional generated ids, if missing before.
     * <br>
     * In case of not existing domain object for given identification, the result will be {@link Optional#empty()}
     */
    protected <T extends IIdentifiable, P extends IIdentifiableDao> Optional<T> save(SavingWithParentRepositoryServiceContext<T, S, P> context) {
        if (context.getParentIdentification() == null) {
            log.error("There is no identification of the parent");
            return Optional.empty();
        }

        String simpleClassName = context.getDomainObject().getClass().getSimpleName();

        P parent = context.getParentDaoCreator().create();
        parent.setIdentification(context.getParentIdentification());

        S daoObject = context.getDaoWithParentConverter().convert(context.getDomainObject(), parent);
        T result;

        if (context.getDomainObject().getIdentification() == null) {
            log.debug("There is not any identification, so the {} with name {} will be stored the first time", simpleClassName
                    , context.getNameable().getName(context.getDomainObject()));
            daoObject = context.getRepository().save(daoObject);
            result = context.getDomainConverter().convert(daoObject);
            log.debug("The {} with name {} was stored with id {} and corresponding identification {}"
                    , simpleClassName, context.getNameable().getName(context.getDomainObject()), daoObject.getId(), result.getIdentification());

            getChangeService().saveCreation(daoObject, context.getEditorIdentification() != null ? context.getEditorIdentification() : daoObject.getIdentification());

            return Optional.of(result);
        }

        Optional<S> storedDao = context.getRepository().findById(daoObject.getId());
        if (storedDao.isEmpty()) {
            log.debug("The {} with identification {} and id {} does not exists and could not be saved", simpleClassName, context.getParentIdentification(), daoObject.getId());
            return Optional.empty();
        }

        daoObject = context.getAdoption().takeOver(daoObject, storedDao.get());

        daoObject = context.getRepository().save(daoObject);
        getChangeService().saveChange(daoObject, storedDao.get(), context.getEditorIdentification());

        result = context.getDomainConverter().convert(daoObject);
        log.debug("The {} with identification {} was saved", simpleClassName, result.getIdentification());

        return Optional.of(result);
    }

    @FunctionalInterface
    public interface Nameable<T extends IIdentifiable> {
        String getName(T t);
    }

    @FunctionalInterface
    public interface Adoption<T extends IIdentifiableDao> {
        T takeOver(T toPersist, T persisted);
    }

    @FunctionalInterface
    public interface DaoCreator<T extends IIdentifiableDao> {
        T create();
    }

    @FunctionalInterface
    public interface DaoWithParentConverter<T extends IIdentifiable, S extends IIdentifiableDao, P extends IIdentifiableDao> {
        S convert(T toConvert, P parent);
    }

    @FunctionalInterface
    public interface DaoConverter<T extends IIdentifiable, S extends IIdentifiableDao> {
        S convert(T toConvert);
    }

    @FunctionalInterface
    public interface DomainConverter<T extends IIdentifiable, S extends IIdentifiableDao> {
        T convert(S toConvert);
    }
}
