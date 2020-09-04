package com.io.lms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LeaveServicesFactory {
    private static final Map<String, LeaveService> leaveServiceCache = new HashMap<>();
    @Autowired
    private List<LeaveService> services;

    @PostConstruct
    public void initMyServiceCache() {
        for (LeaveService service : services) {
            leaveServiceCache.put(service.getType(), service);
        }
    }

    public LeaveService getService(String type) {
        log.debug("Inside getService() of LeaveServicesFactory class");
        LeaveService service = leaveServiceCache.get(type);
        if (service == null) throw new RuntimeException("Leave Type not found: " + type);
        return service;
    }
}
