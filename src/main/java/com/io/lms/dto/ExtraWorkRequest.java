package com.io.lms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExtraWorkRequest {
    Long empId;
    String startDateTime;
    String endDateTime;

}
