package com.fpt.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatGroupResponse {
    private Integer id;
    private String name;
    private String avatar;
    private UserResponse lastUser;
    private String lastMessage;

    public ChatGroupResponse(Integer id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }
}
