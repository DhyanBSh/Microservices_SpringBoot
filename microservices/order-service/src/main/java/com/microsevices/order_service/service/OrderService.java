package com.microsevices.order_service.service;

import com.microsevices.order_service.dto.InventoryResponse;
import com.microsevices.order_service.dto.OrderLineItemsDto;
import com.microsevices.order_service.dto.OrderRequest;
import com.microsevices.order_service.model.Order;
import com.microsevices.order_service.model.OrderLineItems;
import com.microsevices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

//        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
//                .stream()
//                .map(this::mapToDto)
//                .toList();

        List<OrderLineItems> orderLineItems = new ArrayList<>(
                orderRequest.getOrderLineItemsDtoList()
                        .stream()
                        .map(this::mapToDto)
                        .toList()
        );


        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        InventoryResponse[] inventoryResponsArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                        .retrieve()
                                .bodyToMono(InventoryResponse[].class)
                                        .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponsArray)
                .allMatch(InventoryResponse::isInStock);

        if(allProductsInStock) {
            orderRepository.save(order);
        }else{
            throw new IllegalArgumentException("Product is out of stock. Try again later.");
        }

        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems items = new OrderLineItems();
        items.setPrice(orderLineItemsDto.getPrice());
        items.setQuantity(orderLineItemsDto.getQuantity());
        items.setSkuCode(orderLineItemsDto.getSkuCode());
        return items;
    }
}
