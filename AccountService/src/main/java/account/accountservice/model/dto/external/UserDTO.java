package account.accountservice.model.dto.external;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
private long userId;
private String firstName;
private String lastName;
private String emailId;
private String password;
private String IdentificationNumber;
private String authId;
}
