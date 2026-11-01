package kaguya.user.domain.user.model.enums;

import lombok.Getter;

@Getter
public enum SearchType {
    EMAIL(1, "이메일 검색"),
    NICKNAME(2, "닉네임 검색");

    private final int code;
    private final String label;

    SearchType(int code, String label) {
        this.code = code;
        this.label = label;
    }
}