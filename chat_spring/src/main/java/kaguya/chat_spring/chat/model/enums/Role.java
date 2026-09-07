package kaguya.chat_spring.chat.model.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN(1, "관리자"),
    USER(2, "유저"),
    GUEST(3, "게스트");

    private final int code;
    private final String label;

    Role(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
