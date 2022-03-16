package de.ma_vin.ape.users.service.history;

import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Log4j2
public abstract class AbstractChangeService<T extends IIdentifiableDao> {
    public static final String CHANGE_TEMPLATE = "%s: \"%s\" -> \"%s\"";

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
        if (storedValue != null && updatedValue == null) {
            return String.format(CHANGE_TEMPLATE, valueName, storedValue, "null");
        }
        if (storedValue == null && updatedValue != null) {
            return String.format(CHANGE_TEMPLATE, valueName, "null", updatedValue);
        }
        if (!updatedValue.equals(storedValue)) {
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
        if (updatedValue == null && storedValue != null && storedValue instanceof IIdentifiableDao identifiableDao) {
            return String.format(CHANGE_TEMPLATE, valueName, identifiableDao.getIdentification(), "null");
        }
        if (updatedValue != null && storedValue == null && updatedValue instanceof IIdentifiableDao identifiableDao) {
            return String.format(CHANGE_TEMPLATE, valueName, "null", identifiableDao.getIdentification());
        }
        if ((updatedValue == null && storedValue == null) || !(storedValue instanceof IIdentifiableDao) || !(updatedValue instanceof IIdentifiableDao)) {
            return null;
        }
        if (!((IIdentifiableDao) updatedValue).getIdentification().equals(((IIdentifiableDao) storedValue).getIdentification())) {
            return String.format(CHANGE_TEMPLATE, valueName, ((IIdentifiableDao) storedValue).getIdentification(), ((IIdentifiableDao) updatedValue).getIdentification());
        }
        return null;
    }

}
