gitpackage com.dynamiccarsharing.booking.service.interfaces;

import java.util.function.Supplier;

public interface BookingCreationGuard {
    <T> T executeWithCarLock(Long carId, Supplier<T> action);
}
