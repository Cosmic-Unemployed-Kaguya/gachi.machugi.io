package kaguya.user.domain.user.repository;

import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.enums.ManagementType;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryCustom {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

//    Optional<UserEntity> findByUsername(String username);
//    boolean existsByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE UserEntity u SET u.status = :dormant
        WHERE u.status = :active
          AND u.role <> :excludedRole
          AND COALESCE(u.lastLoginDate, u.joinDate) < :cutoff
        """)
    int convertToDormant(
            @Param("active") Status active,
            @Param("dormant") Status dormant,
            @Param("excludedRole") Role excludedRole,
            @Param("cutoff") LocalDateTime cutoff
    );

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE UserEntity u SET u.status = :active
        WHERE u.status = :suspended
          AND NOT EXISTS (
              SELECT m FROM UserManagementEntity m
              WHERE m.user = u
                AND m.managementType = :suspendType
                AND (m.endDate IS NULL OR m.endDate > :now)
          )
        """)
    int releaseSuspended(
            @Param("suspended") Status suspended,
            @Param("active") Status active,
            @Param("suspendType") ManagementType suspendType,
            @Param("now") LocalDateTime now
    );
}