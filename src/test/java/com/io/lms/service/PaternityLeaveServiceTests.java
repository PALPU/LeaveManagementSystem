package com.io.lms.service;

import com.io.lms.dto.LeaveRequest;
import com.io.lms.dto.LeaveResponse;
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

import static com.io.lms.constant.Constants.PATERNITY;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class PaternityLeaveServiceTests {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    LeaveRepository leaveRepository;
    @Autowired
    PaternityLeaveService paternityLeaveService;

    private Employee getEmployeeForTesting() throws ParseException {
        Employee employee = new Employee();

        employee.setName("Prashant Agrawal");
        employee.setEmail("a,prashant2020@gmail.com");
        employee.setGender(Gender.MALE);
        employee.setJoiningDate(Utility.getDateOneYearPrior(Utility.getCurrentDate()));

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
        calendar.add(Calendar.DAY_OF_YEAR, 9);
        Date endDate = calendar.getTime();
        leaveRequest.setLeaveType(PATERNITY);
        calendar.add(Calendar.DAY_OF_YEAR, -100);
        Date childDOB = calendar.getTime();
        leaveRequest.setStartDate(Utility.dateToString(startDate));
        leaveRequest.setEndDate(Utility.dateToString(endDate));
        leaveRequest.setChildDOB(Utility.dateToString(childDOB));

        return leaveRequest;
    }


    @Test
    public void enterNewLeaveRequestTest() throws ParseException {

        Employee employeeForTesting = getEmployeeForTesting();
        Employee employee = employeeRepository.save(employeeForTesting);
        LeaveRequest leaveRequestForTesting = getLeaveRequestForTesting();
        LeaveResponse leaveResponse = paternityLeaveService.enterNewLeaveRequest(leaveRequestForTesting, employee.getId());
        Long leaveCountExpected = paternityLeaveService.getNetLeaveCount(Utility.stringToDate(leaveRequestForTesting.getStartDate()), Utility.stringToDate(leaveRequestForTesting.getEndDate()));
        leaveRepository.deleteById(leaveResponse.getLeaveId());
        employeeRepository.deleteById(employee.getId());
        assertEquals(leaveRequestForTesting.getStartDate(), leaveResponse.getStartDate());
        assertEquals(leaveRequestForTesting.getEndDate(), leaveResponse.getEndDate());
        assertEquals((long) leaveCountExpected, (long) leaveResponse.getLeaveCount());
        assertEquals(PATERNITY, leaveResponse.getLeaveType());

    }

}
