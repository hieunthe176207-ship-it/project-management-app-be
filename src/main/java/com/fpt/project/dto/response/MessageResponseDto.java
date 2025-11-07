package com.fpt.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponseDto {
    public Integer id;
    public Integer senderId;
    public String senderEmail;
    public String senderName;
    public String avatarUrl;
    public String content;
    public int groupId;
    public String timestamp;
}
