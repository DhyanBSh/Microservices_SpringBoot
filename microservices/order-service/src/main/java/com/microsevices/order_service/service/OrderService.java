package com.microsevices.order_service.service;

import com.microsevices.order_service.dto.OrderLineItemsDto;
import com.microsevices.order_service.dto.OrderRequest;
import com.microsevices.order_service.model.Order;
import com.microsevices.order_service.model.OrderLineItems;
import com.microsevices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItems.getQuantity());
        orderLineItems.setSkuCode(orderLineItems.getSkuCode());
        return orderLineItems;
    }
}
