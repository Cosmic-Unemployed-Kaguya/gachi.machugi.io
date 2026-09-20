package kaguya.user.domain.verification.model.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum VerificationType {

    REGISTER("회원 가입"),
//    FIND_ID("아이디 찾기"),
    RESET_PASSWORD("비밀번호 초기화"),
    RELEASE_DORMANCY("휴면상태 해제");

    private final String label;

    VerificationType(String label) {
        this.label = label;
    }

    // 일치하는 Enum 반환 (없으면 null)
    public static VerificationType from(String value) {
        if (value == null) return null;

        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}