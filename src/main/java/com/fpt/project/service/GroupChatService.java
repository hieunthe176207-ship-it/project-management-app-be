package com.fpt.project.service;

import com.fpt.project.dto.request.UpdateGroupChatRequest;
import com.fpt.project.dto.response.ChatGroupResponse;
import com.fpt.project.exception.ApiException;

import java.io.IOException;
import java.util.List;

public interface GroupChatService{
    List<ChatGroupResponse> getGroupChatsByUserId();
    void markMessagesAsRead(Integer groupId);
    void updateGroupChat(UpdateGroupChatRequest data, int id) throws ApiException, IOException;
}
