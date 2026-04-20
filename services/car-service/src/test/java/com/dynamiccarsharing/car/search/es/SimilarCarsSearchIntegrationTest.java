package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.car.testsupport.SimilarCarsEsTestApplication;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = SimilarCarsEsTestApplication.class)
class SimilarCarsSearchIntegrationTest {

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
    private CarSearchService carSearchService;

    @Autowired
    private CarSearchRepository carSearchRepository;

    @MockBean
    private CarRepository carRepository;

    @BeforeEach
    void resetIndex() {
        carSearchRepository.deleteAll();
    }

    @Test
    void findSimilar_excludesAnchor_andAppliesBoolFilters() {
        Location location = Location.builder()
                .id(1L)
                .city("Austin")
                .state("TX")
                .zipCode("78701")
                .build();

        Car anchor = Car.builder()
                .id(100L)
                .registrationNumber("REG-100")
                .make("Toyota")
                .model("Camry")
                .status(CarStatus.AVAILABLE)
                .location(location)
                .ownerId(1L)
                .price(new BigDecimal("100.00"))
                .type(CarType.SEDAN)
                .verificationStatus(VerificationStatus.VERIFIED)
                .averageRating(new BigDecimal("4.0"))
                .reviewCount(2)
                .build();

        when(carRepository.findById(100L)).thenReturn(Optional.of(anchor));

        carSearchRepository.save(doc("100", CarType.SEDAN, "Toyota", 100.0, CarStatus.AVAILABLE, VerificationStatus.VERIFIED, 4.0));
        carSearchRepository.save(doc("101", CarType.SEDAN, "Toyota", 90.0, CarStatus.AVAILABLE, VerificationStatus.VERIFIED, 4.5));
        carSearchRepository.save(doc("102", CarType.SUV, "Toyota", 90.0, CarStatus.AVAILABLE, VerificationStatus.VERIFIED, 4.2));
        carSearchRepository.save(doc("103", CarType.SEDAN, "Honda", 90.0, CarStatus.AVAILABLE, VerificationStatus.VERIFIED, 4.3));
        carSearchRepository.save(doc("104", CarType.SEDAN, "Toyota", 50.0, CarStatus.AVAILABLE, VerificationStatus.VERIFIED, 4.1));

        Page<CarDto> page = carSearchService.findSimilar(100L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(101L);
    }

    private static CarDocument doc(
            String id,
            CarType type,
            String make,
            double pricePerDay,
            CarStatus status,
            VerificationStatus verificationStatus,
            double averageRating
    ) {
        return CarDocument.builder()
                .id(id)
                .type(type)
                .make(make)
                .model("Model-" + id)
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
                .reviewCount(1)
                .build();
    }
}
