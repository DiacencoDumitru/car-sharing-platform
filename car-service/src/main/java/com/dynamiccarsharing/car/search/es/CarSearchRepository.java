package com.dynamiccarsharing.car.search.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CarSearchRepository extends ElasticsearchRepository<CarDocument, String> {
}
