package service.userservice.command.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.commandhandling.model.AggregateRoot;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import service.shared.event.UserEvent;
import service.shared.exception.BankingException;
import service.shared.models.Account;
import service.shared.models.BaseEntity;
import service.shared.models.User;
import service.userservice.command.internal.models.UserWriteModel;
import service.userservice.repository.UserWriteRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static service.shared.util.BankingUtils.generateId;

@AggregateRoot
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateUserCommand {
    @AggregateIdentifier
    private UUID id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Username is required")
    private String username;

    private String role;

    public CreateUserCommand(String firstName, String lastName, String email, String password, String username, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
    }

    @CommandHandler
    public CreateUserCommand(AggregateMember aggregateMember,
                             UserWriteRepository repository,
                             PasswordEncoder passwordEncoder,
                             String firstName,
                             String lastName,
                             String email,
                             String password,
                             String username,
                             String role) {
        if (repository.findByUsername(username).isPresent()) {
            throw new BankingException("Username already exists");
        }

        if (repository.findByEmail(email).isPresent()) {
            throw new BankingException("Email already exists");
        }

        this.id = generateId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = passwordEncoder.encode(password);
        this.username = username;
        this.role = role;

        User user = new User();
        user.setId(this.id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setAccounts(Collections.emptySet());

        UserEvent userEvent = new UserEvent(
                this.id,
                "USER_CREATED",
                user,
                LocalDateTime.now(),
                Map.of("commandType", "CreateUserCommand")
        );

        AggregateLifecycle.apply(userEvent);
    }

    @EventSourcingHandler
    public void on(UserEvent event) {
        if ("USER_CREATED".equals(event.getEventType())) {
            User sourceUser = event.getUser();
            UserWriteModel user = new UserWriteModel(
                    this.id,
                    sourceUser.getEmail(),
                    sourceUser.getFirstName(),
                    sourceUser.getLastName(),
                    sourceUser.getPassword(),
                    sourceUser.getUsername(),
                    sourceUser.getRole(),
                    sourceUser.isEnabled(),
                    sourceUser.isAccountNonExpired(),
                    sourceUser.isAccountNonLocked(),
                    sourceUser.isCredentialsNonExpired(),
                    sourceUser.getAccounts()
            );
        }
    }
}
