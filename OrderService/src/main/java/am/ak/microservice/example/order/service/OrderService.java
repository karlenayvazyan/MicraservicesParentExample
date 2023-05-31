package am.ak.microservice.example.order.service;

import am.ak.microservice.example.order.dto.InventoryResponse;
import am.ak.microservice.example.order.dto.OrderLineItemsRequest;
import am.ak.microservice.example.order.dto.OrderRequest;
import am.ak.microservice.example.order.event.OrderPlacedEvent;
import am.ak.microservice.example.order.model.OrderEntity;
import am.ak.microservice.example.order.model.OrderLineItemsEntity;
import am.ak.microservice.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import zipkin2.internal.Trace;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String createOrder(OrderRequest orderRequest) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItemsEntity> orderLineItemsEntities = orderRequest.getOrderLineItems()
                .stream()
                .map(this::mapToOrderLineItemsEntity)
                .toList();

        orderEntity.setOrderLineItemList(orderLineItemsEntities);

        List<String> skuCodes = orderEntity.getOrderLineItemList()
                .stream()
                .map(OrderLineItemsEntity::getSkuCode)
                .toList();

        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())) {
            InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory", uriBuilder -> {
                        return uriBuilder.queryParam("skuCode", skuCodes).build();
                    })
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean allProductInStock = Arrays.stream(inventoryResponses)
                    .allMatch(InventoryResponse::isInStock);

            if (!allProductInStock) {
                orderRepository.save(orderEntity);
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(orderEntity.getOrderNumber()));
                return "Order placed successfully";
            } else {
                throw new IllegalStateException("Product isn't in stock");
            }
        } finally {
            inventoryServiceLookup.end();
        }
    }

    private OrderLineItemsEntity mapToOrderLineItemsEntity(OrderLineItemsRequest orderLineItemsRequest) {
        OrderLineItemsEntity orderLineItemsEntity = new OrderLineItemsEntity();
        orderLineItemsEntity.setQuantity(orderLineItemsRequest.getQuantity());
        orderLineItemsEntity.setPrice(orderLineItemsRequest.getPrice());
        orderLineItemsEntity.setSkuCode(orderLineItemsRequest.getSkuCode());
        return orderLineItemsEntity;
    }
}
