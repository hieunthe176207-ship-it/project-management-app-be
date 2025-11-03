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
    public int id;
    public String senderId;
    public String senderName;
    public String avatarUrl;
    public String text;
    public String timestamp;
}
