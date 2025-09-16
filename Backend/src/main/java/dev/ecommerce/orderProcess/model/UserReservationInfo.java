package dev.ecommerce.orderProcess.model;

public record UserReservationInfo(
        long productId,
        int held,
        long minuteLeft
) {
}
