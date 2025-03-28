package service.circuitbreakerservice.DTo;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {
    private UUID accountId;
}
