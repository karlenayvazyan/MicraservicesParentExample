package am.ak.microservice.example.product.service;

import am.ak.microservice.example.product.dto.ProductRequest;
import am.ak.microservice.example.product.dto.ProductResponse;
import am.ak.microservice.example.product.model.ProductDocument;
import am.ak.microservice.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest) {
        ProductDocument productDocument = mapToDocument(productRequest);
        productRepository.save(productDocument);
        log.info("Product {} created", productDocument.getId());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private ProductDocument mapToDocument(ProductRequest productRequest) {
        return ProductDocument.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();
    }

    private ProductResponse mapToDto(ProductDocument productDocument) {
        return ProductResponse.builder()
                .id(productDocument.getId())
                .name(productDocument.getName())
                .description(productDocument.getDescription())
                .price(productDocument.getPrice())
                .build();
    }
}
