package service.circuitbreakerservice.DTo;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private UUID userId;
    private Throwable exception;
}
