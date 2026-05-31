package kaguya.domain.user.service;

import kaguya.domain.user.model.dto.response.MyPageRes;
import kaguya.domain.user.model.dto.UserDto;
import kaguya.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.domain.user.model.entity.UserEntity;
import kaguya.domain.user.repository.UserRepository;
import kaguya.domain.user.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 계정 정보 조회 (마이페이지)
    @Transactional(readOnly = true)
    public MyPageRes getMyPage(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        return userMapper.entityToMyPageReq(userEntity);
    }

    // 사용자 정보 조회
    @Transactional(readOnly = true)
    public UserDto getProfile(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        return userMapper.entityToUserDto(userEntity);
    }

    // 닉네임 변경
    @Transactional
    public void updateNickname(String username, UpdateNicknameReq updateNicknameData) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        if(!userEntity.getNickname().equals(updateNicknameData.nickname())) {
            if (userRepository.existsByNickname(updateNicknameData.nickname())) {
                throw new IllegalIdentifierException("이미 사용중인 닉네임입니다.");
            }
            userEntity.changeNickname(updateNicknameData.nickname());
        }
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String username, UpdatePasswordReq updatePasswordData) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(updatePasswordData.currentPassword(), userEntity.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(updatePasswordData.newPassword(), userEntity.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(updatePasswordData.newPassword());
        userEntity.changePassword(encodedPassword);
    }

    // 회원탈퇴
    @Transactional
    public void withdraw(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        userRepository.delete(userEntity);
    }
}
