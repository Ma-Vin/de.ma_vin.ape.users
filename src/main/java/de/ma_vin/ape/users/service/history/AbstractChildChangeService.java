package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;

/**
 * @param <T> the class whose changes are stored
 * @param <S> the first type of parent
 * @param <P> the second type of parent
 */
public abstract class AbstractChildChangeService<T extends IIdentifiableDao, S extends IIdentifiableDao, P extends IIdentifiableDao> extends AbstractChangeService<T> {
    /**
     * Store an addition event of a new object
     *
     * @param toAddObject          the object which is to add to the other one
     * @param parentObject         the parent object where to add
     * @param editorIdentification the identification of the modifier
     */
    public abstract void addToParentFirstType(T toAddObject, S parentObject, String editorIdentification);

    /**
     * Store an addition event of a new object
     *
     * @param toAddObject          the object which is to add to the other one
     * @param parentObject         the parent object where to add
     * @param editorIdentification the identification of the modifier
     */
    public abstract void addToParentSecondType(T toAddObject, P parentObject, String editorIdentification);

    /**
     * Store a remove event of a new object
     *
     * @param toRemoveObject       the object which is to remove from the other one
     * @param parentObject         the parent object where to remove from
     * @param editorIdentification the identification of the modifier
     */
    public abstract void removeFromParentFirstType(T toRemoveObject, S parentObject, String editorIdentification);

    /**
     * Store a remove event of a new object
     *
     * @param toRemoveObject       the object which is to remove from the other one
     * @param parentObject         the parent object where to remove from
     * @param editorIdentification the identification of the modifier
     */
    public abstract void removeFromParentSecondType(T toRemoveObject, P parentObject, String editorIdentification);
}
