package kaguya.user.domain.user.model.entity;

import jakarta.persistence.*;
import kaguya.user.domain.common.model.enums.Role;
import kaguya.user.domain.common.model.enums.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

//    @Column(name = "id", nullable = false)
//    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(nullable = false)
    private Long point;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "withdrawal_date")
    private LocalDateTime withdrawalDate;

    @CreationTimestamp
    @Column(name = "join_date", nullable = false, updatable = false)
    private LocalDateTime joinDate;

    @UpdateTimestamp
    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @Builder
    public UserEntity(String email, String password, String nickname, Long point) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.point = (point != null) ? point : 0L;
    }

    // User
    public void withdraw() {
        this.status = Status.WITHDRAWAL;
        this.withdrawalDate = LocalDateTime.now();
    }
    public void recordLogin() {
        this.lastLoginDate = LocalDateTime.now();
    }
    public void changePassword(String password) {this.password = password;}
    public void changeNickname(String nickname) {this.nickname = nickname;}

    // Admin
    public void changeRole(Role role) {this.role = role;}
    public void changeStatus(Status status) {this.status = status;}
}
