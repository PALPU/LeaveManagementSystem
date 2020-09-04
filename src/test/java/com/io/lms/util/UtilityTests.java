package com.io.lms.util;

import com.io.lms.dto.*;
import com.io.lms.exception.LeaveConstraintFailException;
import com.io.lms.model.Employee;
import com.io.lms.model.ExtraWork;
import com.io.lms.model.Gender;
import com.io.lms.model.Leave;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.io.lms.constant.Constants.*;
import static org.junit.Assert.*;

@SpringBootTest
@Slf4j
public class UtilityTests {
    @Test
    public void mapFromLeaveToLeaveResponseTest() throws ParseException {

        Leave leave = new Leave();
        leave.setLeaveId(1L);
        Date date = Utility.getCurrentDate();
        String dateExpected = Utility.dateToString(date);
        leave.setStartDate(date);
        leave.setEndDate(date);
        leave.setLeaveType(OutOfOffice);
        leave.setLeaveCount(1L);
        LeaveResponse leaveResponse = Utility.mapFromLeaveToLeaveResponse(leave);
        assertEquals((long) leave.getLeaveId(), (long) leaveResponse.getLeaveId());
        assertEquals(dateExpected, leaveResponse.getStartDate());
        assertEquals(dateExpected, leaveResponse.getEndDate());
        assertEquals((long) leave.getLeaveCount(), (long) leaveResponse.getLeaveCount());
        assertEquals(leave.getLeaveType(), leaveResponse.getLeaveType());
    }

    @Test
    public void getDaysCountTest() throws ParseException {

        Date date = Utility.getCurrentDate();
        Long actual = Utility.getDaysCount(date, date);
        Long expected = 1L;
        assertEquals((long) expected, (long) actual);

    }

