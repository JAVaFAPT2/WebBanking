package service.shared.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    private String username;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean accountNonExpired;

    @Column(nullable = false)
    private boolean accountNonLocked;

    @Column(nullable = false)
    private boolean credentialsNonExpired;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_accounts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private Set<Account> accounts;
}
