package kaguya.user.domain.user.repository;

import kaguya.user.domain.user.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

//    Optional<UserEntity> findByUsername(String username);
//    boolean existsByUsername(String username);
}