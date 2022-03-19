package de.ma_vin.ape.users.persistence.history;

import de.ma_vin.ape.users.model.gen.dao.user.UserDao;
import de.ma_vin.ape.users.model.gen.dao.user.history.UserChangeDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserChangeRepository extends JpaRepository<UserChangeDao, Long> {

    @Transactional
    @Modifying
    @Query(value = "update UserChangeDao c set c.user=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :userId ELSE CONCAT(c.deletionInformation, ' ', :userId) END)  where c.user=:user")
    void markedAsDeleted(@Param("user") UserDao user, @Param("userId") String userIdentification);

    @Transactional
    @Modifying
    @Query(value = "update UserChangeDao c set c.editor=null, c.deletionInformation = (CASE WHEN c.deletionInformation=null THEN :editorId ELSE CONCAT(c.deletionInformation, ' ', :editorId) END) where c.editor=:editor")
    void markedEditorAsDeleted(@Param("editor") UserDao editor, @Param("editorId") String editorIdentification);
}
