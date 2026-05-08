package kaguya.domain.user.service;

import kaguya.domain.user.model.dto.AccountDTO;
import kaguya.domain.user.model.dto.UserDTO;
import kaguya.domain.user.model.dto.request.ModifyPasswordReq;
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
    public AccountDTO getMyPage(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        return userMapper.toAccountDto(userEntity);
    }

    // 사용자 정보 조회
    @Transactional(readOnly = true)
    public UserDTO getProfile(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        return userMapper.toUserDto(userEntity);
    }

    // 회원탈퇴
    @Transactional
    public void withdraw(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        userRepository.delete(userEntity);
    }

    // 닉네임 변경
    @Transactional
    public void modifyNickname(String username, String nickname) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        if(!userEntity.getNickname().equals(nickname)) {
            if (userRepository.existsByNickname(nickname)) {
                throw new IllegalIdentifierException("이미 사용중인 닉네임입니다.");
            }
            userEntity.changeNickname(nickname);
        }
    }

    // 비밀번호 변경
    @Transactional
    public void modifyPassword(String username, ModifyPasswordReq modifyPasswordData) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(modifyPasswordData.currentPassword(), userEntity.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(modifyPasswordData.newPassword(), userEntity.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(modifyPasswordData.newPassword());
        userEntity.changePassword(encodedPassword);
    }
}
