package kaguya.user.domain.user.model.enums;

import lombok.Getter;

@Getter
public enum ManagementType {
    WARNING(1, "경고"),
    TEMP_BAN (2, "임시정지"),
    PERM_BAN(3, "영구정지");

    private final int code;
    private final String label;

    ManagementType(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
