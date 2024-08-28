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
package com.ericsson.licenseconsumer.lm.integration.client;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.ericsson.licenseconsumer.lm.integration.http.client.RetrieableHttpClient;
import com.ericsson.licenseconsumer.lm.integration.http.client.exception.HttpInvocationException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class RetriableHttpClientTest {

    private static final String TEST_ENDPOINT = "/rest/api/latest";
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private static String testUri;

    private static HttpRequest defaultHttpRequest;
    private static MockWebServer mockServer;

    @BeforeEach
    public void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        testUri = mockServer.url(TEST_ENDPOINT).toString();
        defaultHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(testUri))
                .GET()
                .build();
    }

    @AfterEach
    public void cleanUp() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void singleSuccessInvocationWithoutBody() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(200);

        mockServer.enqueue(mockResponse);


        HttpResponse<String> response = RetrieableHttpClient.builder(defaultHttpRequest).build().invoke().get(1, SECONDS);

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertEquals(Strings.EMPTY, response.body());
    }

    @Test
    void singleSuccessInvocationWithBody() throws Exception {
        String body = "Test body";

        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(201);
        mockResponse.setBody(body);

        mockServer.enqueue(mockResponse);
        mockServer.enqueue(mockResponse);

        defaultHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(testUri))
                .method(HttpMethod.GET.name(), HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = RetrieableHttpClient.builder(defaultHttpRequest).build().invoke().get(1, SECONDS);

        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
        assertNotNull(response.body(), "Body shall be not null.");
        assertEquals(body, response.body(), "Response body doesn't match.");
    }

    @Test
    void successfulWithRetry() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(500);

        mockServer.enqueue(mockResponse);
        mockResponse.setResponseCode(201);
        mockServer.enqueue(mockResponse);



        HttpResponse<String> response = RetrieableHttpClient.builder(defaultHttpRequest)
                .withSleepDelay(1L)
                .build().invoke().get(5, SECONDS);

        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
    }

    @Test
    void testWithRetryHeaderReceivedInResponse() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(503);
        mockResponse.setHeader(RETRY_AFTER_HEADER, 10);

        mockServer.enqueue(mockResponse);
        mockResponse.setResponseCode(201);
        mockServer.enqueue(mockResponse);
        mockServer.enqueue(mockResponse);

        HttpResponse<String> response = RetrieableHttpClient.builder(defaultHttpRequest, HttpResponse.BodyHandlers.ofString())
                .build().invoke().get(15, SECONDS);

        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
    }

    @Test
    void testCustomExceptionThrownInCaseOfRetriesExceeded() {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(500);

        mockServer.enqueue(mockResponse);
        mockServer.enqueue(mockResponse);

        assertThrows(HttpInvocationException.class, () ->
                RetrieableHttpClient.builder(defaultHttpRequest)
                        .withSleepDelay(1L)
                        .withMaxAttempts(2)
                        .withHttpClient(HttpClient.newHttpClient())
                        .build()
                        .invokeAndGetBody());
    }

    @Test
    void attemptsExceededDueToTimeoutWithCustomException() {

        defaultHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(testUri))
                .GET()
                .timeout(Duration.ofMillis(10L))
                .build();

        assertThrows(HttpInvocationException.class, () ->
                RetrieableHttpClient.builder(defaultHttpRequest)
                        .withSleepDelay(1L)
                        .build()
                        .invokeAndGetBody());
    }

    @Test
    void testCustomExceptionThrownInCaseOfRetriesExceededWithNullThrowable() {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(500);

        mockServer.enqueue(mockResponse);

        assertThrows(HttpInvocationException.class, () ->
                RetrieableHttpClient.builder(defaultHttpRequest)
                        .withSleepDelay(1L)
                        .withMaxAttempts(1)
                        .withHttpClient(HttpClient.newHttpClient())
                        .build()
                        .invokeAndGetBody());
    }

    @Test
    void testSslExceptionRaisedDuringConnection() {

        var req = HttpRequest.newBuilder()
                .uri(URI.create(testUri))
                .GET()
                .build();

        assertThrows(HttpInvocationException.class, () -> RetrieableHttpClient.builder(req).build().invokeAndGetBody());
    }

}
