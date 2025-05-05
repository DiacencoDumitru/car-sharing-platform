package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Car;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CarRepository implements Repository<Car> {

    private final Map<Long, Car> carsById = new HashMap<>();
    private final Map<String, Car> carsByRegistration = new HashMap<>();

    @Override
    public void save(Car car) {
        carsById.put(car.getId(), car);
        carsByRegistration.put(car.getRegistrationNumber(), car);
    }

    @Override
    public Car findById(Long id) {
        return carsById.get(id);
    }

    @Override
    public Car findByField(String fieldValue) {
        return carsByRegistration.get(fieldValue);
    }

    @Override
    public void update(Car car) {
        if (carsById.containsKey(car.getId())) {
            carsById.put(car.getId(), car);
            carsByRegistration.put(car.getRegistrationNumber(), car);
        }
    }

    @Override
    public void delete(Long id) {
        Car car = carsById.get(id);
        carsById.remove(id);
        carsByRegistration.remove(car.getRegistrationNumber());
    }

    @Override
    public Map<Long, Car> findAll() {
        return new HashMap<>(carsById);
    }

    public Map<Long, Car> findByFilter(String field, String value) {
        return carsById.entrySet().stream()
                .filter(entry -> {
                    Car car = entry.getValue();
                    return (field.equals("model") && car.getModel().equals(value)) ||
                            (field.equals("registrationNumber") && car.getRegistrationNumber().equals(value)) ||
                            (field.equals("status") && car.getStatus().equals(value)) ||
                            (field.equals("location") && car.getLocation().equals(value)) ||
                            (field.equals("price") && Double.toString(car.getPrice()).equals(value)) ||
                            (field.equals("type") && car.getType().equals(value)) ||
                            (field.equals("verificationStatus") && car.getVerificationStatus().equals(value));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
