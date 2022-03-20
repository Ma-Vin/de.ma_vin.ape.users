package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;
import de.ma_vin.ape.users.service.context.RepositoryServiceContext;
import de.ma_vin.ape.users.service.history.AbstractChildChangeService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Optional;

/**
 * @param <C> the class which is mainly handled by this class
 * @param <P> the first type of parent
 * @param <O> the second type of parent
 */
@Log4j2
public abstract class AbstractChildRepositoryService<C extends IIdentifiableDao, P extends IIdentifiableDao, O extends IIdentifiableDao> extends AbstractRepositoryService<C> {

    @Override
    protected abstract AbstractChildChangeService<C, P, O> getChangeService();

    /**
     * Creates the context for the parent object
     *
     * @param identification the identification of the parent object
     * @return the created context
     */
    protected abstract RepositoryServiceContext<P> createParentFirstTypeContext(String identification);

    /**
     * Creates the context for the parent object
     *
     * @param identification the identification of the parent object
     * @return the created context
     */
    protected abstract RepositoryServiceContext<O> createParentSecondTypeContext(String identification);


    /**
     * Adds a child to a parent if it is not a direct connection. And uses the first type of parent
     *
     * @param parentIdentification    identification of the parent
     * @param childIdentification     identification of the child
     * @param parentToChildRepository repository of the connection between parent and child
     * @param connectionCreator       Functional to create the connection. Setting values included
     * @param connectionChangeHandler Functional which handles the change history
     * @param editorIdentification    The identification of the user who is adding
     * @param <T>                     The connection
     * @param <I>                     The id of the connection at repository
     * @return {@code true} if the child was added to the parent. Otherwise {@code false}
     */
    protected <T, I extends Serializable>
    boolean add(String parentIdentification, String childIdentification, JpaRepository<T, I> parentToChildRepository
            , ConnectionCreator<P, C, T> connectionCreator, ConnectionChangeHandler<P, C> connectionChangeHandler, String editorIdentification) {
        return add(createParentFirstTypeContext(parentIdentification), createContext(childIdentification), parentToChildRepository
                , connectionCreator, connectionChangeHandler, editorIdentification);
    }

    /**
     * Adds a child to a parent if it is not a direct connection
     *
     * @param parentContext           context of the parent
     * @param childContext            context of the child
     * @param parentToChildRepository repository of the connection between parent and child
     * @param connectionCreator       Functional to create the connection. Setting values included
     * @param connectionChangeHandler Functional which handles the change history
     * @param editorIdentification    The identification of the user who is adding
     * @param <S>                     The parent
     * @param <T>                     The connection
     * @param <I>                     The id of the connection at repository
     * @return {@code true} if the child was added to the parent. Otherwise {@code false}
     */
    protected <S extends IIdentifiableDao, T, I extends Serializable>
    boolean add(RepositoryServiceContext<S> parentContext, RepositoryServiceContext<C> childContext, JpaRepository<T, I> parentToChildRepository
            , ConnectionCreator<S, C, T> connectionCreator, ConnectionChangeHandler<S, C> connectionChangeHandler, String editorIdentification) {

        log.debug("Add a {} with identification \"{}\" to {} with identification \"{}\""
                , childContext.getClassName(), childContext.getIdentification(), parentContext.getClassName(), parentContext.getIdentification());

        Optional<S> parentDao = parentContext.getRepository().findById(IdGenerator.generateId(parentContext.getIdentification(), parentContext.getPrefix()));
        if (parentDao.isEmpty()) {
            log.error("There is not any {} with identification \"{}\" where to add {} with identification \"{}\""
                    , parentContext.getClassName(), parentContext.getIdentification(), childContext.getClassName(), childContext.getIdentification());
            return false;
        }

        Optional<C> childDao = childContext.getRepository().findById(IdGenerator.generateId(childContext.getIdentification(), childContext.getPrefix()));
        if (childDao.isEmpty()) {
            log.error("There is not any {} with identification \"{}\" which could be add {} with identification \"{}\""
                    , childContext.getClassName(), childContext.getIdentification(), parentContext.getClassName(), parentContext.getIdentification());
            return false;
        }

        T connection = connectionCreator.create(parentDao.get(), childDao.get());

        parentToChildRepository.save(connection);
        log.debug("{} with identification \"{}\" was added to {} with identification \"{}\""
                , childContext.getClassName(), childContext.getIdentification(), parentContext.getClassName(), parentContext.getIdentification());

        connectionChangeHandler.handleChange(childDao.get(), parentDao.get(), editorIdentification);
        return true;
    }

