package am.ak.microservice.example.order.repository;

import am.ak.microservice.example.order.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
