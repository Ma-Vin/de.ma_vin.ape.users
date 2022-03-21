package de.ma_vin.ape.users.persistence.history;

import de.ma_vin.ape.users.model.gen.dao.group.AdminGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.AdminGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AdminGroupChangeRepository extends JpaRepository<AdminGroupChangeDao, Long> {

    List<AdminGroupChangeDao> findByAdminGroup(AdminGroupDao adminGroup);

    @Transactional
    @Modifying
    @Query(value = "update AdminGroupChangeDao c set c.adminGroup=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :adminGroupId ELSE CONCAT(c.deletionInformation, ' ', :adminGroupId) END)  where c.adminGroup=:adminGroup")
    void markedAsDeleted(@Param("adminGroup") AdminGroupDao adminGroup, @Param("adminGroupId") String adminGroupIdentification);

    @Transactional
    @Modifying
    @Query(value = "update AdminGroupChangeDao c set c.admin=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :adminId ELSE CONCAT(c.deletionInformation, ' ', :adminId) END) where c.admin=:admin")
    void markedAsDeleted(@Param("admin") UserDao admin, @Param("adminId") String adminIdentification);

    @Transactional
    @Modifying
    @Query(value = "update AdminGroupChangeDao c set c.editor=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :editorId ELSE CONCAT(c.deletionInformation, ' ', :editorId) END) where c.editor=:editor")
    void markedEditorAsDeleted(@Param("editor") UserDao editor, @Param("editorId") String editorIdentification);
}
