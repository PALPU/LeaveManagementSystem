package com.io.lms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.io.lms.constant.Constants.SATURDAY;
import static com.io.lms.constant.Constants.SUNDAY;

@Component
@Slf4j
public class HolidaysAndNonWorkingDays {

    private static HolidaysAndNonWorkingDays holidaysAndNonWorkingDays = null;
    private final String[] holidays = {"01-09-2020", "10-09-2020", "15-09-2020", "20-09-2020", "25-09-2020", "30-09-2020", "01-11-2020", "15-11-2020", "11-12-2020", "15-12-2020"};
    private final Integer[] nonWorkingDays = {SUNDAY, SATURDAY};
    private HashSet<Date> holidaysSet = new HashSet<>();
    private HashSet<Integer> nonWorkingDaysSet = new HashSet<>();

    private HolidaysAndNonWorkingDays() {
        List<Date> dates = Arrays.asList(holidays).stream().map((holiday) -> {
            try {
                return (Utility.stringToDate(holiday));
            } catch (ParseException e) {
                log.error("Exception while parsing the date");
            }
            return null;
        }).filter((holiday) -> holiday != (null)).collect(Collectors.<Date>toList());
        setHolidaysSet(new HashSet<Date>(dates));
        setNonWorkingDaysSet(new HashSet<Integer>(Arrays.asList(nonWorkingDays)));
    }

    public static HolidaysAndNonWorkingDays getInstance() {
        if (holidaysAndNonWorkingDays == null) {
            holidaysAndNonWorkingDays = new HolidaysAndNonWorkingDays();
        }
        return holidaysAndNonWorkingDays;
    }

    public HashSet<Date> getHolidaysSet() {
        return holidaysSet;
    }

    public void setHolidaysSet(HashSet<Date> holidaysSet) {
        this.holidaysSet = holidaysSet;
    }

    public HashSet<Integer> getNonWorkingDaysSet() {
        return nonWorkingDaysSet;
    }

    public void setNonWorkingDaysSet(HashSet<Integer> nonWorkingDaysSet) {
        this.nonWorkingDaysSet = nonWorkingDaysSet;
    }

}
