package config;

import com.dynamiccarsharing.carsharing.repository.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockRepositoryConfig {

    @Bean
    public BookingRepository bookingRepository() {
        return Mockito.mock(BookingRepository.class);
    }

    @Bean
    public CarRepository carRepository() {
        return Mockito.mock(CarRepository.class);
    }

    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    public ContactInfoRepository contactInfoRepository() {
        return Mockito.mock(ContactInfoRepository.class);
    }

    @Bean
    public CarReviewRepository carReviewRepository() {
        return Mockito.mock(CarReviewRepository.class);
    }

    @Bean
    public UserReviewRepository userReviewRepository() {
        return Mockito.mock(UserReviewRepository.class);
    }

    @Bean
    public DisputeRepository disputeRepository() {
        return Mockito.mock(DisputeRepository.class);
    }

    @Bean
    public LocationRepository locationRepository() {
        return Mockito.mock(LocationRepository.class);
    }

    @Bean
    public PaymentRepository paymentRepository() {
        return Mockito.mock(PaymentRepository.class);
    }

    @Bean
    public TransactionRepository transactionRepository() {
        return Mockito.mock(TransactionRepository.class);
    }
}