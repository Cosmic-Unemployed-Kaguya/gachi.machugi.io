package kaguya.user.global.dummy;

import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.entity.UserProfileEntity;
import kaguya.user.domain.user.model.enums.Gender;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.repository.UserProfileRepository;
import kaguya.user.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// @Profile({"dev"}) <- 따로 application.yml 분리 안해서 설정하지 않음
@Component
@RequiredArgsConstructor
public class DummyUserRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 계정 생성
        UserEntity admin = createUser("admin@test.com", "admin", "어드민A");
        admin.changeRole(Role.ADMIN);
        UserEntity user1 = createUser("user1@test.com", "1234", "유저A");
        UserEntity user2 = createUser("user2@test.com", "1234", "유저B");
        UserEntity user3 = createUser("user3@test.com", "1234", "유저C");
        userRepository.saveAll(List.of(admin, user1, user2, user3));

        // 프로필 생성
        UserProfileEntity adminProfile = createUserProfile(admin, "관리자A", LocalDate.parse("2000-01-01"), "010-0000-0000", Gender.NONE);
        UserProfileEntity user1Profile = createUserProfile(user1, "홍길동", LocalDate.parse("2001-02-02"),"010-1111-1111", Gender.MALE);
        UserProfileEntity user2Profile = createUserProfile(user2, "김철수", LocalDate.parse("2002-03-03"),"010-2222-2222", Gender.MALE);
        UserProfileEntity user3Profile = createUserProfile(user3, "최유리", LocalDate.parse("2003-04-04"),"010-3333-00333300", Gender.FEMALE);
        userProfileRepository.saveAll(List.of(adminProfile, user1Profile, user2Profile, user3Profile));
    }

    // UserEntity 생성
    private UserEntity createUser(String email, String rawPassword, String nickname) {

        // 패스워드 설정
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Entity 생성
        return UserEntity.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .point(0L)
                .build();
    }

    // UserProfileEntity 생성
    private UserProfileEntity createUserProfile(UserEntity user, String name, LocalDate birth, String phone, Gender gender) {

        Long idx = user.getIdx();
        return UserProfileEntity.builder()
                .userIdx(idx)
                .name(name)
                .birth(birth)
                .phone(phone)
                .gender(gender)
                .build();
    }
}