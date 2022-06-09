package com.csctracker.securitycore.service;

import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class DateService {
    private final ConfigsService configsService;

    public DateService(ConfigsService configsService) {
        this.configsService = configsService;
    }


    public Date getIniDate(String period) {
        if (period == null) {
            return today();
        }
        long time = LocalDateTime.now().atZone(configsService.getTimeZone().toZoneId()).toInstant().toEpochMilli();
        switch (period) {
            case "today":
                return today();
            case "yesterday":
                return getStartDay(new Date(time - 24 * 60 * 60 * 1000));
            case "week":
                return getStartDay(new Date(time - 7 * 24 * 60 * 60 * 1000));
            case "month":
                return getStartDay(new Date(time - 30L * 24 * 60 * 60 * 1000));
            case "year":
                return getStartDay(new Date(time - 365L * 24 * 60 * 60 * 1000));
        }
        String unit = period.replaceAll("\\d", "");
        int value = Integer.parseInt(period.replaceAll("\\D", ""));
        switch (unit) {
            case "s":
                return new Date(time - value * 1000L);
            case "m":
                return new Date(time - (long) value * 60 * 1000);
            case "h":
                return new Date(time - (long) value * 60 * 60 * 1000);
            case "d":
                return new Date(time - (long) value * 24 * 60 * 60 * 1000);
            case "w":
                return new Date(time - (long) value * 7 * 24 * 60 * 60 * 1000);
            case "M":
                return new Date(time - value * 30L * 24 * 60 * 60 * 1000);
            case "y":
                return new Date(time - value * 365L * 24 * 60 * 60 * 1000);
        }
        return today();
    }

    public Date getEndDate(String period) {
        long time = LocalDateTime.now().atZone(configsService.getTimeZone().toZoneId()).toInstant().toEpochMilli();
        if (period == null) {
            return getEndDay(new Date(time));
        }
        switch (period) {
            case "yesterday":
                return getEndDay(getIniDate(period));
            default:
                return getEndDay(new Date(time));
        }
    }

    private Date today() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(configsService.getTimeZone());
        try {
            return simpleDateFormat.parse(simpleDateFormat.format(new Date()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Date getStartDay(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(simpleDateFormat.format(date));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Date getEndDay(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        SimpleDateFormat simpleDateFormatEnd = new SimpleDateFormat("yyyy-MM-dd 23:59:59.999");
        try {
            return simpleDateFormat.parse(simpleDateFormatEnd.format(date));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}

