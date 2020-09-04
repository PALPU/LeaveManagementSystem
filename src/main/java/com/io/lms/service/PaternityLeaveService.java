package com.io.lms.service;

import com.io.lms.dto.LeaveRequest;
import com.io.lms.dto.LeaveResponse;
import com.io.lms.exception.LeaveConstraintFailException;
import com.io.lms.model.Employee;
import com.io.lms.model.Leave;
import com.io.lms.repository.LeaveRepository;
import com.io.lms.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static com.io.lms.constant.Constants.*;

@Service
@Transactional
@Slf4j
public class PaternityLeaveService implements LeaveService {
    private static final Long MAX_LEAVES_APPLICABLE_AT_A_TIME = 10L;
    private static final Long MAX_TIME_LEAVE_CAN_BE_AVAILED = 2L;
    @Autowired
    LeaveRepository leaveRepository;
    @Autowired
    EmployeeService employeeService;

    @Override
    public String getType() {
        return PATERNITY;
    }

    @Override
    public LeaveResponse enterNewLeaveRequest(LeaveRequest leaveRequest, Long empId) throws ParseException {
        log.debug("Inside enterNewLeaveRequest() of PaternityLeaveService class");
        try {
            leaveRequest.setEmpId(empId);
            Leave leave = null;
            if (validateLeaveRequest(leaveRequest)) {
                leave = leaveRepository.save(mapFromLeaveRequestToLeave(leaveRequest));
            }
            return Utility.mapFromLeaveToLeaveResponse(leave);
        } catch (ParseException e) {
            throw e;
        }

    }

    @Override
    public Long getLeaveBalance(Long empId) {
        log.debug("Inside getLeaveBalance() of PaternityLeaveService class");
        return MAX_LEAVES_APPLICABLE_AT_A_TIME;
    }

    @Override
    public Leave mapFromLeaveRequestToLeave(LeaveRequest leaveRequest) throws ParseException {
        log.debug("Inside mapFromDtoLeaveRequestToLeave() of PaternityLeaveService class");
        Long empId = leaveRequest.getEmpId();
        Date startDate = Utility.stringToDate(leaveRequest.getStartDate());
        Date endDate = Utility.stringToDate(leaveRequest.getEndDate());
        Long leaveDemand = getNetLeaveCount(startDate, endDate);
        if (!isLeaveDemandedAvailable(leaveDemand, empId)) {
            throw new LeaveConstraintFailException("Leave demanded is greater than total leave available, Leave balance is lesser");
        }
        Leave leave = new Leave();
        Employee emp = new Employee();
        emp.setId(leaveRequest.getEmpId());
        leave.setEmployee(emp);
        leave.setLeaveType(leaveRequest.getLeaveType());
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setLeaveCount(leaveDemand);
        return leave;
    }

    @Override
    public boolean validateLeaveRequest(LeaveRequest leaveRequest) throws ParseException {
        log.debug("Inside validateLeaveRequest() of PaternityLeaveService class");
        Long empId = leaveRequest.getEmpId();
        Employee employee = employeeService.findEmployeeById(empId);
        String gender = String.valueOf(employee.getGender()).toLowerCase();
        if (!Utility.validateGender(gender, MALE)) {
            throw new LeaveConstraintFailException("Employee is not " + MALE);
        }
        List<Leave> leaves = leaveRepository.findAllByEmployeeId(empId);
        if (Utility.validateLeaveRequestCommonConstraints(leaveRequest, employee)) {
            log.debug("Leave Request common Validations successful");
        }
        Long count = leaves.stream().filter((leave) -> leave.getLeaveType().toLowerCase().equals("paternity")).count();
        if (count >= MAX_TIME_LEAVE_CAN_BE_AVAILED) {
            log.error("paternity leave cannot be granted more than 2 times");
            throw new LeaveConstraintFailException("paternity leave cannot be granted more than 2 times");
        }
        Date startDate = Utility.stringToDate(leaveRequest.getStartDate());
        Date endDate = Utility.stringToDate(leaveRequest.getEndDate());
        if (!validateChildBirthConstraint(startDate, endDate, Utility.stringToDate(leaveRequest.getChildDOB()))) {
            log.error("Child's D.O.B constraint is not matching");
            throw new LeaveConstraintFailException("Child's D.O.B constraint is not matching");
        }
        return true;
    }

    @Override
    public Long getNetLeaveCount(Date startDate, Date endDate) throws ParseException {
        log.debug("Inside getNetLeaveCount() of PaternityLeaveService class");
        Long daysCount = Utility.getDaysCount(startDate, endDate);

        Long holidays = Utility.numOfHolidaysInRange(startDate, endDate);
        Long nonWorkingDays = Utility.numOfNonWorkingDayInRange(startDate, endDate);
        Long holidaysAndNonWorkingDays = Utility.numOfHolidaysAndNonWorkingDaysInRange(startDate, endDate);

        Long daysNotCounted = holidays + nonWorkingDays - holidaysAndNonWorkingDays;
        if (daysNotCounted.equals(daysCount)) {
            log.error("All days in leave request is either a holiday or a non-working-day");
            throw new LeaveConstraintFailException("All days in leave request is either a holiday or a non-working-day");
        }
        return daysCount - daysNotCounted;
    }


    @Override
    public boolean isLeaveDemandedAvailable(Long leaveDemand, Long empId) throws ParseException {
        log.debug("Inside getNetLeaveCount() of PaternityLeaveService class");
        Long leaveAvailable = getLeaveBalance(empId);
        if (leaveDemand > leaveAvailable) {
            log.error("Leave demanded is greater than total leave available, Leave balance is lesser");
            return false;
        }
        return true;
    }

    private boolean validateChildBirthConstraint(Date startDate, Date endDate, Date childDOB) {
        log.debug("Inside validateChildBirthConstraint() of PaternityLeaveService class");
        return (startDate.getTime() >= childDOB.getTime()) && ((endDate.getTime() - childDOB.getTime()) / MILLI_SEC_PER_DAY <= DAYS_IN_A_YEAR);
    }
}
