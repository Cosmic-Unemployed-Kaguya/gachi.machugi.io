package kaguya.user.domain.common.model.enums;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE (1, "일반"),
    WITHDRAWAL (2, "탈퇴"),
    HUMAN (3, "휴먼"),
    SUSPENDED (4, "정지");

    private final int code;
    private final String label;

    Status(int code, String label) {
        this.code = code;
        this.label = label;
    }
}