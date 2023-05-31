package am.ak.microservice.example.product.repository;

import am.ak.microservice.example.product.model.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<ProductDocument, String> {
}
