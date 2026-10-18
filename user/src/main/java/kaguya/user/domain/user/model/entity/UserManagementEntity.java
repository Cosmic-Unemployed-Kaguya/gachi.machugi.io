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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
