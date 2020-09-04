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

import static com.io.lms.constant.Constants.LEAVE_PER_MONTH;
import static com.io.lms.constant.Constants.OutOfOffice;

@Service
@Transactional
@Slf4j
public class OutOfOfficeLeaveService implements LeaveService {

    @Autowired
    LeaveRepository leaveRepository;
    @Autowired
    EmployeeService employeeService;

    @Override
    public String getType() {
        return OutOfOffice;
    }

    @Override
    public LeaveResponse enterNewLeaveRequest(LeaveRequest leaveRequest, Long empId) throws ParseException, LeaveConstraintFailException {

        log.debug("Inside enterNewLeaveRequest() of OutOfOfficeLeaveService class");
        try {
            Leave leave = null;
            leaveRequest.setEmpId(empId);
            if (validateLeaveRequest(leaveRequest)) {
                leave = mapFromLeaveRequestToLeave(leaveRequest);
                leave = leaveRepository.save(leave);
            }
            return Utility.mapFromLeaveToLeaveResponse(leave);
        } catch (ParseException e) {
            throw e;
        }
    }

    @Override
    public Long getLeaveBalance(Long empId) throws ParseException {
        log.debug("Inside getLeaveBalance() of OutOfOfficeLeaveService class");
        List<Leave> leaves = leaveRepository.findAllByEmployeeId(empId);
        Long leaveTaken = Long.valueOf(leaves.stream()
                .filter(leave -> leave.getLeaveType().toLowerCase().equals("ooo"))
                .map(leave -> leave.getLeaveCount())
                .reduce(0L, Long::sum));
        Long totalLeaves = calculateTotalLeave(empId);
        return totalLeaves - leaveTaken;
    }

    @Override
    public Long calculateTotalLeave(Long empId) throws ParseException {
        log.debug("Inside calculateTotalLeave() of OutOfOfficeLeaveService class");
        Employee employee = employeeService.findEmployeeById(empId);
        Date dateOfJoining = employee.getJoiningDate();
        Date currentDate = Utility.getCurrentDate();
        Long currYear = Utility.getYearFromDate(currentDate);
        Long joiningYear = Utility.getYearFromDate(dateOfJoining);
        Long joiningMonth = Utility.getMonthFromDate(dateOfJoining) + 1;// since 0 based. ex: January will give 0
        Long joiningDay = Utility.getDayOfMonthFromDate(dateOfJoining);

        Long totalMonths = (12L - joiningMonth) + (12 * (currYear - joiningYear));
        Long totalLeaves = (joiningDay > 15) ? 1L : 2L;
        totalLeaves += (LEAVE_PER_MONTH * totalMonths);
        return totalLeaves;
    }

    @Override
    public Leave mapFromLeaveRequestToLeave(LeaveRequest leaveRequest) throws ParseException {
        log.debug("Inside mapFromLeaveRequestToLeave() of OutOfOfficeLeaveService class");
        Long empId = leaveRequest.getEmpId();
        Date startDate = Utility.stringToDate(leaveRequest.getStartDate());
        Date endDate = Utility.stringToDate(leaveRequest.getEndDate());
        Long leaveDemand = getNetLeaveCount(startDate, endDate);
        if (!isLeaveDemandedAvailable(leaveDemand, empId)) {
            throw new LeaveConstraintFailException("Leave demanded is greater than total leave available, Leave balance is lesser");
        }
        Leave leave = new Leave();
        Employee emp = new Employee();
        emp.setId(empId);
        leave.setEmployee(emp);
        leave.setLeaveType(leaveRequest.getLeaveType());
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setLeaveCount(leaveDemand);
        return leave;
    }

    @Override
    public boolean validateLeaveRequest(LeaveRequest leaveRequest) throws ParseException {
        log.debug("Inside validateLeaveRequest() of OutOfOfficeLeaveService class");
        Long empId = leaveRequest.getEmpId();
        Employee employee = employeeService.findEmployeeById(empId);
        if (Utility.validateLeaveRequestCommonConstraints(leaveRequest, employee)) {
            log.debug("Leave Request common Validations successful");
            return true;
        } else {
            log.debug("Leave Request common Validations failed");
            return false;
        }
    }

    @Override
    public Long getNetLeaveCount(Date startDate, Date endDate) throws ParseException {
        log.debug("Inside getNetLeaveCount() of OutOfOfficeLeaveService class");
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
        log.debug("Inside isLeaveDemandedAvailable() of OutOfOfficeLeaveService class");
        Long leaveAvailable = getLeaveBalance(empId);
        if (leaveDemand > leaveAvailable) {
            log.error("Leave demanded is greater than total leave available, Leave balance is lesser");
            return false;
        }
        return true;
    }
}
