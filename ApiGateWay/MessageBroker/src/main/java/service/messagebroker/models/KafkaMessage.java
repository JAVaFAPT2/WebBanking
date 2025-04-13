package service.messagebroker.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KafkaMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private String messageId;
    private String source;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String subject;
    private String action;
    private Priority priority;
    private Map<String, Object> payload;
    public KafkaMessage(MessageType type, String source, String subject, String action) {
        this();
        this.type = type;
        this.source = source;
        this.subject = subject;
        this.action = action;
    }
    @Override
    public String toString() {
        return "KafkaMessage{" +
                "messageId='" + messageId + '\'' +
                ", type=" + type +
                ", source='" + source + '\'' +
                ", timestamp=" + timestamp +
                ", subject='" + subject + '\'' +
                ", action='" + action + '\'' +
                ", priority=" + priority +
                ", payload=" + payload +
                '}';
    }
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    public enum MessageType {
        TRANSACTION,
        NOTIFICATION,
        USER_ACTIVITY,
        SYSTEM_ALERT
    }

}
