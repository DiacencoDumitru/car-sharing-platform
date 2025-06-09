package com.dynamiccarsharing.carsharing.util;

import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.lang.reflect.Field;
import java.util.List;

public class FilterUtil {

    private FilterUtil() {}

    public static <T> void buildQuery(Filter<T> filter, String tableAlias, StringBuilder query, List<Object> params, String... filterFields) throws IllegalAccessException {
        String tablePrefix = tableAlias;

        if (filter == null) return;

        for (Field field : filter.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(filter);
            if (value != null) {
                String columnName = field.getName();

                if (isContactInfoField(columnName) && !tableAlias.equals("contact_infos")) {
                    tablePrefix = "c";
                } else {
                    tablePrefix = tableAlias;
                }

                switch (columnName) {
                    case "location" -> columnName = "location_id";
                    case "verificationStatus" -> columnName = "verification_status";
                    case "renterId" -> columnName = "renter_id";
                    case "reviewerId" -> columnName = "reviewer_id";
                    case "userId" -> columnName = "user_id";
                    case "carId" -> columnName = "car_id";
                    case "bookingId" -> columnName = "booking_id";
                    case "contactInfoId" -> columnName = "contact_info_id";
                    case "pickupLocationId" -> columnName = "pickup_location_id";
                    case "locationId" -> columnName = "location_id";
                    case "creationUserId" -> columnName = "creation_user_id";
                    case "pricePerDay" -> columnName = "price_per_day";
                    case "carType" -> columnName = "car_type";
                    case "paymentMethod" -> columnName = "payment_method";
                    case "startTime" -> columnName = "start_time";
                    case "endTime" -> columnName = "end_time";
                    case "createdAt" -> columnName = "created_at";
                    case "updatedAt" -> columnName = "updated_at";
                    case "resolvedAt" -> columnName = "resolved_at";
                    case "amount" -> columnName = "amount";
                }

                for (String filterField : filterFields) {
                    if (filterField.equals(field.getName())) {
                        if (isLikeField(columnName)) {
                            query.append(" AND ").append(tablePrefix).append(".").append(columnName).append(" LIKE ?");
                            params.add("%" + value + "%");
                        } else {
                            query.append(" AND ").append(tablePrefix).append(".").append(columnName).append(" = ?");
                            params.add(value);
                        }
                        break;
                    }
                }
            }
        }
    }

    private static boolean isContactInfoField(String fieldName) {
        return fieldName.equals("email") || fieldName.equals("phoneNumber") ||
                fieldName.equals("firstName") || fieldName.equals("lastName");
    }

    private static boolean isLikeField(String columnName) {
        return columnName.equals("comment") || columnName.equals("city") ||
                columnName.equals("state") || columnName.equals("zip_code") ||
                columnName.equals("email") || columnName.equals("phone_number") ||
                columnName.equals("first_name") || columnName.equals("last_name") ||
                columnName.equals("registration_number") || columnName.equals("make") ||
                columnName.equals("model") || columnName.equals("dispute_description") ||
                columnName.equals("description");
    }
}