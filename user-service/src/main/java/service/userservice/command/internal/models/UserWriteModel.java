package service.userservice.command.internal.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import service.shared.models.Account;
import service.shared.models.BaseEntity;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_write")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserWriteModel extends BaseEntity {

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
            name = "user_write_accounts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private Set<Account> accounts;


    public UserWriteModel(UUID id, String email, String firstName, String lastName, String password, String username, String role, boolean b, boolean b1, boolean b2, boolean b3, Set<Account> accounts) {
        super(id);
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.username = username;
        this.role = role;
        this.enabled = b;
        this.accountNonExpired = b1;
        this.accountNonLocked = b2;
        this.credentialsNonExpired = b3;
        this.accounts = accounts;

    }
}