    /**
     * Removes a child from a parent if it is not a direct connection
     *
     * @param parentIdentification    identification of the parent
     * @param childIdentification     identification of the child
     * @param deleter                 Function to delete the connection with parent and child dao
     * @param connectionChangeHandler Functional which handles the change history
     * @param editorIdentification    The identification of the user who is removing
     * @return {@code true} if the child was removed from the parent. Otherwise {@code false}
     */
    protected boolean remove(String parentIdentification, String childIdentification, ConnectionDeleter<P, C> deleter
            , ConnectionChangeHandler<P, C> connectionChangeHandler, String editorIdentification) {
        return remove(createParentFirstTypeContext(parentIdentification), createContext(childIdentification), deleter
                , connectionChangeHandler, editorIdentification);
    }

    /**
     * Removes a child from a parent if it is not a direct connection
     *
     * @param parentContext           context of the parent
     * @param childContext            context of the child
     * @param deleter                 Function to delete the connection with parent and child dao
     * @param connectionChangeHandler Functional which handles the change history
     * @param editorIdentification    The identification of the user who is removing
     * @return {@code true} if the child was removed from the parent. Otherwise {@code false}
     */
    protected <S extends IIdentifiableDao>
    boolean remove(RepositoryServiceContext<S> parentContext, RepositoryServiceContext<C> childContext, ConnectionDeleter<S, C> deleter
            , ConnectionChangeHandler<S, C> connectionChangeHandler, String editorIdentification) {

        Long parentId = IdGenerator.generateId(parentContext.getIdentification(), parentContext.getPrefix());
        Long childId = IdGenerator.generateId(childContext.getIdentification(), childContext.getPrefix());
        log.debug("Remove {} with identification \"{}\" and id {} from {} with identification \"{}\" and id {}"
                , childContext.getClassName(), childContext.getIdentification(), childId, parentContext.getClassName(), parentContext.getIdentification(), parentId);

        S parentDao = parentContext.getCreator().create();
        parentDao.setId(parentId);
        C childDao = childContext.getCreator().create();
        childDao.setId(childId);

        Long numDeleted = deleter.delete(parentDao, childDao);
        switch (numDeleted.intValue()) {
            case 0 -> {
                log.warn("The {} with identification \"{}\" and id {} was not removed from {} with identification \"{}\" and id {}"
                        , childContext.getClassName(), childContext.getIdentification(), childId, parentContext.getClassName(), parentContext.getIdentification(), parentId);
                return false;
            }
            case 1 -> {
                connectionChangeHandler.handleChange(childDao, parentDao, editorIdentification);
                log.debug("The {} with identification \"{}\" and id {} was removed from {} with identification \"{}\" and id {}"
                        , childContext.getClassName(), childContext.getIdentification(), childId, parentContext.getClassName(), parentContext.getIdentification(), parentId);
                return true;
            }
            default -> {
                log.error("{} {} with identification \"{}\" and id {} were removed from {} with identification \"{}\" and id {}"
                        , numDeleted, childContext.getClassName(), childContext.getIdentification(), childId, parentContext.getClassName(), parentContext.getIdentification(), parentId);
                return false;
            }
        }
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
    protected interface ConnectionChangeHandler<P extends IIdentifiableDao, S extends IIdentifiableDao> {
        void handleChange(S child, P parent, String editorIdentification);
    }
}
