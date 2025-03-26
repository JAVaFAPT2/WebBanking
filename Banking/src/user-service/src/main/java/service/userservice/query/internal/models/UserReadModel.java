package service.userservice.query.internal.models;

import com.netflix.discovery.converters.Converters;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import service.shared.models.BaseEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_read")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserReadModel extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String username;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false, length = 100)
    private String email;
    @Column(nullable = false, length = 100)
    private String fullName;
    @Column(nullable = false, length = 100)
    private String phoneNumber;
    @Column(nullable = false)
    private String roles;
    @Column(nullable = false, length = 100)
    private Date createdAt;
    @Column(nullable = false, length = 100)
    private Date updatedAt;
}
