package com.fpt.project.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupDetailResponseDto {
    private int id;
    private String name;
    private String avatar;
    private List<MessageResponseDto> messages;
}
