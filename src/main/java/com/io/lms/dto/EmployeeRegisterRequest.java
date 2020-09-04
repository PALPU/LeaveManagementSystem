package com.io.lms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeRegisterRequest {
    private String name;
    private String email;
    private String gender;

}
