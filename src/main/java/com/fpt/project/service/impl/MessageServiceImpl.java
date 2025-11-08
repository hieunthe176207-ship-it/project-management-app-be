package com.fpt.project.service.impl;

import com.fpt.project.constant.MemberStatus;
import com.fpt.project.constant.NotificationType;
import com.fpt.project.dto.request.MessageRequestDto;
import com.fpt.project.dto.response.*;
import com.fpt.project.entity.*;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.*;
import com.fpt.project.service.MessageService;
import com.fpt.project.util.SecurityUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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


    @Override
    public void sendMessage(MessageRequestDto data) throws FirebaseMessagingException, ApiException {
        User user = userRepository.findByEmail(data.getEmail());
        ChatGroup chatGroup = chatGroupRepository.findById(data.getChatGroupId())
                .orElseThrow(() -> {
                    messaging.convertAndSend("/topic/error/" + user.getId(), "Nhóm chat không tồn tại hoăc đã bị xóa");
                    return new ApiException(404, "Nhóm chat không tồn tại");
                });

        Integer projectId = chatGroupRepository.findProjectIdById(data.getChatGroupId());

        // Chặn người gửi nếu không còn trong project
        ProjectMember pm = projectMemberRepository
                .findUserByProjectIdAndUserId(projectId, user.getId());

        if (pm == null) {
            messaging.convertAndSend("/topic/error/" + user.getId(),
                    "Bạn không thuộc dự án này");
            return;
        }
        if (pm.getStatus() == MemberStatus.REMOVED) {
            messaging.convertAndSend("/topic/error/" + user.getId(),
                    "Bạn đã bị xóa khỏi dự án này");
            return;
        }

        // Lưu message
        Message message = new Message();
        message.setContent(data.getContent());
        message.setSender(user);
        message.setGroup(chatGroup);
        messageRepository.save(message);

        MessageResponseDto dto = MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderName(user.getDisplayName())
                .senderEmail(user.getEmail())
                .senderId(user.getId())
                .avatarUrl(user.getAvatar())
                .timestamp(message.getCreatedAt().toString())
                .groupId(chatGroup.getId())
                .build();

        // Phát message cho topic phòng (ai subscribe thì nhận; phía client tự decide hiển thị)
        messaging.convertAndSend("/topic/group/" + chatGroup.getId(), dto);

        // Chỉ lấy user ACTIVE để notify/FCM
        List<User> activeUsers = projectMemberRepository.findUsersByProjectId(projectId);
        GroupUpdateEvent event = GroupUpdateEvent.builder()
                .groupId(chatGroup.getId())
                .content(dto.getContent())
                .timestamp(dto.getTimestamp())
                .lastMessageId(dto.getId())
                .senderId(dto.getSenderId())
                .senderName(dto.getSenderName())
                .build();

        for (User u : activeUsers) {
            // Lưu thông báo
            // Đẩy notify qua STOMP riêng từng user
            messaging.convertAndSend("/topic/notify/message/" + u.getId(),
                    NotificationResponseDto.builder()
                            .title("Bạn có tin nhắn mới ở " + chatGroup.getName())
                            .content(dto.getSenderName() + ": " + dto.getContent())
                            .type(NotificationType.MESSAGE.toString())
                            .createdAt(LocalDateTime.now().toString())
                            .sender(UserResponse.builder()
                                    .displayName(user.getDisplayName())
                                    .email(user.getEmail())
                                    .id(user.getId())
                                    .avatar(user.getAvatar())
                                    .build())
                            .build());

            // Đẩy event group-update cho user
            messaging.convertAndSend("/topic/group-update/" + u.getId(), event);

            // FCM (tránh gửi cho chính người gửi)
            if (u.getId() != user.getId()
                    && u.getTokenFcm() != null && !u.getTokenFcm().isEmpty()) {
                firebaseService.sendToToken(
                        u.getTokenFcm(),
                        "Bạn có tin nhắn mới ở " + chatGroup.getName(),
                        dto.getSenderName() + ": " + dto.getContent(),
                        Map.of(
                                "id", String.valueOf(chatGroup.getId()),
                                "type", "MESSAGE"
                        )
                );
            }
        }

    }

    @Override
    public ChatGroupDetailResponseDto getMessagesByChatGroupId(int chatGroupId) throws ApiException {
        // 1) Lấy current user
        String email = securityUtil.getEmailRequest();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new ApiException(401, "Không xác thực được người dùng");
        }

        // 2) Lấy group & project
        ChatGroup chatGroup = chatGroupRepository.findById(chatGroupId)
                .orElseThrow(() -> new ApiException(404, "Nhóm chat không tồn tại"));

        Project project = chatGroup.getProject();
        if (project == null) {
            throw new ApiException(400, "Nhóm chat không gắn dự án");
        }

        // 3) Kiểm tra quyền: user phải là member ACTIVE của project
        ProjectMember pm = projectMemberRepository
                .findUserByProjectIdAndUserId(project.getId(), currentUser.getId());

        if (pm == null) {
            throw new ApiException(403, "Bạn không thuộc dự án này");
        }
        if (pm.getStatus() == MemberStatus.REMOVED) {
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }

        // 4) Lấy message (kèm sender) và map DTO
        // (Khuyên) dùng query fetch join để tránh N+1 (xem Cách B.2 phía dưới)
        List<Message> list = messageRepository.findByGroupId(chatGroupId);

        List<MessageResponseDto> messages = list.stream()
                .map(m -> MessageResponseDto.builder()
                        .id(m.getId())
                        .content(m.getContent())
                        .senderName(m.getSender().getDisplayName())
                        .senderEmail(m.getSender().getEmail())
                        .senderId(m.getSender().getId())
                        .avatarUrl(m.getSender().getAvatar())
                        .timestamp(m.getCreatedAt().toString())
                        .groupId(chatGroupId)
                        .build())
                .toList();

        return ChatGroupDetailResponseDto.builder()
                .id(chatGroupId)
                .name(chatGroup.getName())
                .avatar(chatGroup.getAvatar())
                .messages(messages)
                .build();
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
