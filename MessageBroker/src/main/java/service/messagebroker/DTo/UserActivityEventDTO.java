package service.messagebroker.DTo;

import java.util.Map;
import java.util.UUID;

public record UserActivityEventDTO(UUID userId,
                                   String activity,
                                   String userActivity,
                                   Map<String, Object> details
                                   ) {
    // No additional methods or fields needed
}
