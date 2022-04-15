package de.ma_vin.ape.users.model.mapper;

import de.ma_vin.ape.users.enums.ModelType;
import de.ma_vin.ape.users.model.gen.domain.IIdentifiable;
import de.ma_vin.ape.users.model.gen.domain.group.history.AdminGroupChange;
import de.ma_vin.ape.users.model.gen.domain.group.history.BaseGroupChange;
import de.ma_vin.ape.users.model.gen.domain.group.history.CommonGroupChange;
import de.ma_vin.ape.users.model.gen.domain.group.history.PrivilegeGroupChange;
import de.ma_vin.ape.users.model.gen.domain.history.AbstractChange;
import de.ma_vin.ape.users.model.gen.domain.user.history.UserChange;
import de.ma_vin.ape.users.model.gen.dto.history.ChangeDto;

/**
 * Maps the domain changes to a dto change
 */
public class ChangeTransportMapper {

    private ChangeTransportMapper(){}

    /**
     * Converts a user change
     *
     * @param userChange the user change to convert
     * @return the converted user change
     */
    public static ChangeDto convertToChangeDto(UserChange userChange) {
        return switch (userChange.getChangeType()) {
            case CREATE, MODIFY -> createChangeDto(userChange, userChange.getUser());
            case DELETE -> createChangeDto(userChange, userChange.getDeletionInformation());
            default -> createChangeDto(userChange);
        };
    }

    /**
     * Converts a admin group change
     *
     * @param adminGroupChange the admin group change to convert
     * @return the converted admin group change
     */
    public static ChangeDto convertToChangeDto(AdminGroupChange adminGroupChange) {
        return switch (adminGroupChange.getChangeType()) {
            case ADD, REMOVE -> createChangeDto(adminGroupChange, adminGroupChange.getAdminGroup(), adminGroupChange.getAdmin());
            case CREATE, MODIFY -> createChangeDto(adminGroupChange, adminGroupChange.getAdminGroup());
            case DELETE -> createChangeDto(adminGroupChange, adminGroupChange.getDeletionInformation());
            default -> createChangeDto(adminGroupChange);
        };
    }

    /**
     * Converts a common group change
     *
     * @param commonGroupChange the common group change to convert
     * @return the converted common group change
     */
    public static ChangeDto convertToChangeDto(CommonGroupChange commonGroupChange) {
        return switch (commonGroupChange.getChangeType()) {
            case ADD, REMOVE -> createCommonGroupAddRemoveChange(commonGroupChange);
            case CREATE, MODIFY -> createChangeDto(commonGroupChange, commonGroupChange.getCommonGroup());
            case DELETE -> createChangeDto(commonGroupChange, commonGroupChange.getDeletionInformation());
            default -> createChangeDto(commonGroupChange);
        };
    }

    /**
     * Creates a change of a common group for add or remove change type
     *
     * @param commonGroupChange the common group change to convert
     * @return the converted common group change
     */
    private static ChangeDto createCommonGroupAddRemoveChange(CommonGroupChange commonGroupChange) {
        if (commonGroupChange.getPrivilegeGroup() != null) {
            return createChangeDto(commonGroupChange, commonGroupChange.getCommonGroup(), commonGroupChange.getPrivilegeGroup());
        }
        if (commonGroupChange.getBaseGroup() != null) {
            return createChangeDto(commonGroupChange, commonGroupChange.getCommonGroup(), commonGroupChange.getBaseGroup());
        }
        if (commonGroupChange.getUser() != null) {
            return createChangeDto(commonGroupChange, commonGroupChange.getCommonGroup(), commonGroupChange.getUser());
        }
        return null;
    }

    /**
     * Converts a privilege group change
     *
     * @param privilegeGroupChange the privilege group change to convert
     * @return the converted privilege group change
     */
    public static ChangeDto convertToChangeDto(PrivilegeGroupChange privilegeGroupChange) {
        return switch (privilegeGroupChange.getChangeType()) {
            case ADD, REMOVE -> createPrivilegeGroupAddRemoveChange(privilegeGroupChange);
            case CREATE, MODIFY -> createChangeDto(privilegeGroupChange, privilegeGroupChange.getPrivilegeGroup());
            case DELETE -> createChangeDto(privilegeGroupChange, privilegeGroupChange.getDeletionInformation());
            default -> createChangeDto(privilegeGroupChange);
        };
    }

