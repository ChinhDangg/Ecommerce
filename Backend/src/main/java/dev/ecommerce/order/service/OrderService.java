package dev.ecommerce.order.service;

import dev.ecommerce.order.entity.Order;
import dev.ecommerce.order.entity.OrderItem;
import dev.ecommerce.order.model.OrderInfo;
import dev.ecommerce.order.model.OrderItemInfo;
import dev.ecommerce.order.model.TotalOrderInfo;
import dev.ecommerce.order.repository.OrderRepository;
import dev.ecommerce.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public TotalOrderInfo getUserOrderInfo(Long userId, int monthCutOff, int page, int size) {
        Instant cutOff =  Instant.now().minus(monthCutOff, ChronoUnit.MONTHS);
        Pageable pageable = PageRequest.of(page, size);
        List<Order> userOrders = orderRepository.findAllByUserIdOrderByPlacedAtDesc(userId, cutOff, pageable);

        List<OrderInfo> orderInfos = new ArrayList<>();
        for (Order order : userOrders) {
            List<OrderItemInfo> orderItemInfos = new ArrayList<>();
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                OrderItemInfo itemInfo = new OrderItemInfo(
                        product.getId(), product.getThumbnail(), product.getName(),
                        orderItem.getUnitPrice(), orderItem.getOrderStatus(),
                        orderItem.getStatusTime().atZone(ZoneId.systemDefault()).toLocalDate());
                orderItemInfos.add(itemInfo);
            }
            OrderInfo orderInfo = new OrderInfo(
                    order.getPlacedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                    order.getTotal(),
                    order.getId(),
                    order.getStatus(),
                    order.getStatusTime().atZone(ZoneId.systemDefault()).toLocalDate(),
                    orderItemInfos
            );
            orderInfos.add(orderInfo);
        }

        return new TotalOrderInfo(orderRepository.countAllByUserIdOrderByPlacedAtDesc(userId, cutOff), orderInfos);
    }
}