    @Test
    public void stringToDateTest() throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        String strDate = "01-01-2020";
        Date dateActual = Utility.stringToDate(strDate);
        Date dateExpected = formatter.parse(strDate);
        assertEquals(dateExpected, dateActual);

    }

    @Test
    public void dateTimeToStringTest() {
        Date date = new Date();
        String strDateActual = Utility.dateTimeToString(date);
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        String strDateExpected = formatter.format(date);
        assertEquals(strDateExpected, strDateActual);
    }

    @Test
    public void stringToDateTimeTest() throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        String strDate = "01-01-2020 01:01:00";
        Date dateActual = Utility.stringToDateTime(strDate);
        Date dateExpected = formatter.parse(strDate);
        assertEquals(dateExpected, dateActual);

    }

    @Test(expected = ParseException.class)
    public void stringToDateTimeWithWrongFormatTest() throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        String strDate = "01-01/2020 01:01:00";
        Date dateActual = Utility.stringToDateTime(strDate);
    }

    @Test
    public void dateToStringTest() {
        Date date = new Date();
        String strDateActual = Utility.dateToString(date);
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        String strDateExpected = formatter.format(date);
        assertEquals(strDateExpected, strDateActual);
    }

    @Test
    public void getCurrentDateTest() throws ParseException {

        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String s = LocalDate.now().format(myFormatObj);
        Date dateExpected = Utility.stringToDate(s);
        Date dateActual = Utility.getCurrentDate();
        assertEquals(dateExpected, dateActual);
    }

    @Test
    public void getCurrentDateWithTimeTest() {
        Date dateExpected = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        Date dateActual = Utility.getCurrentDateWithTime();
        String strExpected = formatter.format(dateExpected);
        String strActual = formatter.format(dateActual);
        assertEquals(strExpected, strActual);
    }

    @Test
    public void getDayOfWeekTest() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayExpected = calendar.get(Calendar.DAY_OF_WEEK);
        int dayActual = Utility.getDayOfWeek(date);
        assertEquals(dayExpected, dayActual);
    }

    @Test
    public void numOfNonWorkingDayInRangeTest() throws ParseException {

        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        HashSet<Integer> nonWorkingDays = holidaysAndNonWorkingDays.getNonWorkingDaysSet();
        Date startDate = Utility.stringToDate("01-01-2020");
        Date endDate = Utility.stringToDate("08-01-2020");
        Date tempDate = startDate;
        Long expectedCount = 0L;
        while (!tempDate.after(endDate)) {
            if (nonWorkingDays.contains(Utility.getDayOfWeek(tempDate))) {
                expectedCount++;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tempDate);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            tempDate = calendar.getTime();
        }
        Long actualCount = Utility.numOfNonWorkingDayInRange(startDate, endDate);
        assertEquals((long) expectedCount, (long) actualCount);

    }

    @Test(expected = ParseException.class)
    public void numOfNonWorkingDayInRangeWithWrongDateFormatTest() throws ParseException {

        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        HashSet<Integer> nonWorkingDays = holidaysAndNonWorkingDays.getNonWorkingDaysSet();
        Date startDate = Utility.stringToDate("01-01/2020");
        Date endDate = Utility.stringToDate("08-01-2020");
        Date tempDate = startDate;
        Long expectedCount = 0L;
        while (!tempDate.after(endDate)) {
            if (nonWorkingDays.contains(Utility.getDayOfWeek(tempDate))) {
                expectedCount++;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tempDate);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            tempDate = calendar.getTime();
        }
        Long actualCount = Utility.numOfNonWorkingDayInRange(startDate, endDate);

    }

    @Test
    public void numOfHolidaysInRangeTest() throws ParseException {
        log.debug("Inside numOfHolidaysInRangeTest() of Utility class");
        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        HashSet<Date> holidays = holidaysAndNonWorkingDays.getHolidaysSet();
        Date startDate = Utility.stringToDate("01-09-2020");
        Date endDate = Utility.stringToDate("15-09-2020");
        Date tempDate = startDate;
        Long expectedCount = 0L;
        while (!tempDate.after(endDate)) {
            if (holidays.contains(tempDate)) {
                expectedCount++;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tempDate);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            tempDate = calendar.getTime();
        }
        Long actualCount = Utility.numOfHolidaysInRange(startDate, endDate);
        assertEquals((long) expectedCount, (long) actualCount);

    }

    @Test
    public void numOfHolidaysAndNonWorkingDaysInRangeTest() throws ParseException {

        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        HashSet<Integer> nonWorkingDays = holidaysAndNonWorkingDays.getNonWorkingDaysSet();
        HashSet<Date> holidays = holidaysAndNonWorkingDays.getHolidaysSet();
        Date startDate = Utility.stringToDate("01-09-2020");
        Date endDate = Utility.stringToDate("15-09-2020");
        Date tempDate = startDate;
        Long expectedCount = 0L;
        while (!tempDate.after(endDate)) {
            if (holidays.contains(tempDate) && nonWorkingDays.contains(Utility.getDayOfWeek(tempDate))) {
                expectedCount++;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tempDate);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            tempDate = calendar.getTime();
        }
        Long actualCount = Utility.numOfHolidaysAndNonWorkingDaysInRange(startDate, endDate);
        assertEquals((long) expectedCount, (long) actualCount);

    }

    @Test
    public void isDateRangeOverlappingTest() throws ParseException {
        Date startDate1 = Utility.stringToDate("01-01-2020");
        Date endDate1 = Utility.stringToDate("02-01-2020");
        Date startDate2 = startDate1;
        Date endDate2 = endDate1;
        boolean actual = Utility.isDateRangeOverlapping(startDate1, endDate1, startDate2, endDate2);
        assertTrue(actual);
        startDate2 = Utility.stringToDate("03-01-2020");
        endDate2 = Utility.stringToDate("04-01-2020");
        actual = Utility.isDateRangeOverlapping(startDate1, endDate1, startDate2, endDate2);
        assertFalse(actual);
    }

    @Test
    public void isStartDateLessThanEqualToEndDateTest() throws ParseException {

        Date startDate = Utility.stringToDate("01-01-2020");
        Date endDate = Utility.stringToDate("02-01-2020");
        boolean actual = Utility.isStartDateLessThanEqualToEndDate(startDate, endDate);
        assertTrue(actual);

        startDate = Utility.stringToDate("03-01-2020");
        actual = Utility.isStartDateLessThanEqualToEndDate(startDate, endDate);
        assertFalse(actual);
    }

    @Test
    public void validateJoiningDateConstraintTest() throws ParseException {

        Date startDate = Utility.stringToDate("02-01-2020");
        Date joiningDate = Utility.stringToDate("01-01-2020");
        boolean actual = Utility.validateJoiningDateConstraint(startDate, joiningDate);
        assertTrue(actual);

        joiningDate = Utility.stringToDate("03-01-2020");
        actual = Utility.validateJoiningDateConstraint(startDate, joiningDate);
        assertFalse(actual);

    }

    @Test
    public void getTotalWorkingDaysTest() throws ParseException {
        Date date = Utility.stringToDate("19-08-2020");
        Long expected = 1L;
        Long actual = Utility.getTotalWorkingDays(date, date);
        assertEquals((long) expected, (long) actual);
    }

    @Test
    public void getDateOneYearPriorTest() throws ParseException {

        Date date = Utility.stringToDate("01-01-2020");
        Date expected = Utility.stringToDate("01-01-2019");
        Date actual = Utility.getDateOneYearPrior(date);
        assertEquals(expected, actual);

    }

    @Test
    public void mapFromEmployeeRegisterRequestToEmployeeTest() throws ParseException {

        EmployeeRegisterRequest employeeRegisterRequest = new EmployeeRegisterRequest();
        Date currDate = Utility.getCurrentDate();
        employeeRegisterRequest.setName("Prashant");
        employeeRegisterRequest.setEmail("abc@gmail.com");
        employeeRegisterRequest.setGender("male");
        Employee employee = Utility.mapFromEmployeeRegisterRequestToEmployee(employeeRegisterRequest);
        assertEquals("Prashant", employee.getName());
        assertEquals("abc@gmail.com", employee.getEmail());
        assertEquals(Gender.MALE, employee.getGender());
        assertEquals(currDate, employee.getJoiningDate());

    }

    @Test
    public void mapFromEmployeeToEmployeeResponseTest() throws ParseException {

        Employee employee = new Employee();
        List<Leave> leaveList = new ArrayList<Leave>();
        employee.setName("Prashant");
        employee.setEmail("abc@gmail.com");
        employee.setGender(Gender.MALE);
        employee.setId(1L);
        employee.setJoiningDate(Utility.getCurrentDate());
        employee.setLeaves(leaveList);
        EmployeeRegisterResponse employeeRegisterResponse = Utility.mapFromEmployeeToEmployeeResponse(employee);
        assertEquals(1, (long) employeeRegisterResponse.getId());
        assertEquals("abc@gmail.com", employeeRegisterResponse.getEmail());
        assertEquals("Prashant", employeeRegisterResponse.getName());
        assertEquals(employee.getJoiningDate(), employeeRegisterResponse.getDateOfJoining());
        assertEquals(String.valueOf(employee.getGender()).toLowerCase(), employeeRegisterResponse.getGender());
        assertEquals(leaveList, employee.getLeaves());

    }

    @Test
    public void mapFromExtraWorkToExtraWorkResponseTest() throws ParseException {

        ExtraWork extraWork = new ExtraWork();
        extraWork.setDate(Utility.getCurrentDate());
        extraWork.setExtraWorkId(1L);
        ExtraWorkResponse extraWorkResponse = Utility.mapFromExtraWorkToExtraWorkResponse(extraWork);
        assertEquals(1, (long) extraWorkResponse.getId());
        assertEquals(Utility.dateToString(Utility.getCurrentDate()), extraWorkResponse.getDate());

    }

    @Test
    public void validateGenderTest() {
        String actualGender = "male";
        String expectedGender = "female";
        boolean result = Utility.validateGender(actualGender, expectedGender);
        assertFalse(result);
        expectedGender = "male";
        result = Utility.validateGender(actualGender, expectedGender);
        assertTrue(result);
    }

    @Test
    public void dateToCalendarTest() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar expected = calendar;
        Calendar actual = Utility.dateToCalendar(date);
        assertEquals(expected, actual);
    }

    @Test
    public void getYearFromDateTest() {
        Date date = new Date();
        Calendar calendar = Utility.dateToCalendar(date);
        Long expected = Long.valueOf(calendar.get(Calendar.YEAR));
        Long actual = Utility.getYearFromDate(date);
        assertEquals(expected, actual);
    }

    @Test
    public void getMonthFromDateTest() {
        Date date = new Date();
        Calendar calendar = Utility.dateToCalendar(date);
        Long expected = Long.valueOf(calendar.get(Calendar.MONTH));
        Long actual = Utility.getMonthFromDate(date);
        assertEquals(expected, actual);
    }

    @Test
    public void getDayOfMonthFromDateTest() {
        Date date = new Date();
        Calendar calendar = Utility.dateToCalendar(date);
        Long expected = Long.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        Long actual = Utility.getDayOfMonthFromDate(date);
        assertEquals(expected, actual);
    }

    @Test
    public void isLeaveOverlappingTest() throws ParseException {

        List<Leave> leaves = new ArrayList<Leave>();
        Leave leave = new Leave();
        Date date = Utility.getCurrentDate();
        boolean actual = Utility.isLeaveOverlapping(date, date, leaves);
        assertFalse(actual);
        leave.setStartDate(date);
        leave.setEndDate(date);
        leaves.add(leave);
        actual = Utility.isLeaveOverlapping(date, date, leaves);
        assertTrue(actual);
    }

    @Test
    public void validateLeaveRequestCommonConstraintsTest() throws ParseException {
        LeaveRequest leaveRequest = new LeaveRequest();
        Employee employee = new Employee();
        String strStartDate = "20-08-2020";
        String strEndDate = "21-08-2020";

        leaveRequest.setEmpId(1L);
        leaveRequest.setStartDate(strStartDate);
        leaveRequest.setEndDate(strEndDate);
        leaveRequest.setLeaveType(OutOfOffice);
        employee.setId(1L);
        employee.setGender(Gender.MALE);
        employee.setJoiningDate(Utility.stringToDate("19-08-2020"));
        employee.setEmail("abc@gmail.com");
        employee.setName("Prashant");
        employee.setLeaves(new ArrayList<>());
        boolean actual = Utility.validateLeaveRequestCommonConstraints(leaveRequest, employee);
        assertTrue(actual);
    }

    @Test(expected = LeaveConstraintFailException.class)
    public void validateLeaveRequestCommonConstraintsWithStartDateConstraintsFailTest() throws ParseException {
        LeaveRequest leaveRequest = new LeaveRequest();
        Employee employee = new Employee();
        String strStartDate = "22-08-2020";
        String strEndDate = "21-08-2020";

        leaveRequest.setEmpId(1L);
        leaveRequest.setStartDate(strStartDate);
        leaveRequest.setEndDate(strEndDate);
        leaveRequest.setLeaveType(OutOfOffice);
        employee.setId(1L);
        employee.setGender(Gender.MALE);
        employee.setJoiningDate(Utility.stringToDate("19-08-2020"));
        employee.setEmail("abc@gmail.com");
        employee.setName("Prashant");
        employee.setLeaves(new ArrayList<>());
        boolean actual = Utility.validateLeaveRequestCommonConstraints(leaveRequest, employee);
    }

    @Test(expected = LeaveConstraintFailException.class)
    public void validateLeaveRequestCommonConstraintsWithJoiningDateConstraintsFailTest() throws ParseException {
        LeaveRequest leaveRequest = new LeaveRequest();
        Employee employee = new Employee();
        String strStartDate = "20-08-2020";
        String strEndDate = "21-08-2020";

        leaveRequest.setEmpId(1L);
        leaveRequest.setStartDate(strStartDate);
        leaveRequest.setEndDate(strEndDate);
        leaveRequest.setLeaveType(OutOfOffice);
        employee.setId(1L);
        employee.setGender(Gender.MALE);
        employee.setJoiningDate(Utility.stringToDate("22-08-2020"));
        employee.setEmail("abc@gmail.com");
        employee.setName("Prashant");
        employee.setLeaves(new ArrayList<>());
        boolean actual = Utility.validateLeaveRequestCommonConstraints(leaveRequest, employee);
    }

    @Test(expected = LeaveConstraintFailException.class)
    public void validateLeaveRequestCommonConstraintsWithLeaveOverlappingFailTest() throws ParseException {
        LeaveRequest leaveRequest = new LeaveRequest();
        Employee employee = new Employee();
        String strStartDate = "20-08-2020";
        String strEndDate = "21-08-2020";

        leaveRequest.setEmpId(1L);
        leaveRequest.setStartDate(strStartDate);
        leaveRequest.setEndDate(strEndDate);
        leaveRequest.setLeaveType(OutOfOffice);
        employee.setId(1L);
        employee.setGender(Gender.MALE);
        employee.setJoiningDate(Utility.stringToDate("22-08-2020"));
        employee.setEmail("abc@gmail.com");
        employee.setName("Prashant");
        List<Leave> leaves = new ArrayList<>();
        Leave leave = new Leave();
        Date startDate = Utility.stringToDate(strStartDate);
        Date endDate = Utility.stringToDate(strEndDate);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leaves.add(leave);
        employee.setLeaves(leaves);
        boolean actual = Utility.validateLeaveRequestCommonConstraints(leaveRequest, employee);
    }


    @Test
    public void isDateHolidaysORNonWorkingDaysTest() throws ParseException {

        Date date = Utility.stringToDate("23-08-2020");
        boolean actual = Utility.isDateHolidaysORNonWorkingDays(date);
        assertTrue(actual);

    }

    @Test
    public void isStartDateEqualsEndDateTest() throws ParseException {
        Date date = Utility.stringToDate("23-08-2020");
        boolean actual = Utility.isStartDateEqualsEndDate(date, date);
        assertTrue(actual);

    }

    @Test
    public void isCurrentDateTimeLessThanEndDateTimeTest() throws ParseException {

        Date date = Utility.stringToDate("23-08-2020");
        boolean actual = Utility.isCurrentDateTimeLessThanEndDateTime(date, date);
        assertFalse(actual);

    }

    @Test
    public void mapFromExtraWorkRequestToExtraWorkTest() throws ParseException {
        ExtraWorkRequest extraWorkRequest = new ExtraWorkRequest();
        String strStartDate = "01-01-2020 11:00:00";
        String strEndDate = "01-01-2020 20:00:00";
        extraWorkRequest.setStartDateTime(strStartDate);
        extraWorkRequest.setEndDateTime(strEndDate);
        extraWorkRequest.setEmpId(1L);
        ExtraWork extraWork = Utility.mapFromExtraWorkRequestToExtraWork(extraWorkRequest);
        assertEquals(Utility.stringToDate(strStartDate), extraWork.getDate());
        assertEquals(1, (long) extraWork.getEmployee().getId());

    }

    @Test
    public void isNumOfHoursValidForExtraWorkTest() throws ParseException {

        Date startDateTime = Utility.getCurrentDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDateTime);
        calendar.add(Calendar.HOUR_OF_DAY, 9);
        Date endDateTime = calendar.getTime();
        boolean actual = Utility.isNumOfHoursValidForExtraWork(startDateTime, endDateTime);
        assertTrue(actual);
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        endDateTime = calendar.getTime();
        actual = Utility.isNumOfHoursValidForExtraWork(startDateTime, endDateTime);
        assertFalse(actual);
    }
}
