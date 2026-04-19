package com.dynamiccarsharing.car.search.es;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.json.JsonData;
import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.contracts.dto.CarDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarSearchServiceImpl implements CarSearchService {

    private final ElasticsearchOperations operations;
    private final CarSearchRepository repository;
    private final CarRepository carRepository;
    private final CarDocumentMapper mapper;

    private final DemoSwitch demoSwitch;

    @Value("${application.search.indexing-enabled:true}")
    private boolean indexingEnabled;

    @Override
    public Page<CarDto> search(String q, CarSearchCriteria criteria, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(qb -> qb.bool(b -> {

                    if (q != null && !q.isBlank()) {
                        b.must(m -> m.simpleQueryString(s -> s
                                .query(q)
                                .fields("make^2", "model^2", "registrationNumber", "locationCity", "locationState", "locationZip")
                        ));
                    }

                    if (criteria != null) {
                        if (criteria.getMake() != null) {
                            b.filter(f -> f.term(t -> t.field("make.raw").value(criteria.getMake())));
                        }
                        if (criteria.getModel() != null) {
                            b.filter(f -> f.term(t -> t.field("model.raw").value(criteria.getModel())));
                        }
                        if (criteria.getType() != null) {
                            b.filter(f -> f.term(t -> t.field("type").value(criteria.getType().name())));
                        }
                        if (criteria.getVerificationStatus() != null) {
                            b.filter(f -> f.term(t -> t.field("verificationStatus").value(criteria.getVerificationStatus().name())));
                        }
                        if (criteria.getStatusIn() != null && !criteria.getStatusIn().isEmpty()) {
                            List<FieldValue> statuses = criteria.getStatusIn().stream()
                                    .map(Enum::name)
                                    .map(FieldValue::of)
                                    .toList();

                            b.filter(f -> f.terms(t -> t.field("status").terms(v -> v.value(statuses))));
                        }
                        if (criteria.getLocationId() != null) {
                            b.filter(f -> f.term(t -> t.field("locationId").value(criteria.getLocationId())));
                        }
                        if (criteria.getOwnerId() != null) {
                            b.filter(f -> f.term(t -> t.field("ownerId").value(criteria.getOwnerId())));
                        }
                        if (criteria.getPriceGreaterThan() != null || criteria.getPriceLessThan() != null) {
                            b.filter(f -> f.range(r -> {
                                var rr = r.field("pricePerDay");
                                if (criteria.getPriceGreaterThan() != null) {
                                    rr.gte(JsonData.of(criteria.getPriceGreaterThan().doubleValue()));
                                }
                                if (criteria.getPriceLessThan() != null) {
                                    rr.lte(JsonData.of(criteria.getPriceLessThan().doubleValue()));
                                }
                                return rr;
                            }));
                        }
                        BigDecimal minRating = criteria.getMinAverageRating();
                        if (minRating != null) {
                            b.filter(f -> f.range(r -> r.field("averageRating")
                                    .gte(JsonData.of(minRating.doubleValue()))));
                        }
                        Integer minReviews = criteria.getMinReviewCount();
                        if (minReviews != null) {
                            b.filter(f -> f.range(r -> r.field("reviewCount")
                                    .gte(JsonData.of(minReviews.doubleValue()))));
                        }
                    }
                    return b;
                }))
                .withPageable(pageable)
                .build();

        SearchHits<CarDocument> hits = operations.search(query, CarDocument.class);
        List<CarDto> content = hits.getSearchHits().stream()
                .map(h -> mapper.toDto(Objects.requireNonNull(h.getContent())))
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    @Override
    @Transactional(readOnly = true)
    public void indexCar(Long carId) {
        if (!indexingEnabled) return;

        DemoSwitch.Mode mode = demoSwitch.get();

        if (mode == DemoSwitch.Mode.FAIL) {
            log.warn("DemoSwitch=FAIL -> forcing exception before indexing");
            throw new RuntimeException("FORCED_DEMO_FAILURE: Elasticsearch unavailable (simulated)");
        }
        if (mode == DemoSwitch.Mode.SLOW) {
            try {
                log.warn("DemoSwitch=SLOW -> simulating slow ES by sleeping 2500ms");
                Thread.sleep(2500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        carRepository.findById(carId).ifPresent(car -> {
            CarDocument doc = mapper.toDocument(car);
            repository.save(doc);
            log.info("Indexed car {} into Elasticsearch", carId);
        });
    }

    @Override
    public void deleteFromIndex(Long carId) {
        if (!indexingEnabled) return;

        DemoSwitch.Mode mode = demoSwitch.get();
        if (mode == DemoSwitch.Mode.FAIL) {
            log.warn("DemoSwitch=FAIL -> forcing exception before deleteFromIndex");
            throw new RuntimeException("FORCED_DEMO_FAILURE: Elasticsearch delete failed (simulated)");
        }
        if (mode == DemoSwitch.Mode.SLOW) {
            try {
                log.warn("DemoSwitch=SLOW -> simulating slow ES by sleeping 2500ms on delete");
                Thread.sleep(2500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        repository.deleteById(String.valueOf(carId));
        log.info("Deleted car {} from Elasticsearch index", carId);
    }
}