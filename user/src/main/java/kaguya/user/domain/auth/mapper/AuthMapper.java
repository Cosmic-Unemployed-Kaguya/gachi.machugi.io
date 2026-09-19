package kaguya.user.domain.auth.mapper;

import kaguya.user.domain.auth.model.dto.request.RegisterReq;
import kaguya.user.domain.auth.model.dto.response.LoginRes;
import kaguya.user.domain.common.model.enums.Gender;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.entity.UserProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    // DTO -> UserEntity
    public UserEntity userDtoToUserEntity(RegisterReq request, String encodedPassword) {
        return UserEntity.builder()
//                .username(request.account().username())
                .email(request.account().email())
                .password(encodedPassword)
                .nickname(request.account().nickname())
                .point(0L)
                .build();
    }

    // DTO -> UserProfileEntity
    public UserProfileEntity userDtoToUserProfileEntity(RegisterReq request, Long userIdx) {
        return UserProfileEntity.builder()
                .userIdx(userIdx)
                .name(request.user().name())
                .birth(request.user().birth())
                .phone(request.user().phone())
                .gender(Gender.fromString(request.user().gender()))
                .build();
    }

    // Entity -> LoginRes
    public LoginRes entityToLoginRes(String accessToken, String refreshToken, UserEntity entity) {
        return new LoginRes(
                accessToken,
                refreshToken,
                entity.getNickname()
        );
    }
}