package kaguya.user.domain.user.mapper;

import kaguya.user.domain.user.model.dto.response.GetUserDetailsReq;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoReq;
import kaguya.user.domain.user.model.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdminMapper {

    // todo. findAll 말고 페이징/검색으로 변경
    // List<userEntity> -> GetUsersInfoReq
    public GetUsersInfoReq userListToGetUsersInfoReq(List<UserEntity> list) {

        // 임시 방편 (findAll)
        List<GetUserDetailsReq> userList = new ArrayList<>();
        for (UserEntity userEntity : list) {
            GetUserDetailsReq userInfo = userEntityToGetUserDetailsReq(userEntity);
            userList.add(userInfo);
        }

        return new GetUsersInfoReq(userList);
    }

    // UserEntity -> GetUserDetailsReq
    public GetUserDetailsReq userEntityToGetUserDetailsReq(UserEntity entity) {
        return new GetUserDetailsReq(
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


}