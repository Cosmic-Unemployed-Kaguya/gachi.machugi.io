package kaguya.user.domain.user.repository;

import kaguya.user.domain.user.model.dto.request.UserSearchReq;
import kaguya.user.domain.user.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<UserEntity> findByCondition(UserSearchReq condition, Pageable pageable);
}