package kaguya.user.domain.user.model.entity;

import jakarta.persistence.*;
import kaguya.user.domain.user.model.enums.Gender;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    private Long userIdx;  // 직접 주입

    private String name;
    private LocalDate birth;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Builder
    public UserProfileEntity (Long userIdx, String name, LocalDate birth, String phone, Gender gender) {
        this.userIdx = userIdx;
        this.name = name;
        this.birth = birth;
        this.phone = phone;
        this.gender = gender;
    }
}