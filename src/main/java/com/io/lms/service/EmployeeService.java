package com.io.lms.service;

import com.io.lms.dto.*;
import com.io.lms.exception.DateParsingException;
import com.io.lms.exception.EmployeeNotFoundException;
import com.io.lms.exception.ExtraWorkDateTimeEligibilityException;
import com.io.lms.exception.LeaveConstraintFailException;
import com.io.lms.model.Employee;
import com.io.lms.model.ExtraWork;
import com.io.lms.model.Leave;
import com.io.lms.repository.EmployeeRepository;
import com.io.lms.repository.ExtraWorkRepository;
import com.io.lms.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.io.lms.constant.Constants.MILLI_SEC_PER_DAY;

@Service
@Transactional
@Slf4j
public class EmployeeService {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    ExtraWorkRepository extraWorkRepository;
    @Autowired
    private LeaveServicesFactory leaveServicesFactory;

    public EmployeeRegisterResponse registerNewEmployee(EmployeeRegisterRequest employeeRegisterRequest) throws ParseException {
        log.debug("Inside registerNewEmployee function of EmployeeService class");
        Employee emp = employeeRepository.save(Utility.mapFromEmployeeRegisterRequestToEmployee(employeeRegisterRequest));
        return (Utility.mapFromEmployeeToEmployeeResponse(emp));
    }


    public List<EmployeeRegisterResponse> getAllEmployees() {
        log.debug("Inside getAllEmployees function of EmployeeService class");
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream().map(Utility::mapFromEmployeeToEmployeeResponse).collect(Collectors.toList());
    }

    public EmployeeRegisterResponse getEmployeeById(Long id) {
        log.debug("Inside getEmployeeById function of EmployeeService class");
        Employee emp = findEmployeeById(id);
        return Utility.mapFromEmployeeToEmployeeResponse(emp);
    }

    public List<LeaveResponse> getAllLeaveHistory(Long empId) {
        log.debug("Inside getAllLeaveHistory function of EmployeeService class");
        Employee emp = findEmployeeById(empId);
        List<Leave> leaves = emp.getLeaves();
        List<LeaveResponse> leaveResponses = leaves.stream().map(Utility::mapFromLeaveToLeaveResponse).collect(Collectors.toList());
        return leaveResponses;
    }

    public List<LeaveResponse> getDateSpecificLeaveHistory(Long empId, LeaveRequest leaveRequest) throws ParseException {
        log.debug("Inside getDateSpecificLeaveHistory() of EmployeeService class");
        final Date startDate = Utility.stringToDate(leaveRequest.getStartDate());
        final Date endDate = Utility.stringToDate(leaveRequest.getEndDate());
        if (!Utility.isStartDateLessThanEqualToEndDate(startDate, endDate)) {
            throw new LeaveConstraintFailException("Start Date is greater than the End Date");
        }
        Employee emp = findEmployeeById(empId);
        List<Leave> leaves = emp.getLeaves();
        Predicate<Leave> isOverLapping = ((leave) -> Utility.isDateRangeOverlapping(leave.getStartDate(), leave.getEndDate(), startDate, endDate));
        Function<Leave, LeaveResponse> leaveResponseWithManipulatedDates = ((leave) -> {
            try {
                return getLeaveResponseWithManipulatedDates(leave, startDate, endDate);
            } catch (ParseException e) {
                throw new DateParsingException(e.getMessage());
            }
        });
        List<LeaveResponse> leaveResponses = leaves.stream()
                .filter(isOverLapping)
                .map(leaveResponseWithManipulatedDates).collect(Collectors.toList());
        return leaveResponses;
    }

    public ExtraWorkResponse logExtraWorkRequest(Long empId, ExtraWorkRequest extraWorkRequest) throws ParseException {
        log.debug("Inside logExtraWorkRequest() of EmployeeService class");
        extraWorkRequest.setEmpId(empId);
        if (validateExtraWorkRequest(extraWorkRequest)) {
            log.debug("Extra work log request validated successfully!!");
        }
        ExtraWork extraWork = extraWorkRepository.save(Utility.mapFromExtraWorkRequestToExtraWork(extraWorkRequest));
        return Utility.mapFromExtraWorkToExtraWorkResponse(extraWork);
    }

