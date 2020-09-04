package com.io.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompOffBalanceResponse {
    private Long empId;
    private Long compOffBalance;
}
