package com.io.lms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeaveResponse {
    private Long leaveId;
    private String leaveType;
    private String startDate;
    private String endDate;
    private Long leaveCount;

}
