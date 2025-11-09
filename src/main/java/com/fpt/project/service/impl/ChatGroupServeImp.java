package com.fpt.project.service.impl;

import com.fpt.project.dto.request.UpdateGroupChatRequest;
import com.fpt.project.dto.response.ChatGroupResponse;
import com.fpt.project.dto.response.MessageResponseDto;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.ChatGroup;
import com.fpt.project.entity.Message;
import com.fpt.project.entity.ProjectMember;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.ChatGroupRepository;
import com.fpt.project.repository.MessageRepository;
import com.fpt.project.repository.ProjectMemberRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.GroupChatService;
import com.fpt.project.util.SecurityUtil;
import com.fpt.project.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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

        //check user is a member of project


        List<ChatGroup> groups = chatGroupRepository.findAllByUserOrderByLastMsg(user.getId());

        List<ChatGroupResponse> response = groups.stream().map(g -> {
            ChatGroupResponse grpResp = new ChatGroupResponse();
            grpResp.setId(g.getId());
            grpResp.setName(g.getName());
            grpResp.setAvatar(g.getAvatar());

            // Lấy tin nhắn cuối cùng
            Message last = messageRepository.findLastMessage(g.getId(), PageRequest.of(0, 1)).stream().findFirst().orElse(null);

            ProjectMember pm = projectMemberRepository.findUserByProjectIdAndUserId(g.getProject().getId(), user.getId());
            int lastReadId = pm.getLastReadMessageId();

            grpResp.setHasNew(last != null && last.getId() > lastReadId);
            grpResp.setLastMessage(
                    last != null ? MessageResponseDto.builder()
                            .id(last.getId())
                            .content(last.getContent())
                            .timestamp(last.getCreatedAt().toString())
                            .build() : null
            );
            grpResp.setLastUser(
                    last != null ? UserResponse.builder()
                            .id(last.getSender().getId())
                            .displayName(last.getSender().getDisplayName())
                            .avatar(last.getSender().getAvatar())
                            .build() : null
            );

            return grpResp;
        }).toList();



        return response;
    }

    @Transactional
    @Override
    public void markMessagesAsRead(Integer groupId) {
        String email = securityUtil.getEmailRequest();
        var user = userRepository.findByEmail(email);

        // lấy projectId từ group
        Integer projectId = chatGroupRepository.findProjectIdById(groupId);

        ProjectMember pm = projectMemberRepository.findUserByProjectIdAndUserId(projectId, user.getId());
        if(pm == null){
            throw new RuntimeException("Người dùng không phải thành viên của dự án");
        }


        // lấy id tin cuối của group
        Integer maxId = messageRepository.findMaxIdByGroup(groupId);
        // cập nhật mốc đã đọc
        projectMemberRepository.markProjectAsRead(projectId, user.getId(), maxId);
    }

    @Override
    public void updateGroupChat(UpdateGroupChatRequest data, int id) throws ApiException, IOException {
        ChatGroup chatGroup = chatGroupRepository.findById(id).get();
        if(chatGroup == null){
            throw new ApiException(404, "Không tìm thấy");
        }

        if(data.getAvatar() != null){
            String url =  Util.uploadImage(data.getAvatar());
            chatGroup.setAvatar(url);
        }

        if(data.getName() != null){
            chatGroup.setName(data.getName());
        }

        chatGroupRepository.save(chatGroup);
    }
}
