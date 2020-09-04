package com.io.lms.controller;

import com.io.lms.dto.LeaveRequest;
import com.io.lms.service.LeaveServicesFactory;
import com.io.lms.util.CustomMessageMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping
public class LeaveController {

    @Autowired
    private LeaveServicesFactory leaveServicesFactory;


    @PostMapping("/leave/apply/{empId}")
    public ResponseEntity enterNewLeaveRequest(@RequestBody LeaveRequest leaveRequest, @PathVariable Long empId) {
        try {
            return new ResponseEntity<>(leaveServicesFactory.getService(leaveRequest.getLeaveType().trim().toLowerCase()).enterNewLeaveRequest(leaveRequest, empId), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception in applying leave of an employee with empId: " + empId + "\nException: " + e.toString());
            CustomMessageMap customMessageMap=new CustomMessageMap("Exception occurred",e.getMessage());
            return new ResponseEntity(customMessageMap.getMessageMap(),HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/employee/{id}/leavebalance")
    public ResponseEntity getAllLeaves(@PathVariable Long id) {
        try {
            return new ResponseEntity("Out Of Office (OOO) Leave Balance= " + leaveServicesFactory.getService("ooo").getLeaveBalance(id), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception in getting the leave balance of an employee with id: " + id + "\nException: " + e.toString());
            CustomMessageMap customMessageMap=new CustomMessageMap("Exception occurred",e.getMessage());
            return new ResponseEntity(customMessageMap.getMessageMap(),HttpStatus.BAD_REQUEST);
        }

    }

}
