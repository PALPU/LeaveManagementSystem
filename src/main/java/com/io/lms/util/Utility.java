package com.io.lms.util;

import com.io.lms.dto.*;
import com.io.lms.exception.LeaveConstraintFailException;
import com.io.lms.model.Employee;
import com.io.lms.model.ExtraWork;
import com.io.lms.model.Gender;
import com.io.lms.model.Leave;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.io.lms.constant.Constants.*;

@Slf4j
public class Utility {


    public static LeaveResponse mapFromLeaveToLeaveResponse(Leave leave) {
        log.debug("Inside mapFromLeaveToLeaveResponseDto() of Utility class");
        LeaveResponse leaveResponse = new LeaveResponse();
        leaveResponse.setLeaveId(leave.getLeaveId());
        leaveResponse.setStartDate(dateToString(leave.getStartDate()));
        leaveResponse.setEndDate(dateToString(leave.getEndDate()));
        leaveResponse.setLeaveType(leave.getLeaveType());
        leaveResponse.setLeaveCount(leave.getLeaveCount());
        return leaveResponse;
    }


    public static Long getDaysCount(Date startDate, Date endDate) {
        log.debug("Inside getDaysCount() of Utility class");
        Long totalMilliSec = endDate.getTime() - startDate.getTime();
        Long daysCount = totalMilliSec / MILLI_SEC_PER_DAY;
        return daysCount + 1;
    }

