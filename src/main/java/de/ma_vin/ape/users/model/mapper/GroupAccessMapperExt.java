package de.ma_vin.ape.users.model.mapper;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.group.PrivilegeGroup;
import de.ma_vin.ape.users.model.gen.mapper.GroupAccessMapper;
import de.ma_vin.util.layer.generator.annotations.mapper.ExtendingAccessMapper;

@ExtendingAccessMapper
public class GroupAccessMapperExt extends GroupAccessMapper {

    @Override
    protected void setBaseGroupValues(BaseGroupDao dao, BaseGroup domain) {
        super.setBaseGroupValues(dao, domain);
        domain.setCommonGroupId(dao.getParentCommonGroup() != null ? dao.getParentCommonGroup().getIdentification() : null);
    }

    @Override
    protected void setPrivilegeGroupValues(PrivilegeGroupDao dao, PrivilegeGroup domain) {
        super.setPrivilegeGroupValues(dao, domain);
        domain.setCommonGroupId(dao.getParentCommonGroup() != null ? dao.getParentCommonGroup().getIdentification() : null);
    }
}
