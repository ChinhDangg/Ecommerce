package dev.ecommerce.userInfo.constant;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public enum OrderPlacedWindow {
    DAYS_30(30),
    DAYS_90(90),
    YEAR; // requires explicit year

    private final Integer days;

    OrderPlacedWindow(int days) { this.days = days; }
    OrderPlacedWindow() { this.days = null; }

    public record TimeRange(Instant start, Instant end) {
        public boolean contains(Instant i) {
            return !i.isBefore(start) && !i.isAfter(end);
        }
    }

    public boolean isYear() {
        return this == YEAR;
    }

    public TimeRange rangeForDays(Instant reference, ZoneId zone) {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(zone, "zone");
        if (days == null) {
            throw new UnsupportedOperationException("Use rangeForYear(year, zone) for YEAR.");
        }
        Instant end = reference;
        Instant start = reference.minus(days, ChronoUnit.DAYS);
        return new TimeRange(start, end);
    }

    public TimeRange rangeForYear(int year, ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        ZonedDateTime startZdt = Year.of(year).atDay(1).atStartOfDay(zone); // Jan 1, 00:00
        // Dec 31, 23:59:59.999999999 in the same zone
        ZonedDateTime endZdt = Year.of(year)
                .atMonth(Month.DECEMBER)
                .atEndOfMonth()
                .atTime(LocalTime.MAX)
                .atZone(zone);
        return new TimeRange(startZdt.toInstant(), endZdt.toInstant());
    }
}
