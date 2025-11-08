package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.UpdateGroupChatRequest;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @PatchMapping("/mark-read/{groupId}")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable Integer groupId) {
        System.out.println("Marking messages as read for groupId: " + groupId);
        groupChatService.markMessagesAsRead(groupId);
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Đánh dấu tin nhắn đã đọc thành công")
                .build());
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateGroupChat(@ModelAttribute UpdateGroupChatRequest data, @PathVariable Integer id) throws IOException, ApiException {
        groupChatService.updateGroupChat(data, id);
        return ResponseEntity.ok(ResponseSuccess.builder()
                        .code(200)
                        .message("Update thành công")
                .build());

    }

}
