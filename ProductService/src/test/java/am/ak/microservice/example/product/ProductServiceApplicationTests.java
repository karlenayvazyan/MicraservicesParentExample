package am.ak.microservice.example.product;

import am.ak.microservice.example.product.dto.ProductRequest;
import am.ak.microservice.example.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.assertions.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.url", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void createProductTest() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getDocumentJson())
        ).andExpect(status().isCreated());
        Assertions.assertTrue(productRepository.findAll().size() > 0);
    }

    private String getDocumentJson() throws JsonProcessingException {
        ProductRequest build = ProductRequest.builder()
                .name("testName")
                .description("testDescription")
                .price(BigDecimal.ONE)
                .build();
        return objectMapper.writeValueAsString(build);
    }
}
