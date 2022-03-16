package de.ma_vin.ape.users.persistence.history;

import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.CommonGroupChangeDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CommonGroupChangeRepository extends JpaRepository<CommonGroupChangeDao, Long> {

    @Transactional
    @Modifying
    @Query(value = "update CommonGroupChangeDao c set c.commonGroup=null, c.deletionInformation=:commonGroupId where c.commonGroup=:commonGroup")
    void markedAsDeleted(@Param("commonGroup") CommonGroupDao commonGroup, @Param("commonGroupId") String commonGroupIdentification);
}
