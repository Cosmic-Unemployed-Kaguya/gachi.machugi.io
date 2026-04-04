package kaguya.user.service;

import kaguya.user.model.dto.request.RegisterReq;
import kaguya.user.model.entity.UserEntity;
import kaguya.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * todo.
     * - 회원가입 시 사용자 개인정보 (이름, 생년월일, 핸드폰 번호) 양방향 암호화
     * - 로그인 기능
     */

    @Transactional
    public void register(RegisterReq registerData) {

        if(userRepository.existsByUsername(registerData.account().username())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if(userRepository.existsByUsername(registerData.account().email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String rawPassword = registerData.account().password();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        UserEntity entity = UserEntity.builder()
                .username(registerData.account().username())
                .password(encodedPassword)
                .nickname(registerData.account().nickname())
                .email(registerData.account().email())
                .name(registerData.user().name())
                .birth(registerData.user().birth())
                .phone(registerData.user().phone())
                .build();

        userRepository.save(entity);
    }
}