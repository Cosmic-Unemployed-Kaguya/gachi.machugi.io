package kaguya.user.domain.user.model.enums;

import lombok.Getter;

@Getter
public enum ManagementType {
    WARNING(1, "경고", null),  // 아직 사용 안함
    TEMP_BAN (2, "임시 정지", Status.SUSPENDED),
    PERM_BAN(3, "영구 정지", Status.BANNED);

    private final int code;
    private final String label;
    private final Status mappedStatus;

    ManagementType(int code, String label, Status mappedStatus) {
        this.code = code;
        this.label = label;
        this.mappedStatus = mappedStatus;
    }
}
