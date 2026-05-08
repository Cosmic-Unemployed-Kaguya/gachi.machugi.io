package kaguya.domain.user.model.entity;

import jakarta.persistence.*;
import kaguya.domain.user.model.enums.Gender;
import kaguya.domain.user.model.enums.Role;
import kaguya.domain.user.model.enums.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "idx")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userIdx;

    @Column(name = "id", nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    private String name;
    private LocalDate birth;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(name = "withdrawal_date")
    private LocalDateTime withdrawalDate;

    @CreationTimestamp
    @Column(name = "join_date", nullable = false, updatable = false)
    private LocalDateTime joinDate;

    @UpdateTimestamp
    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @Builder
    public UserEntity(String username, String password, String nickname, String email, String name, LocalDate birth, String phone, Gender gender) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.name = name;
        this.birth = birth;
        this.phone = phone;
        this.gender = gender;
    }

    // User
    public void changePassword(String password) {this.password = password;}
    public void changeNickname(String nickname) {this.nickname = nickname;}

    // Admin
    public void changeRole(Role role) {this.role = role;}
    public void changeStatus(Status status) {this.status = status;}
}
