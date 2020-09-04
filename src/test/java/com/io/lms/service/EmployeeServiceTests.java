package com.io.lms.service;

import com.io.lms.dto.*;
import com.io.lms.model.Employee;
import com.io.lms.model.Gender;
import com.io.lms.model.Leave;
import com.io.lms.repository.EmployeeRepository;
import com.io.lms.repository.ExtraWorkRepository;
import com.io.lms.repository.LeaveRepository;
import com.io.lms.util.HolidaysAndNonWorkingDays;
import com.io.lms.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.io.lms.constant.Constants.MALE;
import static com.io.lms.constant.Constants.OutOfOffice;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class EmployeeServiceTests {


    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRepository leaveRepository;
    @Autowired
    private ExtraWorkRepository extraWorkRepository;


    private Employee getEmployeeForTesting() throws ParseException {
        Employee employee = new Employee();

        employee.setName("Prashant Agrawal");
        employee.setEmail("a,prashant2020@gmail.com");
        employee.setGender(Gender.MALE);
        Date currDate = Utility.getCurrentDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        calendar.add(Calendar.YEAR, -1);
        Date dateOfJoining = calendar.getTime();//one year prior to current date
        employee.setJoiningDate(dateOfJoining);

        return employee;
    }


    private Leave getLeaveForTesting() throws ParseException {
        Leave leave = new Leave();

        Date currDate = Utility.getCurrentDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date startDate = calendar.getTime();
        calendar.setTime(currDate);
        calendar.add(Calendar.DAY_OF_YEAR, 10);
        Date endDate = calendar.getTime();
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setLeaveCount(10L);
        leave.setLeaveType(OutOfOffice);

        return leave;
    }


    @Test
    public void registerNewEmployeeTest() throws ParseException {
        log.debug("Inside testing registerNewEmployee function of EmployeeServicesTests");

        EmployeeRegisterRequest employeeRegisterRequest = new EmployeeRegisterRequest();
        employeeRegisterRequest.setEmail("a,prashant2020@gmail.com");
        employeeRegisterRequest.setGender(MALE);
        employeeRegisterRequest.setName("Prashant Agrawal");
        EmployeeRegisterResponse employeeRegisterResponse = employeeService.registerNewEmployee(employeeRegisterRequest);
        employeeRepository.deleteById(employeeRegisterResponse.getId());

        assertEquals(employeeRegisterRequest.getEmail(), employeeRegisterResponse.getEmail());
        assertEquals(employeeRegisterRequest.getGender(), employeeRegisterResponse.getGender());
        assertEquals(employeeRegisterRequest.getName(), employeeRegisterResponse.getName());
        assertEquals(null, employeeRegisterResponse.getLeaves());
        assertEquals(Utility.getCurrentDate(), employeeRegisterResponse.getDateOfJoining());


    }

    @Test
    public void getAllEmployeesTest() throws ParseException {
        Employee emp = getEmployeeForTesting();
        employeeRepository.save(emp);
        List<EmployeeRegisterResponse> employeeRegisterResponses = employeeService.getAllEmployees();
        assertEquals(1, (employeeRegisterResponses.size()));
        assertEquals(emp.getId(), employeeRegisterResponses.get(0).getId());
        assertEquals(emp.getEmail(), employeeRegisterResponses.get(0).getEmail());
        assertEquals(emp.getGender().toString().toLowerCase(), employeeRegisterResponses.get(0).getGender().toLowerCase());
        assertEquals(emp.getName(), employeeRegisterResponses.get(0).getName());
        assertEquals(emp.getJoiningDate(), employeeRegisterResponses.get(0).getDateOfJoining());
        employeeRepository.deleteById(emp.getId());
    }

    @Test
    public void getEmployeeByIdTest() throws ParseException {
        Employee employeeForTesting = getEmployeeForTesting();
        employeeRepository.save(employeeForTesting);
        EmployeeRegisterResponse employeeRegisterResponse = employeeService.getEmployeeById(employeeForTesting.getId());
        assertEquals(employeeForTesting.getId(), employeeRegisterResponse.getId());
        assertEquals(employeeForTesting.getName(), employeeRegisterResponse.getName());
        assertEquals(employeeForTesting.getEmail(), employeeRegisterResponse.getEmail());
        assertEquals(employeeForTesting.getGender().toString().toLowerCase(), employeeRegisterResponse.getGender().toLowerCase());
        assertEquals(employeeForTesting.getJoiningDate(), employeeRegisterResponse.getDateOfJoining());
        employeeRepository.deleteById(employeeForTesting.getId());
    }

    @Test
    public void getAllLeaveHistoryTest() throws ParseException {

        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);
        Leave leaveForTesting = new Leave();
        Date currDate = Utility.getCurrentDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date startDate = calendar.getTime();
        calendar.setTime(currDate);
        calendar.add(Calendar.DAY_OF_YEAR, 10);
        Date endDate = calendar.getTime();
        leaveForTesting.setLeaveType(OutOfOffice);
        leaveForTesting.setStartDate(startDate);
        leaveForTesting.setEndDate(endDate);
        leaveForTesting.setEmployee(employee);
        leaveForTesting.setLeaveCount(10L);
        Leave leave = leaveRepository.save(leaveForTesting);
        List<LeaveResponse> leaveResponses = employeeService.getAllLeaveHistory(employeeForTesting.getId());

        leaveRepository.deleteById(leave.getLeaveId());
        employeeRepository.deleteById(employee.getId());

        assertEquals(leave.getLeaveId(), leaveResponses.get(0).getLeaveId());
        assertEquals(10, (long) leaveResponses.get(0).getLeaveCount());
        assertEquals(Utility.dateToString(startDate), leaveResponses.get(0).getStartDate());
        assertEquals(Utility.dateToString(endDate), leaveResponses.get(0).getEndDate());
        assertEquals(OutOfOffice, leaveResponses.get(0).getLeaveType());


    }

    @Test
    public void getDateSpecificLeaveHistoryTest() throws ParseException {

        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);
        Leave leaveForTesting = getLeaveForTesting();
        leaveForTesting.setEmployee(employee);
        Leave leave = leaveRepository.save(leaveForTesting);
        LeaveRequest leaveRequestForTesting = new LeaveRequest();
        leaveRequestForTesting.setStartDate(Utility.dateToString(leave.getStartDate()));
        leaveRequestForTesting.setEndDate(Utility.dateToString(leave.getEndDate()));
        List<LeaveResponse> leaveResponses = employeeService.getDateSpecificLeaveHistory(employee.getId(), leaveRequestForTesting);

        leaveRepository.deleteById(leave.getLeaveId());
        employeeRepository.deleteById(employee.getId());

        assertEquals(1, leaveResponses.size());
        assertEquals(leaveForTesting.getLeaveType(), leaveResponses.get(0).getLeaveType());
        assertEquals(leave.getLeaveId(), leaveResponses.get(0).getLeaveId());
        assertEquals(leaveRequestForTesting.getStartDate(), leaveResponses.get(0).getStartDate());
        assertEquals(leaveRequestForTesting.getEndDate(), leaveResponses.get(0).getEndDate());


    }

    @Test
    public void logExtraWorkRequestTest() throws ParseException {

        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);
        HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = HolidaysAndNonWorkingDays.getInstance();
        HashSet<Integer> nonWorkingDaysSet = holidaysAndNonWorkingDays.getNonWorkingDaysSet();
        Date currDate = Utility.getCurrentDate();
        Date joiningDate = employee.getJoiningDate();
        Date tempDate = joiningDate;
        while (tempDate.before(currDate)) {
            if (nonWorkingDaysSet.contains(Utility.getDayOfWeek(tempDate))) {
                break;
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(tempDate);
                calendar.add(Calendar.DATE, 1);
                tempDate = calendar.getTime();
            }
        }
        Date startDateTime = tempDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(tempDate);
        calendar.add(Calendar.HOUR_OF_DAY, 9);
        Date endDateTime = calendar.getTime();

        ExtraWorkRequest extraWorkRequest = new ExtraWorkRequest();
        extraWorkRequest.setEmpId(employee.getId());
        extraWorkRequest.setStartDateTime(Utility.dateTimeToString(startDateTime));
        extraWorkRequest.setEndDateTime(Utility.dateTimeToString(endDateTime));
        ExtraWorkResponse extraWorkResponse = employeeService.logExtraWorkRequest(employee.getId(), extraWorkRequest);
        extraWorkRepository.deleteById(extraWorkResponse.getId());
        employeeRepository.deleteById(employee.getId());
        assertEquals(Utility.dateToString(startDateTime), extraWorkResponse.getDate());

    }

    @Test
    public void getComOffBalanceTest() throws ParseException {

        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);
        CompOffBalanceResponse compOffBalanceResponse = employeeService.getComOffBalance(employee.getId());
        employeeRepository.deleteById(employee.getId());
        assertEquals(0, (long) compOffBalanceResponse.getCompOffBalance());
        assertEquals(employee.getId(), compOffBalanceResponse.getEmpId());

    }

    @Test
    public void findEmployeeByIdTest() throws ParseException {
        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);

        Employee employeeResponse = employeeService.findEmployeeById(employee.getId());
        assertEquals(employee.getId(), employeeResponse.getId());
        assertEquals(employee.getName(), employeeResponse.getName());
        assertEquals(employee.getEmail(), employeeResponse.getEmail());
        assertEquals(employee.getJoiningDate(), employeeResponse.getJoiningDate());
        assertEquals(employee.getGender(), employeeResponse.getGender());
        employeeRepository.deleteById(employee.getId());
    }


}
