package kaguya.user.domain.user.repository;

import kaguya.user.domain.user.model.entity.UserManagementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserManagementRepository extends JpaRepository<UserManagementEntity, Long> {

}
