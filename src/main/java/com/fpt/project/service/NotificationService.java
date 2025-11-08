package com.fpt.project.service;

import com.fpt.project.dto.response.NotificationResponse;
import com.fpt.project.exception.ApiException;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getNotificationsForUser() throws ApiException;
    int getUnreadNotificationCount() throws ApiException;
    void markAllNotificationsAsRead() throws ApiException;
}
