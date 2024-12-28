package test.projectcv.service;

import test.projectcv.dto.PaymentDto;
import test.projectcv.dto.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto makePayment(PaymentDto paymentDto);
}
