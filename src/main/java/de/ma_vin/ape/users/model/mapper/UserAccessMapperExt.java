package de.ma_vin.ape.users.model.mapper;

import com.github.ma_vin.util.layer_generator.annotations.mapper.ExtendingAccessMapper;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.mapper.UserAccessMapper;

@ExtendingAccessMapper
public class UserAccessMapperExt extends UserAccessMapper {

    @Override
    protected void setUserValues(UserDao dao, User domain) {
        super.setUserValues(dao, domain);
        domain.setGlobalAdmin(dao.getParentAdminGroup() != null);
        domain.setCommonGroupId(dao.getParentCommonGroup() != null ? dao.getParentCommonGroup().getIdentification() : null);
    }
}
