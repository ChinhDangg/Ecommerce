package dev.ecommerce.userInfo.DTO;

import java.time.Instant;

public record TimeFilterOption(
        String key,            // e.g. "last_30_days", "year_2024"
        String label,          // e.g. "Last 30 days", "2024"
        Instant startInclusive,
        Instant endExclusive   // use with placedAt >= start && placedAt < end
) {
}
