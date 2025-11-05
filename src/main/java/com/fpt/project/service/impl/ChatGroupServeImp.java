package com.fpt.project.service.impl;

import com.fpt.project.dto.response.ChatGroupResponse;
import com.fpt.project.dto.response.MessageResponseDto;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.ChatGroup;
import com.fpt.project.entity.ProjectMember;
import com.fpt.project.entity.User;
import com.fpt.project.repository.ChatGroupRepository;
import com.fpt.project.repository.MessageRepository;
import com.fpt.project.repository.ProjectMemberRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.GroupChatService;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatGroupServeImp implements GroupChatService {
    private final ChatGroupRepository chatGroupRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public List<ChatGroupResponse> getGroupChatsByUserId() {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);

        List<ChatGroupResponse> groups = chatGroupRepository.findByUser(user.getId());
        for (ChatGroupResponse g : groups) {
            var last = messageRepository.findTopByGroup_IdOrderByIdDesc(g.getId());
            if (last != null) {
                g.setLastMessage(MessageResponseDto.builder()
                        .content(last.getContent())
                        .timestamp(last.getCreatedAt().toString())
                        .build());

                var s = last.getSender();
                g.setLastUser(UserResponse.builder()
                        .id(s.getId())
                        .displayName(s.getDisplayName())
                        .email(s.getEmail())
                        .avatar(s.getAvatar())
                        .build());
            }

            Integer projectId = chatGroupRepository.findProjectIdById(g.getId());
            Integer lastReadId = projectMemberRepository.findLastReadMessageId(projectId, user.getId());
            int maxMsgId = (last != null) ? last.getId() : 0;
            g.setHasNew(maxMsgId > (lastReadId != null ? lastReadId : 0));
        }

        // Sắp xếp theo timestamp giảm dần (message mới nhất lên đầu)
        groups.sort((g1, g2) -> {
            if (g1.getLastMessage() == null && g2.getLastMessage() == null) return 0;
            if (g1.getLastMessage() == null) return 1;
            if (g2.getLastMessage() == null) return -1;
            return g2.getLastMessage().getTimestamp().compareTo(g1.getLastMessage().getTimestamp());
        });

        return groups;
    }

    @Transactional
    @Override
    public void markMessagesAsRead(Integer groupId) {
        String email = securityUtil.getEmailRequest();
        var user = userRepository.findByEmail(email);

        // lấy projectId từ group
        Integer projectId = chatGroupRepository.findProjectIdById(groupId);


        // lấy id tin cuối của group
        Integer maxId = messageRepository.findMaxIdByGroup(groupId);
        // cập nhật mốc đã đọc
        projectMemberRepository.markProjectAsRead(projectId, user.getId(), maxId);
    }
}
