package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Document(indexName = "cars")
@Setting(sortFields = { })
public class CarDocument {

    @Id
    private String id;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = { @InnerField(suffix = "raw", type = FieldType.Keyword) }
    )
    private String make;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = { @InnerField(suffix = "raw", type = FieldType.Keyword) }
    )
    private String model;

    @Field(type = FieldType.Keyword)
    private CarStatus status;

    @Field(type = FieldType.Keyword)
    private CarType type;

    @Field(type = FieldType.Keyword)
    private VerificationStatus verificationStatus;

    @Field(type = FieldType.Double)
    private Double pricePerDay;

    @Field(type = FieldType.Keyword)
    private String registrationNumber;

    @Field(type = FieldType.Long)
    private Long locationId;

    @Field(type = FieldType.Text)
    private String locationCity;

    @Field(type = FieldType.Text)
    private String locationState;

    @Field(type = FieldType.Keyword)
    private String locationZip;

    @Field(type = FieldType.Long)
    private Long ownerId;
}
