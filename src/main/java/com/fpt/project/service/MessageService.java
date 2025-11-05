package com.fpt.project.service;

import com.fpt.project.dto.request.MessageRequestDto;
import com.fpt.project.dto.response.ChatGroupDetailResponseDto;
import com.fpt.project.dto.response.MessageResponseDto;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.List;

public interface MessageService {
    MessageResponseDto sendMessage(MessageRequestDto data) throws FirebaseMessagingException;
    ChatGroupDetailResponseDto getMessagesByChatGroupId(int chatGroupId);
    int countNewMessages();
}
