package com.io.lms.service;

import com.io.lms.dto.LeaveRequest;
import com.io.lms.dto.LeaveResponse;
import com.io.lms.exception.LeaveConstraintFailException;
import com.io.lms.model.Employee;
import com.io.lms.model.Gender;
import com.io.lms.repository.EmployeeRepository;
import com.io.lms.repository.LeaveRepository;
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

import static com.io.lms.constant.Constants.OutOfOffice;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class OutOfOfficeLeaveServiceTests {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    LeaveRepository leaveRepository;
    @Autowired
    OutOfOfficeLeaveService outOfOfficeLeaveService;

    private Employee getEmployeeForTesting() throws ParseException {
        Employee employee = new Employee();

        employee.setName("Prashant Agrawal");
        employee.setEmail("a,prashant2020@gmail.com");
        employee.setGender(Gender.MALE);
        employee.setJoiningDate(Utility.getCurrentDate());

        return employee;
    }

    private LeaveRequest getLeaveRequestForTesting() throws ParseException {
        LeaveRequest leaveRequest = new LeaveRequest();

        Date currDate = Utility.getCurrentDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date startDate = calendar.getTime();
        calendar.setTime(currDate);
        calendar.add(Calendar.DAY_OF_YEAR, 10);
        Date endDate = calendar.getTime();
        leaveRequest.setLeaveType(OutOfOffice);
        leaveRequest.setStartDate(Utility.dateToString(startDate));
        leaveRequest.setEndDate(Utility.dateToString(endDate));

        return leaveRequest;
    }


    @Test
    public void enterNewLeaveRequestTest() throws ParseException {

        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);
        LeaveRequest leaveRequestForTesting = getLeaveRequestForTesting();
        leaveRequestForTesting.setEmpId(employee.getId());
        Date startDate = Utility.stringToDate(leaveRequestForTesting.getStartDate());
        Date endDate = Utility.stringToDate(leaveRequestForTesting.getEndDate());
        Long leaveCountExpected = outOfOfficeLeaveService.getNetLeaveCount(startDate, endDate);
        LeaveResponse leaveResponse = outOfOfficeLeaveService.enterNewLeaveRequest(leaveRequestForTesting, employee.getId());
        leaveRepository.deleteById(leaveResponse.getLeaveId());
        employeeRepository.deleteById(employee.getId());
        assertEquals(leaveRequestForTesting.getStartDate(), leaveResponse.getStartDate());
        assertEquals(leaveRequestForTesting.getEndDate(), leaveResponse.getEndDate());
        assertEquals(leaveRequestForTesting.getLeaveType(), leaveResponse.getLeaveType());
        assertEquals(leaveCountExpected, leaveResponse.getLeaveCount());

    }

    @Test(expected = LeaveConstraintFailException.class)
    public void enterNewLeaveRequestWithMaxDaysLeaveConstraintFailTest() throws ParseException {

        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);
        LeaveRequest leaveRequestForTesting = getLeaveRequestForTesting();
        leaveRequestForTesting.setEmpId(employee.getId());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utility.stringToDate(leaveRequestForTesting.getStartDate()));
        calendar.add(Calendar.DAY_OF_YEAR, 20);
        Date endDate = calendar.getTime();
        leaveRequestForTesting.setEndDate(Utility.dateToString(endDate));
        LeaveResponse leaveResponse = outOfOfficeLeaveService.enterNewLeaveRequest(leaveRequestForTesting, employee.getId());
        leaveRepository.deleteById(leaveResponse.getLeaveId());
        employeeRepository.deleteById(employee.getId());

    }

    @Test
    public void getLeaveBalanceTest() throws ParseException {

        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);
        Long leaveBalanceActual = outOfOfficeLeaveService.getLeaveBalance(employee.getId());
        Long leaveBalanceExpected = outOfOfficeLeaveService.calculateTotalLeave(employee.getId());
        employeeRepository.deleteById(employee.getId());
        assertEquals(leaveBalanceExpected, leaveBalanceActual);

    }
}
