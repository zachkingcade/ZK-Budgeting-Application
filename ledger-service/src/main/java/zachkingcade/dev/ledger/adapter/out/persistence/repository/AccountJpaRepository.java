package zachkingcade.dev.ledger.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.AccountEntity;

import java.util.Collection;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    @Query("""
            SELECT (COUNT(a) > 0) FROM AccountEntity a JOIN a.type t JOIN t.classification c
            WHERE a.id = :accountId AND a.userId = :userId AND c.description = :classificationDescription
            """)
    boolean isAccountWithClassification(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId,
            @Param("classificationDescription") String classificationDescription);

    @Query("""
            SELECT (COUNT(a) > 0) FROM AccountEntity a JOIN a.type t
            WHERE a.id = :accountId AND a.userId = :userId AND t.description = :typeDescription
            """)
    boolean isAccountWithTypeDescription(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId,
            @Param("typeDescription") String typeDescription);

    @Query("""
            SELECT (COUNT(a) > 0) FROM AccountEntity a JOIN a.type t
            WHERE a.id = :accountId AND a.userId = :userId AND t.description IN :typeDescriptions
            """)
    boolean isAccountWithAnyTypeDescription(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId,
            @Param("typeDescriptions") Collection<String> typeDescriptions);

    Optional<AccountEntity> findByIdAndUserId(Long id, Long userId);

    Boolean existsByDescriptionAndUserIdAndType_Id(String description, Long userId, Long typeId);

    Optional<AccountEntity> findByDescriptionAndUserIdAndType_Id(String description, Long userId, Long typeId);
}
