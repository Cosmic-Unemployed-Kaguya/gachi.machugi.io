package kaguya.user.model.vo;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN(1, "관리자"),
    USER(2, "유저");

    private final int code;
    private final String label;

    Role(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
