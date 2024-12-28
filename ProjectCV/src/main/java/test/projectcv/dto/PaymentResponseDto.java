package test.projectcv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private Long paymentId;
    private String status; // e.g., "SUCCESS", "FAILED"
    private String message; // Additional information about the payment
}