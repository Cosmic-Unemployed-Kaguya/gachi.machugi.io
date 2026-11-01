package kaguya.user.domain.user.mapper;

import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.response.GetBlockDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetBlocksInfoRes;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.entity.UserManagementEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdminMapper {

    // 메서드명 긴거 실화냐

    // Page<UserEntity> -> GetUsersInfoReq
    public GetUsersInfoRes userListToGetUsersInfoReq(Page<UserEntity> page) {

        List<GetUserDetailsRes> userList = page.getContent().stream()
                .map(this::userEntityToGetUserDetailsRes)
                .toList();

        return new GetUsersInfoRes(
                userList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    // UserEntity -> GetUserDetailsReq
    public GetUserDetailsRes userEntityToGetUserDetailsRes(UserEntity entity) {
        return new GetUserDetailsRes(
                entity.getIdx(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getPoint(),
                entity.getRole().name(),
                entity.getStatus().name(),
                entity.getJoinDate(),
                entity.getLastLoginDate(),
                entity.getWithdrawalDate()
        );
    }

    // BlockReq -> UserManagementEntity
    public UserManagementEntity blockReqToUserManagementEntity(UserEntity userEntity, UserEntity adminEntity, BlockReq request) {
        return new UserManagementEntity(
                userEntity,
                adminEntity,
                request.managementType(),
                request.reason(),
                request.endDate()
        );
    }

    // Page<UserManagementEntity> -> GetBlocksInfoRes
    public GetBlocksInfoRes userManagementPageToGetBlocksInfoRes(Page<UserManagementEntity> page) {

        List<GetBlockDetailsRes> blockList = new ArrayList<>();
        for (UserManagementEntity userManagementEntity : page.getContent()) {
            blockList.add(userManagementToGetBlockDetailsRes(userManagementEntity));
        }

        return new GetBlocksInfoRes(
                blockList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    // UserManagementEntity -> GetBlockDetailsRes
    private GetBlockDetailsRes userManagementToGetBlockDetailsRes(UserManagementEntity entity) {
        return new GetBlockDetailsRes(
                entity.getIdx(),
                entity.getUser().getIdx(),
                entity.getUser().getEmail(),
                entity.getUser().getNickname(),
                entity.getManagementType().name(),
                entity.getReason(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getAdmin().getNickname()
        );
    }
}