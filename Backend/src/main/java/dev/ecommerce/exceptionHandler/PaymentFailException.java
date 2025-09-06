package dev.ecommerce.exceptionHandler;

public class PaymentFailException extends RuntimeException {
    public PaymentFailException(String message) {
        super(message);
    }
}
