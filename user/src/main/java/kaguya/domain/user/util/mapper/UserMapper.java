package kaguya.domain.user.util.mapper;

import kaguya.domain.user.model.dto.request.MyPageReq;
import kaguya.domain.user.model.dto.request.ProfileReq;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.model.dto.response.LoginRes;
import kaguya.domain.user.model.entity.UserEntity;
import kaguya.domain.user.model.enums.Gender;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // DTO -> Entity
    public UserEntity toEntity(RegisterReq request, String encodedPassword) {
        return UserEntity.builder()
                .username(request.account().username())
                .password(encodedPassword)
                .nickname(request.account().nickname())
                .email(request.account().email())
                .name(request.user().name())
                .birth(request.user().birth())
                .phone(request.user().phone())
                .gender(Gender.fromString(request.user().gender()))
                .build();
    }

    // Entity -> LoginRes
    public LoginRes toLoginRes(String accessToken, String refreshToken, UserEntity entity) {
        return new LoginRes(
                accessToken,
                refreshToken,
                entity.getNickname()
        );
    }

    // Entity -> MyPageReq
    public MyPageReq toMyPageReq(UserEntity entity) {
        return new MyPageReq(
                entity.getUsername(),
                entity.getNickname(),
                entity.getEmail()
        );
    }

    // Entity -> ProfileReq
    public ProfileReq toProfileReq(UserEntity entity) {
        return new ProfileReq(
                entity.getName(),
                entity.getBirth(),
                entity.getPhone(),
                entity.getGender().toString()
        );
    }
}