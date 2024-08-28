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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

import com.ericsson.licenseconsumer.lm.integration.http.client.model.HttpRequestModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpClientAdapterImpl implements HttpClientAdapter {

    private final int maxAttempts;
    private final long delay;

    public HttpClientAdapterImpl(@Value("${license-manager.retry.attempt:3}") int maxAttempts,
                                 @Value("${license-manager.retry.delay:5}") long delay) {
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    @Override
    public String sendHttpRequest(final HttpRequestModel model) throws URISyntaxException {
        var request = HttpRequest.newBuilder()
                .method(model.getMethod(), HttpRequest.BodyPublishers.ofString(model.getBody()))
                .uri(new URI(model.getHost() + model.getPath()));
        if (model.getHeaders() != null) {
            model.getHeaders().keySet().forEach(headerKey -> request.setHeader(headerKey, model.getHeaders().get(headerKey)));
        }
        return RetrieableHttpClient.builder(request.build())
                .withMaxAttempts(maxAttempts)
                .withSleepDelay(delay)
                .build()
                .invokeAndGetBody();
    }
}
