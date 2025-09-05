package dev.ecommerce.order.service;

import dev.ecommerce.order.model.TimeFilterOption;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class TimeFilter {

    public static List<TimeFilterOption> buildOptions(Instant oldestOrder, ZoneId zone) {
        Instant now = Instant.now();
        long historyDays = DAYS.between(oldestOrder, now);

        // Always include: Last 30 days
        List<TimeFilterOption> options = new ArrayList<>();
        options.add(window("last_30_days", "Last 30 days", now.minus(30, DAYS), now));

        // Special case: if history < 30 days → only that one option
        if (historyDays < 30) return options;

        // Include Last 90 days if the user has at least 90 days of history
        if (historyDays >= 90) {
            options.add(window("last_90_days", "Last 90 days", now.minus(90, DAYS), now));
        }

        // Year buckets from current year down to oldest year (inclusive)
        int currentYear = ZonedDateTime.ofInstant(now, zone).getYear();
        int oldestYear  = ZonedDateTime.ofInstant(oldestOrder, zone).getYear();

        for (int year = currentYear; year >= oldestYear; year--) {
            Instant yearStart = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant();
            Instant yearEndEx = LocalDate.of(year + 1, 1, 1).atStartOfDay(zone).toInstant();

            // Clip the first/last year to real history bounds
            Instant start = (year == oldestYear) ? max(yearStart, floorToStartOfDay(oldestOrder, zone)) : yearStart;
            Instant end   = (year == currentYear) ? now : yearEndEx;

            // Only add if the interval has any overlap with [oldestOrder, now)
            if (start.isBefore(end)) {
                options.add(window("year_" + year, String.valueOf(year), start, end));
            }
        }

        return options;
    }

    private static TimeFilterOption window(String key, String label, Instant start, Instant end) {
        return new TimeFilterOption(key, label, start, end);
    }

    private static Instant floorToStartOfDay(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }
}
