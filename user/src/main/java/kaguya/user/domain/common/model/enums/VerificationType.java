package kaguya.user.domain.common.model.enums;

import lombok.Getter;

@Getter
public enum VerificationType {
    FIND_ID("아이디 찾기"),
    RESET_PASSWORD("비밀번호 초기화");

    private final String label;

    VerificationType(String label) {
        this.label = label;
    }
}
