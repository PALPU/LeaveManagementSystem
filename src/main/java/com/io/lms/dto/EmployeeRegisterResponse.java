package com.io.lms.dto;

import com.io.lms.model.Leave;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@NoArgsConstructor
public class EmployeeRegisterResponse {
    private Long id;
    private String name;
    private String email;
    private Date dateOfJoining;
    private String gender;
    private List<Leave> leaves;

}
