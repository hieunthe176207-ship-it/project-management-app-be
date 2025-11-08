package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.ChatMessage;
import com.fpt.project.dto.request.MessageRequestDto;
import com.fpt.project.dto.response.GroupUpdateEvent;
import com.fpt.project.dto.response.MessageResponseDto;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.ChatGroupRepository;
import com.fpt.project.repository.ProjectMemberRepository;
import com.fpt.project.service.MessageService;
import com.fpt.project.util.SecurityUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;



    @MessageMapping("/chat")
    public void handleChat(MessageRequestDto data) throws FirebaseMessagingException, ApiException {
        messageService.sendMessage(data);

    }

    @GetMapping("/get-all/{id}")
    public ResponseEntity<?> getAllMessages(@PathVariable int id) throws ApiException {
        return ResponseEntity.ok(ResponseSuccess.builder()
                .data(messageService.getMessagesByChatGroupId(id))
                        .message("Get messages successfully")
                        .code(200)
                .build());
    }


    @GetMapping("/count-new-messsages")
    public ResponseEntity<?> countNewMessages() {
        return ResponseEntity.ok(ResponseSuccess.builder()
                .data(messageService.countNewMessages())
                .message("Count new messages successfully")
                .code(200)
                .build());
    }


}