    private LeaveResponse getLeaveResponseWithManipulatedDates(Leave leave, Date startDate, Date endDate) throws ParseException {
        log.debug("Inside filterLeaveResponses() of EmployeeService class");
        Date dbStartDate = leave.getStartDate();
        Date dbEndDate = leave.getEndDate();
        if (startDate.getTime() >= dbStartDate.getTime() && endDate.getTime() <= dbEndDate.getTime()) {
            leave.setStartDate(startDate);
            leave.setEndDate(endDate);
        } else if (startDate.getTime() <= dbStartDate.getTime() && endDate.getTime() >= dbStartDate.getTime() && endDate.getTime() <= dbEndDate.getTime()) {
            leave.setEndDate(endDate);

        } else if (endDate.getTime() >= dbEndDate.getTime() && startDate.getTime() >= dbStartDate.getTime() && startDate.getTime() <= dbEndDate.getTime()) {
            leave.setStartDate(startDate);
        }
        leave.setLeaveCount(leaveServicesFactory.getService(leave.getLeaveType()).getNetLeaveCount(leave.getStartDate(), leave.getEndDate()));
        return Utility.mapFromLeaveToLeaveResponse(leave);
    }

    private boolean validateExtraWorkRequest(ExtraWorkRequest extraWorkRequest) throws ParseException {
        log.debug("Inside validateExtraWorkRequest() of EmployeeService class");
        Long empId = extraWorkRequest.getEmpId();
        if (validateDateEligibilityForExtraWork(extraWorkRequest.getStartDateTime(), extraWorkRequest.getEndDateTime())) {
            Employee employeeToCheck = findEmployeeById(empId);
            Date startDate = Utility.stringToDate(extraWorkRequest.getStartDateTime());
            if (!Utility.validateJoiningDateConstraint(startDate, employeeToCheck.getJoiningDate())) {
                throw new ExtraWorkDateTimeEligibilityException("Joining date greater than extra-work starting date");
            }
            List<ExtraWork> extraWorks = extraWorkRepository.findAllByEmployeeId(empId);
            Predicate<ExtraWork> isSameDatePresentInDB = (extraWork) -> Utility.dateToString(extraWork.getDate()).equals(Utility.dateToString(startDate));
            Long sameDateCount = extraWorks.stream().filter(isSameDatePresentInDB).count();
            if (sameDateCount > 0L) {
                throw new ExtraWorkDateTimeEligibilityException("Date already exists, overlapping log days");
            }
        } else {
            throw new ExtraWorkDateTimeEligibilityException("Date Eligibility failed for extra-work logging");
        }
        return true;
    }

    private boolean validateDateEligibilityForExtraWork(String startDateTime, String endDateTime) throws ParseException {
        log.debug("Inside mapFromDtoExtraWorkRequestToExtraWork() of EmployeeService class");
        Date startDateWithoutTime = Utility.stringToDate(startDateTime);
        Date endDateWithoutTime = Utility.stringToDate(endDateTime);
        Date startDateWithTime = Utility.stringToDateTime(startDateTime);
        Date endDateWithTime = Utility.stringToDateTime(endDateTime);
        Date currDateWithTime = Utility.getCurrentDateWithTime();
        //checks the date is same or not and whether the log end date is lesser than the current date
        if ((!Utility.isStartDateEqualsEndDate(startDateWithoutTime, endDateWithoutTime)) || Utility.isCurrentDateTimeLessThanEndDateTime(currDateWithTime, endDateWithTime)) {
            return false;
        }
        if (Utility.isDateHolidaysORNonWorkingDays(startDateWithoutTime)) {
            //if its lesser than 8 hrs
            return Utility.isNumOfHoursValidForExtraWork(startDateWithTime, endDateWithTime);
        } else {//if it's neither a holiday nor a non-working day
            return false;
        }
    }

    public CompOffBalanceResponse getComOffBalance(Long id) throws ParseException {
        log.debug("Inside getComOffBalance() of EmployeeService class");
        Date currentDate = Utility.getCurrentDate();
        Employee employee = findEmployeeById(id);
        List<ExtraWork> extraWorks = employee.getExtraWorks();
        Predicate<ExtraWork> validateThirtyDaysConstraint = ((extraWork) -> ((currentDate.getTime() - extraWork.getDate().getTime()) / MILLI_SEC_PER_DAY <= 30));
        Function<ExtraWork, Long> givesOneDay = (extraWork) -> 1L;
        Long balanceDays = extraWorks.stream()
                .filter(validateThirtyDaysConstraint)
                .map(givesOneDay)
                .reduce(0L, Long::sum);
        return new CompOffBalanceResponse(id, balanceDays);
    }

    public Employee findEmployeeById(Long empId) {
        log.debug("Inside findEmployeeById() of EmployeeService class");
        Employee emp = employeeRepository.findById(empId).orElseThrow(() -> new EmployeeNotFoundException("Employee id: '" + empId + "' does not exist"));
        return emp;
    }
}
