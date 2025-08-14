package dev.ecommerce.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue
    private long id;
    private String firstname;
    private String lastname;
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User(String firstname, String lastname, String username, String password, Role role) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
        this.role = role;
    }

}
