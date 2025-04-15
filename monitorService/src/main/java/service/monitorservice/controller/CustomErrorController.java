package service.monitorservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", statusCode);
        errorDetails.put("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
        errorDetails.put("message", exception != null ? exception.getMessage() : "Unknown error");
        errorDetails.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorDetails, HttpStatus.valueOf(statusCode));
    }
}
