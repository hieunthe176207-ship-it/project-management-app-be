
package com.fpt.project.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class TaskUpdateAssigneesRequest {
    private List<Integer> assigneeIds;
}