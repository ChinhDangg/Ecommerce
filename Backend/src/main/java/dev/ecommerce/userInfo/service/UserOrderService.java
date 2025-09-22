package dev.ecommerce.userInfo.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.orderProcess.entity.Order;
import dev.ecommerce.orderProcess.entity.OrderItem;
import dev.ecommerce.userInfo.DTO.TimeFilterOption;
import dev.ecommerce.userInfo.DTO.UserOrderInfo;
import dev.ecommerce.userInfo.DTO.UserOrderItemInfo;
import dev.ecommerce.userInfo.DTO.UserOrderHistory;
import dev.ecommerce.orderProcess.repository.OrderRepository;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.userInfo.constant.OrderPlacedWindow;
import dev.ecommerce.userInfo.entity.UserUsageInfo;
import dev.ecommerce.userInfo.repository.UserUsageInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final OrderRepository orderRepository;
    private final UserUsageInfoRepository userInfoRepository;

    public UserUsageInfo findUserInfoByUserId(Long userId) {
        return userInfoRepository.findByUserId(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId)
        );
    }

    // start should be the current date and end is at some point in the past
    @Transactional(readOnly = true)
    public UserOrderHistory getUserOrderHistory(Long userId, String orderPlacedWindow, Integer page, int size) {
        page = page == null ? 0 : page;
        Pageable pageable = PageRequest.of(page, size);

        orderPlacedWindow = orderPlacedWindow == null ? OrderPlacedWindow.DAYS_30.name() : orderPlacedWindow;

        TimeRange timeRange;

        OrderPlacedWindow day = isDay(orderPlacedWindow);
        if (day == null) {
            if (!orderPlacedWindow.matches("\\d{4}"))
                throw new ResourceNotFoundException("Order Placed Window in year is not valid");
            timeRange = rangeForYear(Integer.parseInt(orderPlacedWindow), ZoneId.systemDefault());
        } else {
            timeRange = rangeForDays(Instant.now(), day.getDays(), ZoneId.systemDefault());
        }

        Instant start = timeRange.start;
        Instant end = timeRange.end;

        Page<Order> userOrders = orderRepository.findByUserInfoIdAndPlacedAtGreaterThanEqualAndPlacedAtLessThanOrderByPlacedAtDesc(
                userId, start, end, pageable
        );

        List<UserOrderInfo> userOrderInfos = new ArrayList<>();
        for (Order order : userOrders) {
            List<UserOrderItemInfo> userOrderItemInfos = new ArrayList<>();
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                UserOrderItemInfo itemInfo = new UserOrderItemInfo(
                        product.getId(),
                        product.getThumbnail(),
                        product.getName(),
                        orderItem.getUnitPrice(),
                        orderItem.getQuantity(),
                        orderItem.getOrderStatus(),
                        orderItem.getStatusTime() == null ? null : orderItem.getStatusTime().atZone(ZoneId.systemDefault()).toLocalDate());
                userOrderItemInfos.add(itemInfo);
            }
            UserOrderInfo userOrderInfo = new UserOrderInfo(
                    order.getPlacedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                    order.getTotal(),
                    order.getId(),
                    order.getStatus(),
                    order.getStatusTime().atZone(ZoneId.systemDefault()).toLocalDate(),
                    userOrderItemInfos
            );
            userOrderInfos.add(userOrderInfo);
        }

        return new UserOrderHistory(
                buildOptions(findUserInfoByUserId(userId).getFirstOrderAt(), ZoneId.systemDefault()),
                new PageImpl<>(userOrderInfos, PageRequest.of(page, size),
                        orderRepository.countAllByUserInfoIdAndPlacedAtGreaterThanEqualAndPlacedAtLessThanOrderByPlacedAtDesc(
                                userId, start, end)
                )
        );
    }

    public OrderPlacedWindow isDay(String value) {
        try {
            return OrderPlacedWindow.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    public TimeRange rangeForDays(Instant reference, int days, ZoneId zone) {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(zone, "zone");
        Instant end = reference;
        Instant start = reference.minus(days, ChronoUnit.DAYS);
        return new TimeRange(start, end);
    }

    public record TimeRange(Instant start, Instant end) {}

    public static List<TimeFilterOption> buildOptions(Instant oldestOrder, ZoneId zone) {

        // Always include: Last 30 days
        List<TimeFilterOption> options = new ArrayList<>();
        options.add(window(OrderPlacedWindow.DAYS_30.name(), "Last 30 days"));

        if (oldestOrder == null) {
            return options;
        }

        Instant now = Instant.now();
        long historyDays = DAYS.between(oldestOrder, now);

        // Special case: if history < 30 days → only that one option
        if (historyDays < 30) return options;

        // Include Last 90 days if the user has at least 90 days of history
        if (historyDays >= 90) {
            options.add(window(OrderPlacedWindow.DAYS_90.name(), "Last 90 days"));
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
                options.add(window(String.valueOf(year), "Year " + year));
            }
        }

        return options;
    }

    private static TimeFilterOption window(String key, String label) {
        return new TimeFilterOption(key, label);
    }

    private static Instant floorToStartOfDay(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }
}
