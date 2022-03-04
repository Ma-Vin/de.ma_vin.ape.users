package de.ma_vin.ape.users.persistence;

import de.ma_vin.ape.users.model.gen.dao.security.TokenDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenDao, Long> {

    Optional<TokenDao> findByUuid(String uuid);

    boolean existsByUuid(String uuid);

    @Transactional
    long deleteByUuid(String uuid);

    @Transactional
    @Query(value = "DELETE FROM TokenDao t WHERE t.expiresAtLeast<:actualDateTime")
    long deleteExpired(@Param("actualDateTime") LocalDateTime actualDateTime);

    @Transactional
    @Query(value = "DELETE FROM TokenDao t WHERE t.expiresAtLeast IS NULL")
    long deleteWithoutExpiration();
}
