package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.Location;

public class LocationFilter implements Filter<Location> {
    private final String city;
    private final String state;
    private final String zipCode;

    private LocationFilter(String city, String state, String zipCode) {
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    public static LocationFilter of(String city, String state, String zipCode) {
        return new LocationFilter(city, state, zipCode);
    }

    public static LocationFilter ofCity(String city) {
        return new LocationFilter(city, null, null);
    }

    public static LocationFilter ofState(String state) {
        return new LocationFilter(null, state, null);
    }

    public static LocationFilter ofZipCode(String zipCode) {
        return new LocationFilter(null, null, zipCode);
    }

    @Override
    public boolean test(Location location) {
        boolean matches = true;
        if (city != null) matches &= location.getCity().equals(city);
        if (state != null) matches &= location.getState().equals(state);
        if (zipCode != null) matches &= location.getZipCode().equals(zipCode);
        return matches;
    }
}
