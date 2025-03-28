package service.accountservice.command.api;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountCommand {
    private String userName;
    private String password;
    private String emailFromUser;
}
