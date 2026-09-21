package kaguya.user.domain.user.mapper;

import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileRes;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.entity.UserProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    // Entity -> MyPageReq
    public MyPageRes entityToMyPageReq(UserEntity entity) {
        return new MyPageRes(
//                entity.getUsername(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getJoinDate()
        );
    }

    // Entity -> ProfileReq
    public ProfileRes entityToUserReq(UserProfileEntity entity) {
        return new ProfileRes(
                entity.getName(),
                entity.getBirth(),
                entity.getPhone(),
                entity.getGender().toString()
        );
    }
}