package com.fpt.project.service;

import com.fpt.project.dto.request.MessageRequestDto;
import com.fpt.project.dto.response.ChatGroupDetailResponseDto;
import com.fpt.project.dto.response.MessageResponseDto;
import com.fpt.project.exception.ApiException;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.List;

public interface MessageService {
    void sendMessage(MessageRequestDto data) throws FirebaseMessagingException, ApiException;
    ChatGroupDetailResponseDto getMessagesByChatGroupId(int chatGroupId) throws ApiException;
    int countNewMessages();
}
