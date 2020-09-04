package com.io.lms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeaveRequest {

    private Long empId;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String childDOB;
    private String expectedDeliveryDate;

}
