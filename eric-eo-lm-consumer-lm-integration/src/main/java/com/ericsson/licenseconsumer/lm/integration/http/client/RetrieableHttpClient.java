/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.licenseconsumer.lm.integration.http.client;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.net.ssl.SSLHandshakeException;

import com.ericsson.licenseconsumer.lm.integration.http.client.exception.HttpInvocationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RetrieableHttpClient<T> {

    private static final Predicate<Throwable> DEFAULT_RETRY_ON_THROWABLE = ex -> ex instanceof IOException;
    private static final Predicate<HttpResponse> DEFAULT_RETRY_ON_RESPONSE = resp -> resp.statusCode() >= 500;
    private static final Predicate<Throwable> RETRY_ON_SSL_EXCEPTION = ex -> ex instanceof SSLHandshakeException;
    private static final BodyHandler<String> DEFAULT_BODY_HANDLER = HttpResponse.BodyHandlers.ofString();
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_IN_SECONDS = 3;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpClient client;
    private final HttpRequest request;
    private final BodyHandler<T> handler;
    private Executor delayedExecutor;
    private final int maxAttempts;

    private final AtomicInteger attempts = new AtomicInteger();

    private RetrieableHttpClient(Builder<T> builder) {
        this.client = builder.client != null
                ? builder.client : HttpClient.newHttpClient();
        this.request = builder.request;
        this.handler = builder.bodyHandler;
        this.maxAttempts = builder.maxAttempts > 0 ? builder.maxAttempts : DEFAULT_MAX_ATTEMPTS;

        long sleepValue = builder.sleepDelay > 0 ? builder.sleepDelay : DEFAULT_DELAY_IN_SECONDS;
        this.delayedExecutor = CompletableFuture.delayedExecutor(
                Duration.ofSeconds(sleepValue).toMillis(), TimeUnit.MILLISECONDS);
    }

    public T invokeAndGetBody() {
        try {
            HttpResponse<T> httpResponse = invoke().get(1, TimeUnit.SECONDS);
            return httpResponse.body();
        } catch (InterruptedException ex) {
            LOGGER.error("Exception with thread sleeping during retry logic processing: ", ex);
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            LOGGER.error("Exception occurred during request invocation: ", e);
        }
        throw new HttpInvocationException("Request invocation failed.");
    }

    public CompletableFuture<HttpResponse<T>> invoke() {
        attempts.incrementAndGet();
        return client.sendAsync(request, handler)
                .thenApply(resp -> {
                    if (DEFAULT_RETRY_ON_RESPONSE.test(resp)) {
                        checkRetryHeaderIsPresent(resp);
                        return attemptRetry(null);
                    } else {
                        return CompletableFuture.completedFuture(resp);
                    }
                })
                .exceptionally(ex -> {
                    if (RETRY_ON_SSL_EXCEPTION.test(ex.getCause())) {
                        LOGGER.warn("Could not establish SSL connection. Certificates are not provided or expired: ", ex.getCause());
                        return attemptRetry(ex);
                    } else if (DEFAULT_RETRY_ON_THROWABLE.test(ex.getCause())) {
                        return attemptRetry(ex);
                    } else {
                        return CompletableFuture.failedFuture(ex);
                    }
                })
                .thenCompose(Function.identity());
    }

    private void checkRetryHeaderIsPresent(final HttpResponse<T> resp) {
        var headers = resp.headers();
        var delay = headers.firstValueAsLong(RETRY_AFTER_HEADER);
        if (delay.isPresent()) {
            LOGGER.debug("{} header is present in response. Retrying after {} seconds", RETRY_AFTER_HEADER, delay.getAsLong());
            delayedExecutor = CompletableFuture.delayedExecutor(delay.getAsLong(), TimeUnit.SECONDS);
        }
    }

    private CompletableFuture<HttpResponse<T>> attemptRetry(Throwable throwable) {
        if (attemptsRemains()) {
            LOGGER.warn("Retrying: attempt={} path={}", attempts.get(), request.uri());
            return CompletableFuture.supplyAsync(this::invoke, delayedExecutor)
                    .thenCompose(Function.identity());
        } else {
            return handleRetryExceeded(throwable);
        }
    }

    private CompletableFuture<HttpResponse<T>> handleRetryExceeded(Throwable throwable) { // NOSONAR
        if (throwable != null) {
            return CompletableFuture.failedFuture(new HttpInvocationException(throwable));
        } else {
            throw new HttpInvocationException();
        }
    }

    private boolean attemptsRemains() {
        return attempts.get() < maxAttempts;
    }

    public static Builder<String> builder(HttpRequest request) {
        return new Builder<>(request, DEFAULT_BODY_HANDLER);
    }

    public static <T> Builder<T> builder(HttpRequest request, BodyHandler<T> bodyHandler) {
        return new Builder<>(request, bodyHandler);
    }

    public static final class Builder<T> {
        private final HttpRequest request;
        private final BodyHandler<T> bodyHandler;
        private HttpClient client;
        private int maxAttempts;
        private long sleepDelay;

        public Builder(HttpRequest request, BodyHandler<T> bodyHandler) {
            this.request = request;
            this.bodyHandler = bodyHandler;
        }

        public Builder<T> withHttpClient(HttpClient client) {
            this.client = client;
            return this;
        }

        public Builder<T> withMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder<T> withSleepDelay(long sleepDelay) {
            this.sleepDelay = sleepDelay;
            return this;
        }

        public RetrieableHttpClient<T> build() {
            return new RetrieableHttpClient<>(this);
        }
    }
}
