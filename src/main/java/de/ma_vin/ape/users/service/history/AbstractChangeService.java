package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.enums.ChangeType;
import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;
import de.ma_vin.ape.users.model.gen.dao.history.AbstractChangeDao;
import de.ma_vin.ape.users.model.gen.domain.history.AbstractChange;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @param <T> dao class whose changes are handled
 * @param <S> domain change class
 */
@Log4j2
public abstract class AbstractChangeService<T extends IIdentifiableDao, S extends AbstractChange> {
    public static final String CHANGE_TEMPLATE = "%s: \"%s\" -> \"%s\"";

    /**
     * Store a creation event of a new object
     *
     * @param createdObject        the object which was created
     * @param editorIdentification the identification of the creator
     */
    public abstract void saveCreation(T createdObject, String editorIdentification);

    /**
     * Stores a modification of an existing object
     *
     * @param updatedObject        the object after changes
     * @param storedObject         the object before changes
     * @param editorIdentification the identification of the modifier
     */
    public abstract void saveChange(T updatedObject, T storedObject, String editorIdentification);

    /**
     * Stores a deletion of an existing object and removes references to it
     *
     * @param deletedObject        the object to delete
     * @param editorIdentification the identification of the deleter
     */
    public abstract void delete(T deletedObject, String editorIdentification);

    /**
     * Loads changes for a given identification
     *
     * @param identification the identification of the object whose changes are to load
     * @return the list of changes
     */
    public abstract List<S> loadChanges(String identification);

    /**
     * Sets the action and {@link ChangeType} of an existing object at an {@link AbstractChangeDao}
     *
     * @param updatedObject the object after changes
     * @param storedObject  the object before changes
     * @param change        the change object where to set differences and change type
     */
    protected void determineChanges(T updatedObject, T storedObject, AbstractChangeDao change) {
        change.setChangeType(ChangeType.MODIFY);
        change.setAction(determineDiffAsText(updatedObject, storedObject));
        if (change.getAction().isEmpty()) {
            log.warn("There was tried to store a base group {} where no diff could be determined", updatedObject.getIdentification());
            change.setChangeType(ChangeType.UNKNOWN);
            change.setAction(null);
        }
    }

    /**
     * Determines the differences between the actual and the stored object as text
     *
     * @param updatedObject the object after changes
     * @param storedObject  the object before changes
     * @return A string with the determined changes
     */
    protected String determineDiffAsText(T updatedObject, T storedObject) {
        StringBuilder sb = new StringBuilder();
        determineDiff(updatedObject, storedObject).forEach(s -> {
            sb.append(s);
            sb.append(", ");
        });
        String result = sb.toString();
        return result.length() > 0 ? result.substring(0, result.length() - 2) : "";
    }

    /**
     * Determines the differences between the actual and the stored object
     *
     * @param updatedObject the object after changes
     * @param storedObject  the object before changes
     * @return A list with the determined changes
     */
    protected TreeSet<String> determineDiff(T updatedObject, T storedObject) {
        TreeSet<String> result = new TreeSet<>();
        Arrays.stream(updatedObject.getClass().getMethods())
                .filter(m -> m.getParameterCount() == 0 && m.getName().startsWith("get"))
                .filter(m -> !m.getName().startsWith("getParent"))
                .filter(m -> !m.getName().startsWith("getMockitoInterceptor"))
                .filter(m -> !m.getReturnType().equals(Collection.class))
                .map(m -> {
                    try {
                        return checkForDiff(m.getName().substring(3), m.invoke(updatedObject), m.invoke(storedObject));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Failure while determining diff {}.{} at {}:", updatedObject.getClass().getName(), m.getName(), updatedObject.getIdentification());
                        log.error(e);
                        return String.format("Failure: %s.%s", updatedObject.getClass().getName(), m.getName());
                    }
                })
                .filter(Objects::nonNull)
                .forEach(result::add);
        return result;
    }

    /**
     * Checks for different identification of two objects
     *
     * @param valueName    the name of the value
     * @param updatedValue the value after changes
     * @param storedValue  the value before changes
     * @return the difference as text. {@code null} if both values are null or they are equal
     */
    protected String checkForDiff(String valueName, Object updatedValue, Object storedValue) {
        if (updatedValue == null && storedValue == null) {
            return null;
        }
        if (updatedValue instanceof IIdentifiableDao || storedValue instanceof IIdentifiableDao) {
            return checkDifferentIdentification(valueName, updatedValue, storedValue);
        }
        if (updatedValue == null || !updatedValue.equals(storedValue)) {
            return String.format(CHANGE_TEMPLATE, valueName, storedValue, updatedValue);
        }
        return null;
    }

    /**
     * Checks for different identification a {@link IIdentifiableDao}s
     *
     * @param valueName    the name of the value
     * @param updatedValue the value after changes
     * @param storedValue  the value before changes
     * @return the difference as text. {@code null} if both values are null or they have equal identifications
     */
    protected String checkDifferentIdentification(String valueName, Object updatedValue, Object storedValue) {
        if (updatedValue == null && storedValue instanceof IIdentifiableDao identifiableDao) {
            return String.format(CHANGE_TEMPLATE, valueName, identifiableDao.getIdentification(), "null");
        }
        if (storedValue == null && updatedValue instanceof IIdentifiableDao identifiableDao) {
            return String.format(CHANGE_TEMPLATE, valueName, "null", identifiableDao.getIdentification());
        }
        if (!(storedValue instanceof IIdentifiableDao) || !(updatedValue instanceof IIdentifiableDao)) {
            return null;
        }
        if (!((IIdentifiableDao) updatedValue).getIdentification().equals(((IIdentifiableDao) storedValue).getIdentification())) {
            return String.format(CHANGE_TEMPLATE, valueName, ((IIdentifiableDao) storedValue).getIdentification(), ((IIdentifiableDao) updatedValue).getIdentification());
        }
        return null;
    }

}
