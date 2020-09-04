package com.io.lms.controller;

import com.io.lms.dto.EmployeeRegisterRequest;
import com.io.lms.dto.ExtraWorkRequest;
import com.io.lms.dto.LeaveRequest;
import com.io.lms.service.EmployeeService;
import com.io.lms.util.CustomMessageMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/register")
    public ResponseEntity registerNewEmployee(@RequestBody EmployeeRegisterRequest employeeRegisterRequest) {
        log.debug("Registering new Employee");
        try {
            return new ResponseEntity<>(employeeService.registerNewEmployee(employeeRegisterRequest), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception occurred in registering new Employee: " + e.toString());
            CustomMessageMap customMessageMap=new CustomMessageMap("Exception occurred",e.getMessage());
            return new ResponseEntity(customMessageMap.getMessageMap(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public ResponseEntity getAllEmployees() {
        return new ResponseEntity(employeeService.getAllEmployees(), HttpStatus.OK);
    }

    @GetMapping("/id/{empId}")
    public ResponseEntity getEmployeeById(@PathVariable Long empId) {
        try {
            return new ResponseEntity(employeeService.getEmployeeById(empId), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception occurred in getting an employee with empId: " + empId + "\nException: " + e.toString());
            CustomMessageMap customMessageMap=new CustomMessageMap("Exception occurred",e.getMessage());
            return new ResponseEntity(customMessageMap.getMessageMap(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{empId}/leavehistory")
    public ResponseEntity getLeaveHistory(@PathVariable Long empId) {
        try {
            return new ResponseEntity(employeeService.getAllLeaveHistory(empId), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception occurred in getting leaveHistory of employee with empId: " + empId + "\nException: " + e.toString());
            CustomMessageMap customMessageMap=new CustomMessageMap("Exception occurred",e.getMessage());
            return new ResponseEntity(customMessageMap.getMessageMap(),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{empId}/leavehistory")
    public ResponseEntity getLeaveHistory(@PathVariable Long empId, @RequestBody LeaveRequest leaveRequest) {
        try {
            return new ResponseEntity(employeeService.getDateSpecificLeaveHistory(empId, leaveRequest), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception occurred in getting leaveHistory for the date range of the employee with empId: " + empId + "\nException: " + e.toString());
            CustomMessageMap customMessageMap=new CustomMessageMap("Exception occurred",e.getMessage());
            return new ResponseEntity(customMessageMap.getMessageMap(),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/logextrawork")
    public ResponseEntity logExtraWorkRequest(@PathVariable Long id, @RequestBody ExtraWorkRequest extraWorkRequest) {
        try {
            return new ResponseEntity(employeeService.logExtraWorkRequest(id, extraWorkRequest), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception in logging extraWork: " + e);
            CustomMessageMap customMessageMap=new CustomMessageMap("Exception occurred",e.getMessage());
            return new ResponseEntity(customMessageMap.getMessageMap(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}/compoffbalance")
    public ResponseEntity getCompOffBalance(@PathVariable Long id) {
        try {
            return new ResponseEntity(employeeService.getComOffBalance(id), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception in finding Compensatory off balance for an employee with empId: " + id + "\nException:" + e.toString());
            CustomMessageMap customMessageMap=new CustomMessageMap("Exception occurred",e.getMessage());
            return new ResponseEntity(customMessageMap.getMessageMap(),HttpStatus.BAD_REQUEST);
        }
    }

}
