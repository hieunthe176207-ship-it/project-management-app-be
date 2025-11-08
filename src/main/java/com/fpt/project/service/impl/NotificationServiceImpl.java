package com.fpt.project.service.impl;

import com.fpt.project.dto.response.NotificationResponse;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.Notification;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.NotificationRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.NotificationService;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SecurityUtil securityUtil;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public List<NotificationResponse> getNotificationsForUser() throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return notifications.stream().map(notification -> {
            NotificationResponse response = new NotificationResponse();
            response.setId(notification.getId());
            response.setType(notification.getType().toString());
            response.setTitle(notification.getTitle());
            response.setContent(notification.getContent());
            response.setCreatedAt(notification.getCreatedAt().toString());
            response.setIsRead(notification.getIsRead());
            response.setTargetId(notification.getTargetId());
            response.setSender(UserResponse.builder()
                            .displayName(notification.getSender().getDisplayName())
                            .email(notification.getSender().getEmail())
                            .avatar(notification.getSender().getAvatar())
                    .build());
            return response;
        }).toList();
    }

    @Override
    public int getUnreadNotificationCount() throws ApiException {
            String email = securityUtil.getEmailRequest();
            User user = userRepository.findByEmail(email);
            int count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
            return count;
    }

    @Transactional
    @Override
    public void markAllNotificationsAsRead() throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        notificationRepository.markAllAsReadByUserId(user.getId());
    }
}
