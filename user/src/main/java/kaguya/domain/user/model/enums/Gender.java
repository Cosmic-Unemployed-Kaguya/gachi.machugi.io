package kaguya.domain.user.model.enums;

import lombok.Getter;

@Getter
public enum Gender {
    NONE (0, "선택 안함"),
    MALE (1, "남성"),
    FEMALE (2, "여성");

    private final int code;
    private final String label;

    Gender(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static Gender fromString(String value) {
        if (value == null) return NONE;

        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}