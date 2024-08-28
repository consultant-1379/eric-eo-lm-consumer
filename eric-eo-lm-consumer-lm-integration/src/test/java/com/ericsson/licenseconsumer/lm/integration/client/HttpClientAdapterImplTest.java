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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericsson.licenseconsumer.lm.integration.http.client.HttpClientAdapterImpl;
import com.ericsson.licenseconsumer.lm.integration.http.client.model.HttpRequestModel;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class HttpClientAdapterImplTest {

    private static final String TEST_ENDPOINT = "/rest/api/latest";
    private static MockWebServer mockServer;

    private HttpClientAdapterImpl httpClientAdapter;

    private static String testUri;


    @BeforeAll
    public static void setUpAll() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        testUri = mockServer.url(TEST_ENDPOINT).toString();
    }

    @BeforeEach
    public void setup() {
        httpClientAdapter = new HttpClientAdapterImpl(3, 5);
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testSendHttpRequestWithoutHeaders() throws URISyntaxException {
        mockServer.enqueue(new MockResponse());

        HttpRequestModel model = HttpRequestModel.builder()
                .method("GET")
                .host(testUri)
                .body("")
                .path("")
                .build();

        String actualResponse = httpClientAdapter.sendHttpRequest(model);
        Assertions.assertEquals("", actualResponse);
    }

    @Test
    void testSendHttpRequestWithHeaders() throws URISyntaxException {
        mockServer.enqueue(new MockResponse());

        HttpRequestModel model = HttpRequestModel.builder()
                .method("GET")
                .host(testUri)
                .headers(Map.of("content", "value"))
                .body("")
                .path("")
                .build();

        String actualResponse = httpClientAdapter.sendHttpRequest(model);
        Assertions.assertEquals("", actualResponse);
    }
}
