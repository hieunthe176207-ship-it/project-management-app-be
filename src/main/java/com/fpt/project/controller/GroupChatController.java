package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/group-chat")
@RequiredArgsConstructor
@RestController
public class GroupChatController {
    private final GroupChatService groupChatService;
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllGroupChats() {
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Lấy danh sách nhóm chat thành công")
                .data(groupChatService.getGroupChatsByUserId())
                .build());
    }
}
