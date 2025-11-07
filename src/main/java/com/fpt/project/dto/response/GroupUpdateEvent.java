package com.fpt.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GroupUpdateEvent {
    private Integer groupId;
    private String  content;     // "Sender: content"
    private String  timestamp;   // ISO hoặc "HH:mm"
    private Integer lastMessageId;
    private Integer senderId;
    private String senderName;
}