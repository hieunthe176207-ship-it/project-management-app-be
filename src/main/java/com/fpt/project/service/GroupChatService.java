package com.fpt.project.service;

import com.fpt.project.dto.response.ChatGroupResponse;

import java.util.List;

public interface GroupChatService{
    List<ChatGroupResponse> getGroupChatsByUserId();
    void markMessagesAsRead(Integer groupId);
}
