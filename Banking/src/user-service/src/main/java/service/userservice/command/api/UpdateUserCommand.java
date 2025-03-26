package service.userservice.command.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserCommand {
    @NotBlank(message = "User ID cannot be blank")
    private UUID userId;

    private String username;

    private String password;

    @Email(message = "Email should be valid")
    private String email;
}
