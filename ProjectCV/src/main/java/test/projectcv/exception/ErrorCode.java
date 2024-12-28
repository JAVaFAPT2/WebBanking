package test.projectcv.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(1000, "Uncategorized Exception", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_FAILED(1001, "Authentication failed", HttpStatus.UNAUTHORIZED),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(1008, "Token has expired", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_TOKEN(1009, "Token format is not supported", HttpStatus.FORBIDDEN),
    MALFORMED_TOKEN(1010, "Token is malformed", HttpStatus.BAD_REQUEST),
    NOT_FOUND(1012, "Not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(1013, "Not found Order", HttpStatus.NOT_FOUND),
    ALREADY_LOGIN(1011, "User already logged in", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1012, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1013, "Expired or Invalid Token", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_EXISTS(1014, "Email not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(1015,"Email have been here" ,HttpStatus.NOT_ACCEPTABLE );


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
