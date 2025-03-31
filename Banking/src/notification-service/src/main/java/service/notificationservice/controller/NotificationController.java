package service.notificationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import service.notificationservice.entity.NotificationEntity;
import service.notificationservice.service.NotificationService;
import service.shared.models.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ApiResponse<NotificationEntity> createNotification(@RequestBody NotificationEntity notification) {
        try{
            NotificationEntity createdNotification = notificationService.createNotification(notification);
            return new ApiResponse<>(true, createdNotification, "Notification created successfully", HttpStatus.CREATED);
        }catch (Exception e){
            return new ApiResponse<>(false,null,"Error creating notification",HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping
    public ApiResponse<List<NotificationEntity>> getAllNotifications() {
        try{
            List<NotificationEntity> notifications = notificationService.getAllNotifications();
            return new ApiResponse<>(true, notifications, "Notifications retrieved successfully", HttpStatus.OK);
        }catch (Exception e){
            return new ApiResponse<>(false,null,"Error retrieving notifications",HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationEntity> getNotificationById(@PathVariable UUID id) {
        NotificationEntity notification = notificationService.getNotificationById(id);
        if (notification != null) {
            return new ApiResponse<>(true,notification,"Notification retrieved successfully", HttpStatus.OK);
        } else {
            return new ApiResponse<>(true,null,"Notification not found",HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<NotificationEntity> updateNotification(@PathVariable UUID id, @RequestBody NotificationEntity notification) {
        notification.setId(id);
        NotificationEntity updatedNotification = notificationService.updateNotification(notification);
        if (updatedNotification != null) {
            return new ApiResponse<>(true,updatedNotification,"Notification updated successfully", HttpStatus.OK);
        } else {
            return new ApiResponse<>(true,null,"Notification not found",HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(@PathVariable UUID id) {
        try {
            notificationService.deleteNotification(id);
            return new ApiResponse<>(true,null,"Notification deleted successfully",HttpStatus.NO_CONTENT);
        }catch (Exception e) {
            return new ApiResponse<>(false,null,"Notification not found",HttpStatus.NOT_FOUND);
        }
    }


}
