package kaguya.user.domain.user.service;

import kaguya.user.domain.user.model.enums.ManagementType;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.model.enums.Status;
import kaguya.user.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserStatusService {

    /**
     * 스케쥴링(UserStatusScheduler) 작업
     * - N개월 이상 접속하지 않은 유저 휴면계정으로 변경
     * - 임시 정지된 유저 활성화
     */

    // 마지막 로그인 후 휴면 전환까지의 기간 (3개월)
    private static final long DORMANT_MONTHS = 3;

    private final UserRepository userRepository;

    // 휴면 전환
    @Transactional
    public int convertDormantUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(DORMANT_MONTHS);
        return userRepository.convertToDormant(Status.ACTIVE, Status.DORMANT, Role.ADMIN, cutoff);
    }

    // 정지 해제 (임시 정지 기간이 끝난 유저 활성화)
    @Transactional
    public int releaseSuspendedUsers() {
        return userRepository.releaseSuspended(Status.SUSPENDED, Status.ACTIVE, ManagementType.TEMP_BAN, LocalDateTime.now());
    }
}
