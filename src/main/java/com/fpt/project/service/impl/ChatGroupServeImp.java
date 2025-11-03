package com.fpt.project.service.impl;

import com.fpt.project.dto.response.ChatGroupResponse;
import com.fpt.project.entity.ChatGroup;
import com.fpt.project.entity.User;
import com.fpt.project.repository.ChatGroupRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.GroupChatService;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatGroupServeImp implements GroupChatService {
    private final ChatGroupRepository chatGroupRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    @Override
    public List<ChatGroupResponse> getGroupChatsByUserId() {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);

        List<ChatGroupResponse> chatGroups = chatGroupRepository.findByUser(user.getId());
        // Chuyển đổi danh sách ChatGroup thành ChatGroupResponse
        return chatGroups;
    }
}
