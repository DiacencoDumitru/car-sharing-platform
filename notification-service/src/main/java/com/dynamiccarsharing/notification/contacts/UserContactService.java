package com.dynamiccarsharing.notification.contacts;

import java.util.Optional;

public interface UserContactService {
    Optional<String> getRenterEmail(Long renterId);

    Optional<String> getRenterPhoneNumber(Long renterId);
}

