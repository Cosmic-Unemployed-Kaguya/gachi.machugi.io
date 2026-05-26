package kaguya.domain.user.util.mapper;

import kaguya.domain.user.model.dto.response.MyPageRes;
import kaguya.domain.user.model.dto.UserDto;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.model.dto.response.LoginRes;
import kaguya.domain.user.model.entity.UserEntity;
import kaguya.domain.user.model.enums.Gender;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

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

    // Entity -> MyPageReq
    public MyPageRes entityToMyPageReq(UserEntity entity) {
        return new MyPageRes(
                entity.getUsername(),
                entity.getNickname(),
                entity.getEmail()
        );
    }

    // Entity -> UserDto
    public UserDto entityToUserDto(UserEntity entity) {
        return new UserDto(
                entity.getName(),
                entity.getBirth(),
                entity.getPhone(),
                entity.getGender().toString()
        );
    }
}