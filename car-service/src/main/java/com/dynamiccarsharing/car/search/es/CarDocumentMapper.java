package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.contracts.dto.CarDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CarDocumentMapper {

    @Mapping(target = "id", expression = "java(car.getId() == null ? null : String.valueOf(car.getId()))")
    @Mapping(target = "make", source = "make")
    @Mapping(target = "model", source = "model")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "verificationStatus", source = "verificationStatus")
    @Mapping(target = "pricePerDay", expression = "java(toDouble(car.getPrice()))")
    @Mapping(target = "registrationNumber", source = "registrationNumber")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationCity", source = "location.city")
    @Mapping(target = "locationState", source = "location.state")
    @Mapping(target = "locationZip", source = "location.zipCode")
    @Mapping(target = "ownerId", source = "ownerId")
    CarDocument toDocument(Car car);

    @InheritInverseConfiguration(name = "toDocument")
    @Mapping(target = "price", expression = "java(toBigDecimal(doc.getPricePerDay()))")
    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "instanceId", ignore = true)
    CarDto toDto(CarDocument doc);

    default Double toDouble(BigDecimal price) {
        return price == null ? null : price.doubleValue();
    }

    default BigDecimal toBigDecimal(Double v) {
        return v == null ? null : BigDecimal.valueOf(v);
    }
}
