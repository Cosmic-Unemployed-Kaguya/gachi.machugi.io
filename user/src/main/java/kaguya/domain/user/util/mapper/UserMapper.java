package kaguya.domain.user.util.mapper;

import kaguya.domain.user.model.dto.AccountDTO;
import kaguya.domain.user.model.dto.UserDTO;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.model.dto.response.LoginRes;
import kaguya.domain.user.model.entity.UserEntity;
import kaguya.domain.user.model.enums.Gender;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMapper {

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

    public LoginRes toLoginRes(String accessToken, String refreshToken, UserEntity entity) {
        return new LoginRes(accessToken, refreshToken, entity.getNickname());
    }

    /**
     * ===========================
     * gRPC Request -> Service DTO
     * ===========================
     */
    public RegisterReq toRegisterReq(RegisterRequest request) {
        AccountDTO accountDTO = toAccountDto(request.getAccount());
        UserDTO userDTO = toUserDto(request.getUser());

        return new RegisterReq(accountDTO, userDTO);
    }

    private AccountDTO toAccountDto(AccountData account) {
        return new AccountDTO(
                account.getUsername(),
                account.getPassword(),
                account.getNickname(),
                account.getEmail()
        );
    }

    private UserDTO toUserDto(UserData user) {
        return new UserDTO(
                user.getName(),
                LocalDate.parse(user.getBirth()),  // String -> LocalDate 변환
                user.getPhone(),
                user.getGender()
        );
    }
}
