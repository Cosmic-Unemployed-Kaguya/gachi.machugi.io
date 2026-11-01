package kaguya.user.domain.user.repository;

import kaguya.user.domain.user.model.dto.request.BlockSearchReq;
import kaguya.user.domain.user.model.entity.UserManagementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserManagementRepositoryCustom {
    Page<UserManagementEntity> findByCondition(BlockSearchReq condition, Pageable pageable);
}