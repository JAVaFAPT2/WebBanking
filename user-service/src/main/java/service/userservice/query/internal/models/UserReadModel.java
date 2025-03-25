package service.userservice.query.internal.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserReadModel {
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private List<String> roles;
    private Map<String, Object> metadata;
    private Date createdAt;
    private Date updatedAt;
}
