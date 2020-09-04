package com.io.lms.service;

import com.io.lms.dto.LeaveRequest;
import com.io.lms.dto.LeaveResponse;
import com.io.lms.model.Leave;

import java.text.ParseException;
import java.util.Date;

public interface LeaveService {
    String getType();

    LeaveResponse enterNewLeaveRequest(LeaveRequest leaveRequest, Long empId) throws ParseException;

    Long getLeaveBalance(Long empId) throws ParseException;

    default Long calculateTotalLeave(Long empId) throws ParseException {
        return 0L;
    }

    Leave mapFromLeaveRequestToLeave(LeaveRequest leaveRequest) throws ParseException;

    boolean validateLeaveRequest(LeaveRequest leaveRequest) throws ParseException;

    Long getNetLeaveCount(Date startDate, Date endDate) throws ParseException;

    boolean isLeaveDemandedAvailable(Long leaveDemand, Long empId) throws ParseException;
}
