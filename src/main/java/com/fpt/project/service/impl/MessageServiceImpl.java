package com.fpt.project.service.impl;

import com.fpt.project.constant.NotificationType;
import com.fpt.project.dto.request.MessageRequestDto;
import com.fpt.project.dto.response.*;
import com.fpt.project.entity.ChatGroup;
import com.fpt.project.entity.Message;
import com.fpt.project.entity.Notification;
import com.fpt.project.entity.User;
import com.fpt.project.repository.*;
import com.fpt.project.service.MessageService;
import com.fpt.project.util.SecurityUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final FirebaseService firebaseService;
    private final SecurityUtil securityUtil;
    private final ProjectMemberRepository projectMemberRepository;
    private final SimpMessagingTemplate messaging;
    private final NotificationRepository notificationRepository;

    @Override
    public MessageResponseDto sendMessage(MessageRequestDto data) throws FirebaseMessagingException {
        User user = userRepository.findByEmail(data.getEmail());
        ChatGroup chatGroup = chatGroupRepository.findById(data.getChatGroupId()).orElse(null);

        Message message = new Message();
        message.setContent(data.getContent());
        message.setSender(user);
        message.setGroup(chatGroup);

        messageRepository.save(message);

        MessageResponseDto messageResponseDto = MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderName(user.getDisplayName())
                .senderEmail(user.getEmail())
                .senderId(user.getId())
                .avatarUrl(user.getAvatar())
                .timestamp(message.getCreatedAt().toString())
                .build();


        Integer projectId = chatGroupRepository.findProjectIdById(data.getChatGroupId());
        List<User> users = projectMemberRepository.findUsersByProjectId(projectId);

        GroupUpdateEvent event = GroupUpdateEvent.builder()
                .groupId(data.getChatGroupId())
                .content(messageResponseDto.getContent())
                .timestamp(messageResponseDto.getTimestamp())
                .lastMessageId(messageResponseDto.getId())
                .senderId(messageResponseDto.getSenderId())
                .senderName(messageResponseDto.getSenderName())
                .build();
        messaging.convertAndSend("/topic/group/" + data.getChatGroupId(), messageResponseDto);
        for (User u : users) {
            Notification notification = new Notification();
            notification.setSender(user);
            notification.setTitle("Bạn có tin nhắn mới ở " + chatGroup.getName());
            notification.setContent(messageResponseDto.getSenderName() + ": " + messageResponseDto.getContent());
            notification.setType(NotificationType.MESSAGE);
            notification.setUser(u);
            notificationRepository.save(notification);


            messaging.convertAndSend("/topic/notify/" + u.getId(), NotificationResponseDto.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .type(notification.getType().name())
                    .createdAt(notification.getCreatedAt().toString())
                            .sender(UserResponse.builder()
                                    .displayName(user.getDisplayName())
                                    .email(user.getEmail())
                                    .id(user.getId())
                                    .avatar(user.getAvatar())
                                    .build())
                    .build()
            );

            messaging.convertAndSend("/topic/group-update/" + u.getId(), event);
            if(u.getTokenFcm() != null && !u.getTokenFcm().isEmpty() && u.getId() != user.getId()) {
                firebaseService.sendToToken(
                        u.getTokenFcm(),
                        "Bạn có tin nhắn mới ở " + chatGroup.getName(),
                        messageResponseDto.getSenderName() + ": " + messageResponseDto.getContent(),
                        Map.of(
                                "groupId", String.valueOf(data.getChatGroupId()),
                                "senderName", user.getDisplayName(),
                                "content", data.getContent()
                        )
                );
            }
        }

        return messageResponseDto;

    }

    @Override
    public ChatGroupDetailResponseDto getMessagesByChatGroupId(int chatGroupId) {
        ChatGroup chatGroup = chatGroupRepository.findById(chatGroupId).orElse(null);
        List<MessageResponseDto> messages = messageRepository.findByGroupId(chatGroupId).stream().map(message -> MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderName(message.getSender().getDisplayName())
                .senderEmail(message.getSender().getEmail())
                .senderId(message.getSender().getId())
                .avatarUrl(message.getSender().getAvatar())
                .timestamp(message.getCreatedAt().toString())
                .groupId(chatGroupId)
                .build()).toList();

        ChatGroupDetailResponseDto groupDetailResponseDto = new ChatGroupDetailResponseDto();
        groupDetailResponseDto.setId(chatGroupId);
        groupDetailResponseDto.setMessages(messages);
        groupDetailResponseDto.setName(chatGroup.getName());
        groupDetailResponseDto.setAvatar(chatGroup.getAvatar());
        return groupDetailResponseDto;
    }

    @Override
    public int countNewMessages() {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        if (user != null) {
            int count = messageRepository.countUnreadMessages(user.getId());
            return count;
        }
        return 0;
    }
}
