package service.userservice.command.internal;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import service.shared.event.UserEvent;
import service.shared.exception.BankingException;
import service.shared.models.User;
import service.userservice.command.api.commands.CreateUserCommand;
import service.userservice.command.internal.models.UserWriteModel;
import service.userservice.repository.UserWriteRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

public class UserCommandHandler {
    private final UserWriteRepository userWriteRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public UserCommandHandler(UserWriteRepository userWriteRepository, PasswordEncoder passwordEncoder, Validator validator) {
        this.userWriteRepository = userWriteRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    @CommandHandler
    public void handleCreateUserCommand(service.userservice.command.api.commands.CreateUserCommand command) {
        // Validate command
        Set<ConstraintViolation<CreateUserCommand>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            throw new BankingException("Command validation failed: " + violations.toString());
        }

        // Check if user already exists
        if (userWriteRepository.findByUsername(command.getUsername()).isPresent()) {
            throw new BankingException("Username already exists");
        }

        if (userWriteRepository.findByEmail(command.getEmail()).isPresent()) {
            throw new BankingException("Email already exists");
        }

        // Create new user
        UserWriteModel user = new UserWriteModel(
                command.getId(),
                command.getEmail(),
                command.getFirstName(),
                command.getLastName(),
                passwordEncoder.encode(command.getPassword()),
                command.getUsername(),
                command.getRole(),
                true,
                true,
                true,
                true,
                Collections.emptySet()
        );

        // Save user to repository
        userWriteRepository.save(user);
        // Publish user created event
        User userObject = new User();
        userObject.setId(user.getId());
        userObject.setEmail(user.getEmail());
// Set other fields accordingly
        UserEvent userEvent = new UserEvent(
                user.getId(),
                "USER CREATED",
                userObject,
                LocalDateTime.now(),
                Map.of("commandType", "CreateUserCommand")
        );

        apply(userEvent);
    }

//    @CommandHandler
//    public void handleUpdateUserCommand(UpdateUserCommand command) {
//
//    }

    @EventSourcingHandler
    public void onUserCreatedEvent(UserEvent event) {
        if ("USER_CREATED".equals(event.getEventType())) {
           User user = event.getUser();
            UserWriteModel userWriteModel = new UserWriteModel(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPassword(),
                    user.getUsername(),
                    user.getRole(),
                    user.isEnabled(),
                    user.isAccountNonExpired(),
                    user.isAccountNonLocked(),
                    user.isCredentialsNonExpired(),
                    user.getAccounts()
            );
            userWriteRepository.save(userWriteModel);
        }
    }
}
