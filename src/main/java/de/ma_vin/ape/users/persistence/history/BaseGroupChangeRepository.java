package de.ma_vin.ape.users.persistence.history;

import de.ma_vin.ape.users.model.gen.dao.group.BaseGroupDao;
import de.ma_vin.ape.users.model.gen.dao.group.history.BaseGroupChangeDao;
import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BaseGroupChangeRepository extends JpaRepository<BaseGroupChangeDao, Long> {

    @Transactional
    @Modifying
    @Query(value = "update BaseGroupChangeDao c set c.baseGroup=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :baseGroupId ELSE CONCAT(c.deletionInformation, ' ', :baseGroupId) END)  where c.baseGroup=:baseGroup")
    void markedAsDeleted(@Param("baseGroup") BaseGroupDao baseGroup, @Param("baseGroupId") String baseGroupIdentification);

    @Transactional
    @Modifying
    @Query(value = "update BaseGroupChangeDao c set c.editor=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :editorId ELSE CONCAT(c.deletionInformation, ' ', :editorId) END) where c.editor=:editor")
    void markedEditorAsDeleted(@Param("editor") UserDao editor, @Param("editorId") String editorIdentification);
}
