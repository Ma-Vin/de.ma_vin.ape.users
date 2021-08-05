package de.ma_vin.ape.users.model.mapper;

import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.model.gen.mapper.UserAccessMapper;
import de.ma_vin.util.layer.generator.annotations.mapper.ExtendingAccessMapper;

@ExtendingAccessMapper
public class UserAccessMapperExt extends UserAccessMapper {

    @Override
    protected void setUserValues(UserDao dao, User domain) {
        super.setUserValues(dao, domain);
        domain.setGlobalAdmin(dao.getParentAdminGroup() != null);
        domain.setCommonGroupId(dao.getParentCommonGroup() != null ? dao.getParentCommonGroup().getIdentification() : null);
    }
}
