package com.fpt.project.chat;

import com.fpt.project.dto.request.ChatMessage;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor

public class ChatController {

    private final SecurityUtil securityUtil;
    // Client publish tới /app/chat  -> broadcast ra /topic/public
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
