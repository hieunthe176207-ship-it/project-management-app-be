package com.fpt.project.controller;

import com.fpt.project.dto.response.ResponseSuccess;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/notification")
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my-notifications")
    public ResponseEntity<?> getMyNotifications() throws ApiException {
        // Implementation to get notifications for the current user
        return ResponseEntity.ok(ResponseSuccess.<Object>builder()
                .status(200)
                .message("Notifications retrieved successfully")
                .data(notificationService.getNotificationsForUser())
                .build());
    }
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadNotificationCount() throws ApiException {
        int count = notificationService.getUnreadNotificationCount();
        return ResponseEntity.ok(ResponseSuccess.<Integer>builder()
                .status(200)
                .message("Unread notification count retrieved successfully")
                .data(count)
                .build());
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<?> markAllNotificationsAsRead() throws ApiException {
        notificationService.markAllNotificationsAsRead();
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .status(200)
                .message("All notifications marked as read successfully")
                .build());
    }
}
