package com.dynamiccarsharing.notification.contacts;

import java.util.Optional;
import java.util.List;

public interface UserContactService {
    Optional<String> getRenterEmail(Long renterId);

    Optional<String> getRenterPhoneNumber(Long renterId);

    List<Long> getUserIdsWhoFavoritedCar(Long carId);
}

