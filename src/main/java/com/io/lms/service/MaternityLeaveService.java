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

import static com.io.lms.constant.Constants.MATERNITY;

@Service
@Transactional
@Slf4j
public class MaternityLeaveService implements LeaveService {
    private static final Long MAX_TIME_LEAVE_CAN_BE_AVAILED = 2L;
    private static final Long DAYS_SHOULD_HAVE_SERVED = 80L;
    private static final Long MAX_LEAVES_APPLICABLE_AT_A_TIME = Long.valueOf(26 * 7);
    @Autowired
    EmployeeService employeeService;
    @Autowired
    LeaveRepository leaveRepository;

    @Override
    public String getType() {
        return MATERNITY;
    }

    @Override
    public LeaveResponse enterNewLeaveRequest(LeaveRequest leaveRequest, Long empId) throws ParseException {
        log.debug("Inside enterNewLeaveRequest() of MaternityLeaveService class");
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
        log.debug("Inside getLeaveBalance() of MaternityLeaveService class");
        return MAX_LEAVES_APPLICABLE_AT_A_TIME;
    }

    @Override
    public Leave mapFromLeaveRequestToLeave(LeaveRequest leaveRequest) throws ParseException {
        log.debug("Inside mapFromDtoLeaveRequestToLeave() of MaternityLeaveService class");
        Long empId = leaveRequest.getEmpId();
        Date startDate = Utility.stringToDate(leaveRequest.getStartDate());
        Date endDate = Utility.stringToDate(leaveRequest.getEndDate());
        Long leaveDemand = Utility.getDaysCount(startDate, endDate);
        if (!isLeaveDemandedAvailable(leaveDemand, empId)) {
            throw new LeaveConstraintFailException("Leave demanded is greater than total leave available, Leave balance is lesser");
        }
        Leave leave = new Leave();
        Employee emp = new Employee();
        emp.setId(empId);
        leave.setEmployee(emp);
        leave.setLeaveCount(leaveDemand);
        leave.setLeaveType(leaveRequest.getLeaveType());
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        return leave;
    }

    @Override
    public boolean validateLeaveRequest(LeaveRequest leaveRequest) throws ParseException {
        Long empId = leaveRequest.getEmpId();
        Employee employee = employeeService.findEmployeeById(empId);
        List<Leave> leaves = leaveRepository.findAllByEmployeeId(empId);
        String gender = String.valueOf(employee.getGender()).toLowerCase();
        if (!Utility.validateGender(gender, "female")) {
            log.error("Employee is not female, hence cannot apply for maternity leave");
            throw new LeaveConstraintFailException("Employee is not female");
        }
        if (Utility.validateLeaveRequestCommonConstraints(leaveRequest, employee)) {
            log.debug("Leave Request common Validations successful");
        }
        Long count = leaves.stream().filter((leave) -> leave.getLeaveType().toLowerCase().equals("maternity")).count();
        if (count >= MAX_TIME_LEAVE_CAN_BE_AVAILED) {
            log.error("Maternity leave cannot be granted more than 2 times");
            throw new LeaveConstraintFailException("maternity leave cannot be granted more than 2 times");
        }
        if (validateNumOfDaysOfDuty(leaveRequest, employee)) {
            log.debug("Num of days of duty constraint is validated successfully!!");
        } else {
            throw new LeaveConstraintFailException("Working days are less than " + DAYS_SHOULD_HAVE_SERVED + " days preceding 12 months from expected delivery date");
        }
        return true;
    }

    @Override
    public Long getNetLeaveCount(Date startDate, Date endDate) {
        log.debug("Inside getNetLeaveCount() of MaternityLeaveService class");
        return Utility.getDaysCount(startDate, endDate);
    }


    @Override
    public boolean isLeaveDemandedAvailable(Long leaveDemand, Long empId) {
        log.debug("Inside isLeaveDemandedAvailable() of MaternityLeaveService class");
        Long leaveAvailable = getLeaveBalance(empId);
        if (leaveDemand > leaveAvailable) {
            log.error("Leave demanded is greater than total leave available, Leave balance is lesser");
            return false;
        }
        return true;
    }

    private Long getTotalLeavesTakenInRange(Long empId, Date startDate, Date endDate) throws ParseException {
        log.debug("Inside getTotalLeavesTaken() of MaternityLeaveService class");
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStartDate(Utility.dateToString(startDate));
        leaveRequest.setEndDate(Utility.dateToString(endDate));
        List<LeaveResponse> leaveResponses = employeeService.getDateSpecificLeaveHistory(empId, leaveRequest);
        Long totalLeaveTaken = leaveResponses.stream().map((leaveResponse) -> leaveResponse.getLeaveCount()).reduce(0L, Long::sum);
        return totalLeaveTaken;
    }

    private boolean validateNumOfDaysOfDuty(LeaveRequest leaveRequest, Employee employee) throws ParseException {
        log.debug("Inside validateNumOfDaysOfDuty() of MaternityLeaveService class");
        Date startDate = Utility.stringToDate(leaveRequest.getStartDate());
        Date currDate = Utility.getCurrentDate();
        Long empId = leaveRequest.getEmpId();
        Date expectedDeliveryDate = Utility.stringToDate(leaveRequest.getExpectedDeliveryDate());
        if (expectedDeliveryDate.getTime() < startDate.getTime()) {
            throw new LeaveConstraintFailException("Expected Date of delivery is before the leave start date");
        }
        Date dateOneYearPriorToDelivery = Utility.getDateOneYearPrior(expectedDeliveryDate);
        Date dateOfJoining = employee.getJoiningDate();
        Date d1 = (dateOfJoining.getTime() > dateOneYearPriorToDelivery.getTime()) ? dateOfJoining : dateOneYearPriorToDelivery;
        Date d2 = (startDate.getTime() < currDate.getTime()) ? startDate : currDate;
        Long totalWorkingDays = Utility.getTotalWorkingDays(d1, d2);
        Long totalLeavesTaken = getTotalLeavesTakenInRange(empId, d1, d2);
        Long totalDaysServed = totalWorkingDays - totalLeavesTaken;
        if (totalDaysServed < DAYS_SHOULD_HAVE_SERVED) {
            log.error("Num of days of duty constraint validation Failed");
            return false;
        }
        return true;
    }

}
