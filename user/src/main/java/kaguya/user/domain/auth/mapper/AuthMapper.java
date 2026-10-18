package kaguya.user.domain.auth.mapper;

import kaguya.user.domain.auth.model.dto.request.RegisterReq;
import kaguya.user.domain.auth.model.dto.response.LoginRes;
import kaguya.user.domain.common.model.enums.Gender;
import kaguya.user.domain.user.model.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    // DTO -> Entity
    public UserEntity userDtoToEntity(RegisterReq request, String encodedPassword) {
        return UserEntity.builder()
                .username(request.account().username())
                .password(encodedPassword)
                .email(request.account().email())
                .nickname(request.account().nickname())
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