    /**
     * Creates a change of a privilege group for add or remove change type
     *
     * @param privilegeGroupChange the privilege group change to convert
     * @return the converted privilege group change
     */
    private static ChangeDto createPrivilegeGroupAddRemoveChange(PrivilegeGroupChange privilegeGroupChange) {
        if (privilegeGroupChange.getBaseGroup() != null) {
            return createChangeDto(privilegeGroupChange, privilegeGroupChange.getPrivilegeGroup(), privilegeGroupChange.getBaseGroup());
        }
        if (privilegeGroupChange.getUser() != null) {
            return createChangeDto(privilegeGroupChange, privilegeGroupChange.getPrivilegeGroup(), privilegeGroupChange.getUser());
        }
        return null;
    }

    /**
     * Converts a base group change
     *
     * @param baseGroupChange the base group change to convert
     * @return the converted base group change
     */
    public static ChangeDto convertToChangeDto(BaseGroupChange baseGroupChange) {
        return switch (baseGroupChange.getChangeType()) {
            case ADD, REMOVE -> createBaseGroupAddRemoveChange(baseGroupChange);
            case CREATE, MODIFY -> createChangeDto(baseGroupChange, baseGroupChange.getBaseGroup());
            case DELETE -> createChangeDto(baseGroupChange, baseGroupChange.getDeletionInformation());
            default -> createChangeDto(baseGroupChange);
        };
    }

    /**
     * Creates a change of a base group for add or remove change type
     *
     * @param baseGroupChange the base group change to convert
     * @return the converted base group change
     */
    private static ChangeDto createBaseGroupAddRemoveChange(BaseGroupChange baseGroupChange) {
        if (baseGroupChange.getSubBaseGroup() != null) {
            return createChangeDto(baseGroupChange, baseGroupChange.getBaseGroup(), baseGroupChange.getSubBaseGroup());
        }
        if (baseGroupChange.getUser() != null) {
            return createChangeDto(baseGroupChange, baseGroupChange.getBaseGroup(), baseGroupChange.getUser());
        }
        return null;
    }

    /**
     * Creates a dto change and maps the common entries of {@link AbstractChange}
     *
     * @param sourceChange the domain object which is to map
     * @param subject      the object which is affected
     * @return a new dto change
     */
    private static ChangeDto createChangeDto(AbstractChange sourceChange, IIdentifiable subject) {
        return createChangeDto(sourceChange, subject, null);
    }

    /**
     * Creates a dto change and maps the common entries of {@link AbstractChange}
     *
     * @param sourceChange the domain object which is to map
     * @param subject      the object which is affected
     * @param target       the object which is added or removed to/from the subject
     * @return a new dto change
     */
    private static ChangeDto createChangeDto(AbstractChange sourceChange, IIdentifiable subject, IIdentifiable target) {
        return createChangeDto(sourceChange, subject.getIdentification(), target != null ? target.getIdentification() : null
                , target != null ? ModelType.getModelTypeByClass(target.getClass()) : null);
    }

    /**
     * Creates a dto change and maps the common entries of {@link AbstractChange}
     *
     * @param sourceChange the domain object which is to map
     * @return a new dto change
     */
    private static ChangeDto createChangeDto(AbstractChange sourceChange) {
        return createChangeDto(sourceChange, (String) null);
    }

    /**
     * Creates a dto change and maps the common entries of {@link AbstractChange}
     *
     * @param sourceChange          the domain object which is to map
     * @param subjectIdentification identification of the object which is affected
     * @return a new dto change
     */
    private static ChangeDto createChangeDto(AbstractChange sourceChange, String subjectIdentification) {
        return createChangeDto(sourceChange, subjectIdentification, null, null);
    }

    /**
     * Creates a dto change and maps the common entries of {@link AbstractChange}
     *
     * @param sourceChange          the domain object which is to map
     * @param subjectIdentification identification of the object which is affected
     * @param targetIdentification  identification of the object which is added or removed to/from the subject
     * @param targetType            the typ of the object which is added or removed to/from the subject
     * @return a new dto change
     */
    private static ChangeDto createChangeDto(AbstractChange sourceChange, String subjectIdentification, String targetIdentification, ModelType targetType) {
        ChangeDto result = new ChangeDto();

        result.setSubjectIdentification(subjectIdentification);
        result.setTargetIdentification(targetIdentification);

        result.setChangeType(sourceChange.getChangeType());
        result.setChangeTime(sourceChange.getChangeTime());
        result.setEditor(sourceChange.getEditor() != null ? sourceChange.getEditor().getIdentification() : null);
        result.setIsEditorAdmin(sourceChange.getEditor() != null && sourceChange.getEditor().isGlobalAdmin());
        result.setAction(sourceChange.getAction());
        result.setTargetType(targetType);

        return result;
    }
}
