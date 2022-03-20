package de.ma_vin.ape.users.service.context;

import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;
import de.ma_vin.ape.users.model.gen.domain.IIdentifiable;
import de.ma_vin.ape.users.service.AbstractRepositoryService;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Context object for saving at {@link AbstractRepositoryService}
 *
 * @param <T> Domain class of the object which should be stored
 * @param <S> Dao class of the object which should be stored
 */
@Data
public class SavingRepositoryServiceContext<T extends IIdentifiable, S extends IIdentifiableDao> {
    /**
     * Domain object which should be stored
     */
    private T domainObject;
    /**
     * identification of the user who is saving
     */
    private String editorIdentification;
    /**
     * Functional to access the name of t
     */
    private AbstractRepositoryService.Nameable<T> nameable;
    /**
     * Functional to convert the domain object to a dao one
     */
    private AbstractRepositoryService.DaoConverter<T, S> daoConverter;
    /**
     * Functional to convert the dao object to a domain one
     */
    private AbstractRepositoryService.DomainConverter<T, S> domainConverter;
    /**
     * Repository where to store the object
     */
    private JpaRepository<S, Long> repository;
    /**
     * Functional to take over values from persisted dao before saving
     */
    private AbstractRepositoryService.Adoption<S> adoption;

    private SavingRepositoryServiceContext() {
        nameable = domain -> "";
        adoption = (dao, storedDao) -> dao;
    }

    /**
     * Constructor
     *
     * @param domainObject         Domain object which should be stored
     * @param editorIdentification identification of the user who is saving
     * @param daoConverter         Functional to convert the domain object to a dao one
     * @param domainConverter      Functional to convert the dao object to a domain one
     * @param repository           Repository where to store the object
     */
    public SavingRepositoryServiceContext(T domainObject
            , String editorIdentification
            , AbstractRepositoryService.DaoConverter<T, S> daoConverter
            , AbstractRepositoryService.DomainConverter<T, S> domainConverter
            , JpaRepository<S, Long> repository) {

        this();
        this.domainObject = domainObject;
        this.editorIdentification = editorIdentification;
        this.daoConverter = daoConverter;
        this.domainConverter = domainConverter;
        this.repository = repository;
    }

    /**
     * Adds the config for the nameable
     *
     * @param nameable Functional to access the name of t
     * @return the modified context
     */
    public SavingRepositoryServiceContext<T, S> config(AbstractRepositoryService.Nameable<T> nameable) {
        setNameable(nameable);
        return this;
    }

    /**
     * Adds the config for the adoption
     *
     * @param adoption Functional to take over values from persisted dao before saving
     * @return the modified context
     */
    public SavingRepositoryServiceContext<T, S> config(AbstractRepositoryService.Adoption<S> adoption) {
        setAdoption(adoption);
        return this;
    }
}
