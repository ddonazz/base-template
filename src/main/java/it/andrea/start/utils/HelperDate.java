package it.andrea.start.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HelperDate {

    private static final Logger LOG = LoggerFactory.getLogger(HelperDate.class);

    private HelperDate() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // --- Formatting Methods ---

    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime != null ? dateTime.format(formatter) : null;
    }

    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : null;
    }

    // --- Conversion Methods (java.util.Date <-> java.time) ---

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String dateToStringISO(LocalDate date) {
        return formatDate(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String dateTimeToStringISO(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // --- Date/Time Component Extraction ---

    public static Integer year() {
        return LocalDate.now().getYear();
    }

    public static Integer year(LocalDate date) {
        return Optional.ofNullable(date).map(LocalDate::getYear).orElse(null);
    }

    public static Integer month(LocalDate date) {
        return Optional.ofNullable(date).map(LocalDate::getMonthValue).orElse(null);
    }

    public static Integer day(LocalDate date) {
        return Optional.ofNullable(date).map(LocalDate::getDayOfMonth).orElse(null);
    }

    public static int totalDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1).lengthOfMonth();
    }

    public static int totalDayOfMonth(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        return date.lengthOfMonth();
    }

    public static int getDayOfWeek(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        return date.getDayOfWeek().getValue();
    }

    public static int getDayOfYear(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        return date.getDayOfYear();
    }

    // --- Date/Time Calculations ---

    public static long getDaysBetweenTwoDates(LocalDate dateStart, LocalDate dateEnd) {
        if (dateStart == null || dateEnd == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return ChronoUnit.DAYS.between(dateStart, dateEnd);
    }

    public static long getSecondsBetweenTwoDates(LocalDateTime dateStart, LocalDateTime dateEnd) {
        if (dateStart == null || dateEnd == null) {
            throw new IllegalArgumentException("Both start and end dates must be non-null");
        }
        return ChronoUnit.SECONDS.between(dateStart, dateEnd);
    }

    public static List<Integer> getYearsBetweenDates(LocalDate dateStart, LocalDate dateEnd) {
        List<Integer> years = new ArrayList<>();
        if (dateStart == null || dateEnd == null) {
            throw new IllegalArgumentException("Both start and end dates must be non-null");
        }
        if (dateStart.isAfter(dateEnd)) {
            return years; 
        }
        for (int year = dateStart.getYear(); year <= dateEnd.getYear(); year++) {
            years.add(year);
        }
        return years;
    }

    public static LocalDate addYear(LocalDate date) {
        return Optional.ofNullable(date).map(d -> d.plusYears(1)).orElse(null);
    }

    public static LocalDate addDaysToDate(LocalDate date, int days) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        return date.plusDays(days);
    }

    public static LocalDateTime addSecondsToDateTime(LocalDateTime date, int seconds) {
        return Optional.ofNullable(date).map(d -> d.plusSeconds(seconds)).orElse(null);
    }

    public static LocalDate findNextDayOfWeekInRange(LocalDate start, LocalDate stop, int dayOfWeek) {
        if (start == null || stop == null) {
            throw new IllegalArgumentException("Start and stop dates must not be null");
        }
        if (start.isAfter(stop)) {
            return null; 
        }

        if (start.get(ChronoField.DAY_OF_WEEK) == dayOfWeek) {
            return start;
        }

        int daysToAdd = (dayOfWeek - start.get(ChronoField.DAY_OF_WEEK) + 7) % 7;
        if (daysToAdd == 0) { 
            daysToAdd = 7;
        }
        LocalDate nextDay = start.plusDays(daysToAdd);
        if (!nextDay.isAfter(stop)) {
            return nextDay;
        }
        return null;
    }

    public static int dayOfWeek(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        return date.getDayOfWeek().getValue();
    }

    public static LocalDate getDate(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }
    
}
