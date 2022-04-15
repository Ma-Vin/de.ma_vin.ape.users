package de.ma_vin.ape.users.enums;

import de.ma_vin.ape.users.model.domain.group.AdminGroupExt;
import de.ma_vin.ape.users.model.domain.group.BaseGroupExt;
import de.ma_vin.ape.users.model.domain.group.CommonGroupExt;
import de.ma_vin.ape.users.model.domain.group.PrivilegeGroupExt;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.domain.resource.UserResource;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class ModelTypeTest {

    @DisplayName("Get model type with direct class")
    @Test
    public void testGetModelTypeByClass() {
        assertEquals(ModelType.ADMIN_GROUP, ModelType.getModelTypeByClass(AdminGroup.class), "Wrong type for admin group");
        assertEquals(ModelType.COMMON_GROUP, ModelType.getModelTypeByClass(CommonGroup.class), "Wrong type for common group");
        assertEquals(ModelType.PRIVILEGE_GROUP, ModelType.getModelTypeByClass(PrivilegeGroup.class), "Wrong type for privilege group");
        assertEquals(ModelType.BASE_GROUP, ModelType.getModelTypeByClass(BaseGroup.class), "Wrong type for base group");
        assertEquals(ModelType.USER, ModelType.getModelTypeByClass(User.class), "Wrong type for user");
        assertEquals(ModelType.USER_RESOURCE, ModelType.getModelTypeByClass(UserResource.class), "Wrong type for user resource");
    }

    @DisplayName("Get model type with extended class")
    @Test
    public void testGetModelTypeByClassExtended() {
        assertEquals(ModelType.ADMIN_GROUP, ModelType.getModelTypeByClass(AdminGroupExt.class), "Wrong type for admin group");
        assertEquals(ModelType.COMMON_GROUP, ModelType.getModelTypeByClass(CommonGroupExt.class), "Wrong type for common group");
        assertEquals(ModelType.PRIVILEGE_GROUP, ModelType.getModelTypeByClass(PrivilegeGroupExt.class), "Wrong type for privilege group");
        assertEquals(ModelType.BASE_GROUP, ModelType.getModelTypeByClass(BaseGroupExt.class), "Wrong type for base group");
        assertEquals(ModelType.USER, ModelType.getModelTypeByClass(UserExt.class), "Wrong type for user");
    }

    @DisplayName("Get model type with mockito class")
    @Test
    public void testGetModelTypeByClassMockito() {
        assertEquals(ModelType.ADMIN_GROUP, ModelType.getModelTypeByClass(mock(AdminGroup.class).getClass()), "Wrong type for admin group");
        assertEquals(ModelType.COMMON_GROUP, ModelType.getModelTypeByClass(mock(CommonGroup.class).getClass()), "Wrong type for common group");
        assertEquals(ModelType.PRIVILEGE_GROUP, ModelType.getModelTypeByClass(mock(PrivilegeGroup.class).getClass()), "Wrong type for privilege group");
        assertEquals(ModelType.BASE_GROUP, ModelType.getModelTypeByClass(mock(BaseGroup.class).getClass()), "Wrong type for base group");
        assertEquals(ModelType.USER, ModelType.getModelTypeByClass(mock(User.class).getClass()), "Wrong type for user");
        assertEquals(ModelType.USER_RESOURCE, ModelType.getModelTypeByClass(mock(UserResource.class).getClass()), "Wrong type for user resource");
    }
}
