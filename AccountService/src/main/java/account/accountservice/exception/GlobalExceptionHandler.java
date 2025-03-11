package account.accountservice.exception;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${spring.application.bad_request}")
    private String badRequest;
    @Value("${spring.application.not_found}")
    private String notFound;
    @Value("${spring.application.internal_server_error}")
    private String internalServerError;
    @Value("${spring.application.conflict}")
    private String conflict;

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        return new ResponseEntity<>(new ErrorResponse(badRequest, ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Object> handleGlobalException(GlobalException ex) {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.builder()
                        .errorCode(ex.getErrorCode())
                        .errorMessage(ex.getErrorMessage())
                        .build());
    }

}
