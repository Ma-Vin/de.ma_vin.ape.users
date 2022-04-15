package de.ma_vin.ape.users.enums;

import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.resource.UserResource;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ModelType {
    ADMIN_GROUP(AdminGroup.class.getSimpleName()),
    BASE_GROUP(BaseGroup.class.getSimpleName()),
    COMMON_GROUP(CommonGroup.class.getSimpleName()),
    PRIVILEGE_GROUP(PrivilegeGroup.class.getSimpleName()),
    USER(User.class.getSimpleName()),
    USER_RESOURCE(UserResource.class.getSimpleName()),
    UNKNOWN("Unknown");

    private String className;

    public static ModelType getModelTypeByClassName(String className) {
        return Arrays.stream(ModelType.values()).filter(mt -> mt.getClassName().equals(className)).findFirst().orElse(UNKNOWN);
    }

    public static ModelType getModelTypeByClass(Class<?> clazz) {
        return clazz.getSimpleName().contains("$MockitoMock$")
                ? getModelTypeByClass(clazz.getSuperclass())
                : getModelTypeByClassName(clazz.getSimpleName());
    }
}