    public static Date stringToDate(String str) throws ParseException {
        log.debug("Inside stringToDate() of Utility class");
        str = str.trim();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date date = formatter.parse(str);
            return date;
        } catch (ParseException e) {
            log.error("Exception occurred while parsing the date: " + e);
            throw e;
        }
    }

    public static String dateTimeToString(Date date) {
        log.debug("Inside dateTimeToString() of Utility class");
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        String str = formatter.format(date);
        return str;
    }

    public static Date stringToDateTime(String str) throws ParseException {
        log.debug("Inside stringToDateTime() of Utility class");
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        try {
            Date date = formatter.parse(str);
            return date;
        } catch (ParseException e) {
            log.error("Exception occurred while parsing the date: " + e);
            throw e;
        }
    }

    public static String dateToString(Date date) {
        log.debug("Inside dateToString() of Utility class");
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        String str = formatter.format(date);
        return str.trim();
    }

    public static Date getCurrentDate() throws ParseException {
        log.debug("Inside getCurrentDate() of Utility class");
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String s = LocalDate.now().format(myFormatObj);
        return stringToDate(s);
    }

    public static Date getCurrentDateWithTime() {
        log.debug("Inside getCurrentDateWithTime() of Utility class");
        Date date = new Date();
        return date;
    }

    public static int getDayOfWeek(Date date) {
        log.debug("Inside getDay() of Utility class");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static Long numOfNonWorkingDayInRange(Date startDate, Date endDate) {
        log.debug("Inside numOfNonWorkingDayInRange() of Utility class");
        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        log.debug("calculating no. of non-working-days in the range: " + startDate + " to " + endDate);
        Long daysCount = 0L;
        HashSet<Integer> nonWorkingDays = holidaysAndNonWorkingDays.getNonWorkingDaysSet();
        Date current = startDate;
        while (!current.after(endDate)) {
            if (nonWorkingDays.contains(getDayOfWeek(current))) {
                daysCount++;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(current);
            calendar.add(Calendar.DATE, 1);
            current = calendar.getTime();
        }
        return daysCount;
    }

    public static Long numOfHolidaysInRange(Date startDate, Date endDate) {
        log.debug("Inside numOfHolidaysInRange() of Utility class");
        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        log.debug("calculating no. of holidays for the date range: " + startDate + " to " + endDate);
        Long daysCount = 0L;
        HashSet<Date> holidays = holidaysAndNonWorkingDays.getHolidaysSet();
        Date current = startDate;
        while (!current.after(endDate)) {
            if (holidays.contains(current)) {
                daysCount++;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(current);
            calendar.add(Calendar.DATE, 1);
            current = calendar.getTime();
        }
        return daysCount;
    }

    public static Long numOfHolidaysAndNonWorkingDaysInRange(Date startDate, Date endDate) {
        log.debug("Inside numOfHolidaysAndNonWorkingDaysInRange() of Utility class");
        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        Long daysCount = 0L;
        HashSet<Date> holidays = holidaysAndNonWorkingDays.getHolidaysSet();
        HashSet<Integer> nonWorkingDays = holidaysAndNonWorkingDays.getNonWorkingDaysSet();
        Date current = startDate;
        while (!current.after(endDate)) {
            if (holidays.contains(current) && nonWorkingDays.contains(getDayOfWeek(current))) {
                daysCount++;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(current);
            calendar.add(Calendar.DATE, 1);
            current = calendar.getTime();
        }
        return daysCount;
    }

    public static boolean isDateRangeOverlapping(Date startDate1, Date endDate1, Date startDate2, Date endDate2) {
        log.debug("Inside isDateRangeOverlapping() of Utility class");
        return (startDate2.getTime() >= startDate1.getTime() && endDate2.getTime() <= endDate1.getTime())
                || (startDate2.getTime() <= endDate1.getTime() && endDate2.getTime() >= endDate1.getTime())
                || (startDate2.getTime() <= startDate1.getTime() && endDate2.getTime() >= startDate1.getTime());
    }

    public static boolean isStartDateLessThanEqualToEndDate(Date startDate, Date endDate) {
        log.debug("Inside isStartDateLessThanEqualToEndDate() of Utility class");
        return startDate.getTime() <= endDate.getTime();
    }

    public static boolean validateJoiningDateConstraint(Date startDate, Date dateOfJoining) {
        log.debug("Inside validateJoiningDateConstraint() of Utility class");
        return startDate.getTime() >= dateOfJoining.getTime();
    }

    public static Long getTotalWorkingDays(Date startDate, Date endDate) {
        log.debug("Inside getTotalWorkingDays() of Utility class");
        Long holidaysCount = numOfHolidaysInRange(startDate, endDate);
        Long nonWorkingDays = numOfNonWorkingDayInRange(startDate, endDate);
        Long holidaysAndNonWorkingDays = numOfHolidaysAndNonWorkingDaysInRange(startDate, endDate);
        Long daysCount = getDaysCount(startDate, endDate);
        Long totalWorkingDays = daysCount - (holidaysCount + nonWorkingDays - holidaysAndNonWorkingDays);
        return totalWorkingDays;
    }

    public static Date getDateOneYearPrior(Date expectedDeliveryDate) {
        log.debug("Inside getDateOneYearPrior() of Utility class");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expectedDeliveryDate);
        calendar.add(Calendar.YEAR, -1); // to get 1 year prior Date
        Date dateOneYearPrior = calendar.getTime();
        return dateOneYearPrior;
    }


    public static Employee mapFromEmployeeRegisterRequestToEmployee(EmployeeRegisterRequest employeeRegisterRequest) throws ParseException {
        log.debug("Inside mapFromDtoToEmployee function of Utility class");
        Employee employee = new Employee();
        employee.setGender(Gender.valueOf(employeeRegisterRequest.getGender().toUpperCase()));
        employee.setEmail(employeeRegisterRequest.getEmail());
        employee.setName(employeeRegisterRequest.getName());
        employee.setJoiningDate(Utility.getCurrentDate());
        return employee;
    }

    public static EmployeeRegisterResponse mapFromEmployeeToEmployeeResponse(Employee emp) {
        log.debug("Inside mapFromEmployeeToDto function of Utility class");
        EmployeeRegisterResponse employeeRegisterResponse = new EmployeeRegisterResponse();
        employeeRegisterResponse.setId(emp.getId());
        employeeRegisterResponse.setName(emp.getName());
        employeeRegisterResponse.setEmail(emp.getEmail());
        employeeRegisterResponse.setDateOfJoining(emp.getJoiningDate());
        employeeRegisterResponse.setGender(String.valueOf(emp.getGender()).toLowerCase());
        employeeRegisterResponse.setLeaves(emp.getLeaves());
        return employeeRegisterResponse;
    }

    public static ExtraWorkResponse mapFromExtraWorkToExtraWorkResponse(ExtraWork extraWork) {
        log.debug("Inside mapFromExtraWorkToExtraWorkResponseDto() of Utility class");
        ExtraWorkResponse extraWorkResponse = new ExtraWorkResponse();
        extraWorkResponse.setId(extraWork.getExtraWorkId());
        extraWorkResponse.setDate(dateToString(extraWork.getDate()));
        return extraWorkResponse;
    }

    public static boolean validateGender(String actual, String expected) {
        log.debug("Inside validateGender() of Utility class");
        return actual.toLowerCase().equals(expected.toLowerCase());
    }

    public static Calendar dateToCalendar(Date date) {
        log.debug("Inside dateToCalendar() of Utility class");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Long getYearFromDate(Date date) {
        log.debug("Inside getYearFromDate() of Utility class");
        Calendar calendar = dateToCalendar(date);
        return Long.valueOf(calendar.get(Calendar.YEAR));
    }

    public static Long getMonthFromDate(Date date) {
        log.debug("Inside getMonthFromDate() of Utility class");
        Calendar calendar = dateToCalendar(date);
        return Long.valueOf(calendar.get(Calendar.MONTH));
    }

    public static Long getDayOfMonthFromDate(Date date) {
        log.debug("Inside getDayOfMonthFromDate() of Utility class");
        Calendar calendar = dateToCalendar(date);
        return Long.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean isLeaveOverlapping(Date startDate, Date endDate, List<Leave> leaves) {
        log.debug("Inside isLeaveOverlapping() of Utility class");
        boolean flag = false;
        for (Leave leave : leaves) {
            if (Utility.isDateRangeOverlapping(leave.getStartDate(), leave.getEndDate(), startDate, endDate)) {
                log.error("leave Date range overlaps with the previous leaves");
                flag = true;
                break;
            }
        }
        return flag;
    }

    public static boolean validateLeaveRequestCommonConstraints(LeaveRequest leaveRequest, Employee employee) throws ParseException {
        log.debug("Inside validateLeaveRequestCommonConstraints() of Utility class");
        Long empId = leaveRequest.getEmpId();
        Date startDate = Utility.stringToDate(leaveRequest.getStartDate());
        Date endDate = Utility.stringToDate(leaveRequest.getEndDate());
        if (!Utility.isStartDateLessThanEqualToEndDate(startDate, endDate)) {
            log.error("start-Date is greater than end-Date");
            throw new LeaveConstraintFailException("start-Date is greater than end-Date");
        }
        if (!Utility.validateJoiningDateConstraint(startDate, employee.getJoiningDate())) {
            log.error("Leave starting date: " + leaveRequest.getStartDate() + ", cannot be prior to joining date:  " + employee.getJoiningDate());
            throw new LeaveConstraintFailException("Leave starting date: " + leaveRequest.getStartDate() + ", cannot be prior to joining date:  " + employee.getJoiningDate());
        }
        List<Leave> leaves = employee.getLeaves();
        if (Utility.isLeaveOverlapping(startDate, endDate, leaves)) {
            throw new LeaveConstraintFailException("leave Date range overlaps with the previous leaves");
        }
        return true;
    }


    public static boolean isDateHolidaysORNonWorkingDays(Date date) {
        log.debug("Inside isDateHolidaysORNonWorkingDays() of Utility class");
        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        HashSet<Integer> nonWorkingDays = holidaysAndNonWorkingDays.getNonWorkingDaysSet();
        HashSet<Date> holidays = holidaysAndNonWorkingDays.getHolidaysSet();
        return (holidays.contains(date) || nonWorkingDays.contains(Utility.getDayOfWeek(date)));
    }

    public static boolean isStartDateEqualsEndDate(Date startDate, Date endDate) {
        log.debug("Inside isStartDateEqualsEndDate() of Utility class");
        return startDate.equals(endDate);
    }

    public static boolean isCurrentDateTimeLessThanEndDateTime(Date currentDate, Date endDate) {
        log.debug("Inside isCurrentDateTimeLessThanEndDateTime() of Utility class");
        return currentDate.getTime() < endDate.getTime();
    }

    public static ExtraWork mapFromExtraWorkRequestToExtraWork(ExtraWorkRequest extraWorkRequest) throws ParseException {
        log.debug("Inside mapFromDtoExtraWorkRequestToExtraWork() of Utility class");
        ExtraWork extraWork = new ExtraWork();
        Date startDate = Utility.stringToDate(extraWorkRequest.getStartDateTime());
        extraWork.setDate(startDate);
        Employee employee = new Employee();
        employee.setId(extraWorkRequest.getEmpId());
        extraWork.setEmployee(employee);
        return extraWork;
    }

    public static boolean isNumOfHoursValidForExtraWork(Date startDateTime, Date endDateTime) {
        log.debug("Inside isNumOfHoursValidForExtraWork() of Utility class");
        return endDateTime.getTime() - startDateTime.getTime() >= (MIN_WORKING_HOURS_REQUIRED_EXTRA_WORK * MILLI_SEC_PER_HOUR);
    }
}
