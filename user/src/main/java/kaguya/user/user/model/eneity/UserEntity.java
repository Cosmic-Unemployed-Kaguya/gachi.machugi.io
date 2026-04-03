package kaguya.user.user.model.eneity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Table(name = "user")
public class UserEntity {

    @Id
    @Column(name = "idx")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userIdx;

    @Column(name = "id", nullable = false)
    private String id;

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
    private String gender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private String role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private String status;

    @Column(name = "withdrawal_date", nullable = false)
    private LocalDateTime withdrawalDate;

    @CreationTimestamp
    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @UpdateTimestamp
    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;
}
