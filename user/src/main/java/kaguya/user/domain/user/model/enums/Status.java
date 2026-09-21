package kaguya.user.domain.user.model.enums;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE (1, "일반"),
    DORMANT (2, "휴면"),
    SUSPENDED (3, "임시정지"),
    BANNED(4, "영구정지"),
    WITHDRAWAL (5, "탈퇴");

    private final int code;
    private final String label;

    Status(int code, String label) {
        this.code = code;
        this.label = label;
    }
}