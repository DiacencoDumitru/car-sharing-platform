package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.car.testsupport.SimilarCarsEsTestApplication;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = SimilarCarsEsTestApplication.class)
@AutoConfigureMockMvc
class CarSearchEndpointIntegrationTest {

    @Container
    static final ElasticsearchContainer ELASTICSEARCH = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.14.3"))
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node");

    @DynamicPropertySource
    static void registerEsProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", () -> "http://" + ELASTICSEARCH.getHttpHostAddress());
        registry.add("spring.cloud.discovery.enabled", () -> "false");
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarSearchRepository carSearchRepository;

    @MockBean
    private CarRepository carRepository;

    @BeforeEach
    void resetIndex() {
        carSearchRepository.deleteAll();
    }

    @Test
    void search_returnsFilteredCars_fromElasticsearch() throws Exception {
        carSearchRepository.save(doc("201", "Toyota", "Camry", 100.0, CarStatus.AVAILABLE, VerificationStatus.VERIFIED, 4.7));
        carSearchRepository.save(doc("202", "Toyota", "Corolla", 210.0, CarStatus.AVAILABLE, VerificationStatus.VERIFIED, 4.8));
        carSearchRepository.save(doc("203", "Honda", "Civic", 95.0, CarStatus.RENTED, VerificationStatus.VERIFIED, 4.9));

        mockMvc.perform(get("/api/v1/cars/search")
                        .param("q", "toyota")
                        .param("minPrice", "80")
                        .param("maxPrice", "150")
                        .param("statusIn", "AVAILABLE")
                        .param("minAverageRating", "4.5")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(201))
                .andExpect(jsonPath("$.content[0].make").value("Toyota"))
                .andExpect(jsonPath("$.content[0].instanceId").value("car-service:unknown"));
    }

    private static CarDocument doc(
            String id,
            String make,
            String model,
            double pricePerDay,
            CarStatus status,
            VerificationStatus verificationStatus,
            double averageRating
    ) {
        return CarDocument.builder()
                .id(id)
                .make(make)
                .model(model)
                .type(CarType.SEDAN)
                .status(status)
                .verificationStatus(verificationStatus)
                .pricePerDay(pricePerDay)
                .registrationNumber("REG-" + id)
                .locationId(1L)
                .locationCity("Austin")
                .locationState("TX")
                .locationZip("78701")
                .ownerId(1L)
                .averageRating(averageRating)
                .reviewCount(3)
                .build();
    }
}
