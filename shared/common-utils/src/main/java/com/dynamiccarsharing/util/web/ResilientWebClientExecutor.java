package com.dynamiccarsharing.util.web;

import com.dynamiccarsharing.util.exception.ServiceException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ResilientWebClientExecutor {

    private final Duration timeout;
    private final long retryMaxAttempts;
    private final Duration retryBackoff;
    private final Predicate<Throwable> retriablePredicate;

    public ResilientWebClientExecutor(long timeoutSeconds, long retryMaxAttempts, long retryBackoffMillis) {
        this(timeoutSeconds, retryMaxAttempts, retryBackoffMillis, ResilientWebClientExecutor::isDefaultRetriable);
    }

    public ResilientWebClientExecutor(long timeoutSeconds, long retryMaxAttempts, long retryBackoffMillis, Predicate<Throwable> retriablePredicate) {
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.retryMaxAttempts = retryMaxAttempts;
        this.retryBackoff = Duration.ofMillis(retryBackoffMillis);
        this.retriablePredicate = Objects.requireNonNull(retriablePredicate);
    }

    public <T> T execute(Supplier<Mono<T>> requestSupplier, String failureMessage) {
        try {
            return requestSupplier.get()
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(retryMaxAttempts, retryBackoff).filter(retriablePredicate))
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw e;
            }
            throw new ServiceException(failureMessage, e);
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            Throwable unwrapped = Exceptions.unwrap(e);
            WebClientResponseException webClientResponseException = findWebClientResponseException(unwrapped);
            if (webClientResponseException != null) {
                if (webClientResponseException.getStatusCode().is4xxClientError()) {
                    throw webClientResponseException;
                }
                throw new ServiceException(failureMessage, webClientResponseException);
            }
            throw new ServiceException(failureMessage, e);
        } catch (Exception e) {
            throw new ServiceException(failureMessage, e);
        }
    }

    private WebClientResponseException findWebClientResponseException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientResponseException) {
                return webClientResponseException;
            }
            current = current.getCause();
        }
        return null;
    }

    private static boolean isDefaultRetriable(Throwable throwable) {
        return throwable instanceof WebClientRequestException || throwable instanceof ServiceException;
    }
}
