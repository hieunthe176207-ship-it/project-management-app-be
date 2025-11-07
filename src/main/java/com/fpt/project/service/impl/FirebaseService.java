package com.fpt.project.service.impl;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;


@Service
public class FirebaseService {

    public String sendToToken(String token, String title, String body, Map<String, String> data)
            throws FirebaseMessagingException {

        AndroidConfig android = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setTtl(Duration.ofMinutes(10).toMillis())
                .setNotification(AndroidNotification.builder()
                        .setChannelId("chat_channel") // trùng channel trên Android
                        .build())
                .build();

        Message.Builder b = Message.builder()
                .setToken(token)
                .setAndroidConfig(android)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) b.putAllData(data);

        return FirebaseMessaging.getInstance().send(b.build());
    }
}
