package org.loanservice.model.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "loans.contact-info")
@Component
@Getter
@Setter
public class LoansContactInfoRequest {
    private String message;
    private Map<String, String> contactDetails;
    private List<String> onCallSupport;
}
