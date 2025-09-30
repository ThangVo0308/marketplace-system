package com.example.marketplace.entities;

import com.example.marketplace.enums.Gender;
import com.example.marketplace.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User extends AbstractEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    String email;

    @Column(nullable = false)
    String password;

    @Column(name = "is_activated", nullable = false)
    boolean isActivated = false;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    String username;

    @Column(name = "first_name", nullable = false, length = 50)
    String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    String lastName;

    @Column(name = "middle_name", length = 50)
    String middleName;

    @Column(name = "mobile_number", length = 20)
    String mobileNumber;

    @Column(name = "birthdate", nullable = false)
    LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    Gender gender;

    @ManyToOne
    @JsonManagedReference
    Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    FileMetadata avatar;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Product> productList;

}
