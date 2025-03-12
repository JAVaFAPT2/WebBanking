package org.fundtransfer.exception;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlobalException extends RuntimeException{

    private String errorCode;

    private String message;

    public GlobalException(String message) {
        this.message = message;
    }
}
