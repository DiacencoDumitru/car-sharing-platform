package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.Location;

public class LocationFilter implements Filter<Location> {
    private String city;
    private String state;
    private String zipCode;

    public LocationFilter setCity(String city) {
        this.city = city;
        return this;
    }

    public LocationFilter setState(String state) {
        this.state = state;
        return this;
    }

    public LocationFilter setZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
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
