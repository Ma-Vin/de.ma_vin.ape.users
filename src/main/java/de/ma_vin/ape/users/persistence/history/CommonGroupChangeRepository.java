package de.ma_vin.ape.users.persistence.history;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.CommonGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.PrivilegeGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.CommonGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommonGroupChangeRepository extends JpaRepository<CommonGroupChangeDao, Long> {

    List<CommonGroupChangeDao> findByCommonGroup(CommonGroupDao commonGroup);

    @Transactional
    @Modifying
    @Query(value = "update CommonGroupChangeDao c set c.commonGroup=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :commonGroupId ELSE CONCAT(c.deletionInformation, ' ', :commonGroupId) END) where c.commonGroup=:commonGroup")
    void markedAsDeleted(@Param("commonGroup") CommonGroupDao commonGroup, @Param("commonGroupId") String commonGroupIdentification);

    @Transactional
    @Modifying
    @Query(value = "update CommonGroupChangeDao c set c.baseGroup=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :baseGroupId ELSE CONCAT(c.deletionInformation, ' ', :baseGroupId) END) where c.baseGroup=:baseGroup")
    void markedAsDeleted(@Param("baseGroup") BaseGroupDao baseGroup, @Param("baseGroupId") String baseGroupIdentification);

    @Transactional
    @Modifying
    @Query(value = "update CommonGroupChangeDao c set c.privilegeGroup=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :privilegeGroupId ELSE CONCAT(c.deletionInformation, ' ', :privilegeGroupId) END) where c.privilegeGroup=:privilegeGroup")
    void markedAsDeleted(@Param("privilegeGroup") PrivilegeGroupDao privilegeGroup, @Param("privilegeGroupId") String privilegeGroupIdentification);

    @Transactional
    @Modifying
    @Query(value = "update CommonGroupChangeDao c set c.user=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :userId ELSE CONCAT(c.deletionInformation, ' ', :userId) END) where c.user=:user")
    void markedAsDeleted(@Param("user") UserDao user, @Param("userId") String userIdentification);

    @Transactional
    @Modifying
    @Query(value = "update CommonGroupChangeDao c set c.editor=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :editorId ELSE CONCAT(c.deletionInformation, ' ', :editorId) END) where c.editor=:editor")
    void markedEditorAsDeleted(@Param("editor") UserDao editor, @Param("editorId") String editorIdentification);
}
