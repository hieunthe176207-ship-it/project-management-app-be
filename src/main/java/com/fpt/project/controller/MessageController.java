package com.fpt.project.controller;

import com.fpt.project.dto.request.ChatMessage;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Slf4j
@Controller
@RequiredArgsConstructor

public class MessageController {

    private final SecurityUtil securityUtil;


    @MessageMapping("/chat")
    @SendTo("/topic/public")
    public ChatMessage handleChat(ChatMessage msg) {
        System.out.println(msg.getContent());
        return ChatMessage.builder()
                .sender(msg.getSender())
                .content(msg.getContent())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
