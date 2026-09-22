package kaguya.user.domain.user.model.entity;

import jakarta.persistence.*;
import kaguya.user.domain.user.model.enums.ManagementType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_management")
public class UserManagementEntity {

    @Id
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx",  nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_idx",  nullable = false)
    private UserEntity admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "management_type",  nullable = false)
    private ManagementType managementType;

    @Column(nullable = false)
    private String reason;

    @Column(name = "start_date",  nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "create_at",  nullable = false)
    private LocalDateTime createdAt;


    @Builder
    public UserManagementEntity(UserEntity user, UserEntity admin, ManagementType managementType, String reason, LocalDateTime endDate) {
        this.user = user;
        this.admin = admin;
        this.managementType = managementType;
        this.reason = reason;
        this.startDate = LocalDateTime.now();
        this.endDate = endDate;
    }

    /*
    idx (PK)
    user_idx (FK)
    admin_idx (제제를 가한 관리자 idx)
    management_type (제제 종류 - 임시차단, 영구차단)
    reason (제제 이유)
    start_date
    end_date
    created_at
*/

}
