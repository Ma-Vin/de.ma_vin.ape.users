package de.ma_vin.ape.users.service.context;

import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;
import de.ma_vin.ape.users.model.gen.domain.IIdentifiable;
import de.ma_vin.ape.users.service.AbstractRepositoryService;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Context object for saving at {@link AbstractRepositoryService} with properties for the parent
 *
 * @param <T> Domain class of the object which should be stored
 * @param <S> Dao class of the object which should be stored
 */
@Data
public class SavingWithParentRepositoryServiceContext<T extends IIdentifiable, S extends IIdentifiableDao, P extends IIdentifiableDao> extends SavingRepositoryServiceContext<T, S> {
    /**
     * identification of the parent domain object
     */
    private String parentIdentification;
    /**
     * Functional to create an empty parent Dao
     */
    private AbstractRepositoryService.DaoCreator<P> parentDaoCreator;
    /**
     * Functional to convert the domain object to a dao one with parent
     */
    private AbstractRepositoryService.DaoWithParentConverter<T, S, P> daoWithParentConverter;

    /**
     * Constructor
     *
     * @param domainObject         Domain object which should be stored
     * @param editorIdentification identification of the user who is saving
     * @param daoConverter         Functional to convert the domain object to a dao one
     * @param domainConverter      Functional to convert the dao object to a domain one
     * @param repository           Repository where to store the object
     */
    public SavingWithParentRepositoryServiceContext(T domainObject
            , String editorIdentification
            , AbstractRepositoryService.DaoConverter<T, S> daoConverter
            , AbstractRepositoryService.DomainConverter<T, S> domainConverter
            , JpaRepository<S, Long> repository) {

        super(domainObject, editorIdentification, daoConverter, domainConverter, repository);
    }

    /**
     * Adds the config for the parent
     *
     * @param parentIdentification   identification of the parent domain object
     * @param parentDaoCreator       Functional to create an empty parent Dao
     * @param daoWithParentConverter Functional to convert the domain object to a dao one with parent
     * @return the modified context
     */
    public SavingWithParentRepositoryServiceContext<T, S, P> config(String parentIdentification, AbstractRepositoryService.DaoCreator<P> parentDaoCreator
            , AbstractRepositoryService.DaoWithParentConverter<T, S, P> daoWithParentConverter) {

        setParentIdentification(parentIdentification);
        setParentDaoCreator(parentDaoCreator);
        setDaoWithParentConverter(daoWithParentConverter);
        return this;
    }

    @Override
    public SavingWithParentRepositoryServiceContext<T, S, P> config(AbstractRepositoryService.Adoption<S> adoption) {
        return (SavingWithParentRepositoryServiceContext<T, S, P>) super.config(adoption);
    }

    @Override
    public SavingWithParentRepositoryServiceContext<T, S, P> config(AbstractRepositoryService.Nameable<T> nameable) {
        return (SavingWithParentRepositoryServiceContext<T, S, P>) super.config(nameable);
    }
}
