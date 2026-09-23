package kaguya.user.domain.user.mapper;

import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.entity.UserManagementEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdminMapper {

    // todo. findAll 말고 페이징/검색으로 변경
    // List<userEntity> -> GetUsersInfoReq
    public GetUsersInfoRes userListToGetUsersInfoReq(List<UserEntity> list) {

        // 임시 방편 (findAll)
        List<GetUserDetailsRes> userList = new ArrayList<>();
        for (UserEntity userEntity : list) {
            GetUserDetailsRes userInfo = userEntityToGetUserDetailsReq(userEntity);
            userList.add(userInfo);
        }

        return new GetUsersInfoRes(userList);
    }

    // UserEntity -> GetUserDetailsReq
    public GetUserDetailsRes userEntityToGetUserDetailsReq(UserEntity entity) {
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
}