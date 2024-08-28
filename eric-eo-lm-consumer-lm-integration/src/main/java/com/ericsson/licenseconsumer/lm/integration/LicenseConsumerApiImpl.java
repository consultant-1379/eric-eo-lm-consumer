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
package com.ericsson.licenseconsumer.lm.integration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.ericsson.licenseconsumer.lm.integration.http.client.HttpClientAdapter;
import com.ericsson.licenseconsumer.lm.integration.http.client.model.HttpRequestModel;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseData;
import com.ericsson.licenseconsumer.lm.integration.model.request.ProductTypeLicenseRequest;
import com.ericsson.licenseconsumer.lm.integration.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class LicenseConsumerApiImpl implements LicenseConsumerApi {

    @Value("${license-manager.url}")
    private String licenseManagerHost;

    private final HttpClientAdapter httpClient;
    private final JsonUtil jsonUtil;

    public LicenseConsumerApiImpl(final HttpClientAdapter httpClient, final JsonUtil jsonUtil) {
        this.httpClient = httpClient;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public LicenseData getLicenses(final ProductTypeLicenseRequest productTypeRequest) throws IOException,
            URISyntaxException {
        var model = HttpRequestModel.builder()
                .body(jsonUtil.serializeJson(productTypeRequest))
                .headers(Map.ofEntries(
                        Map.entry("Content-type", "application/json")))
                .host(licenseManagerHost)
                .method(HttpMethod.GET.name())
                .path("/license-manager/api/v1/licenses")
                .build();

        return handleHttpResponse(httpClient.sendHttpRequest(model));
    }

    @Override
    public LicenseData ensureLicenses(final ProductTypeLicenseRequest productTypeLicenseRequest) throws IOException,
            URISyntaxException {
        var model = HttpRequestModel.builder()
                .body(jsonUtil.serializeJson(productTypeLicenseRequest))
                .headers(Map.ofEntries(
                        Map.entry("Content-type", "application/json")))
                .host(licenseManagerHost)
                .method(HttpMethod.POST.name())
                .path("/license-manager/api/v1/licenses/requests")
                .build();

        return handleHttpResponse(httpClient.sendHttpRequest(model));
    }

    private LicenseData handleHttpResponse(String responseBody) throws JsonProcessingException {
        return jsonUtil.deserializeJson(responseBody, LicenseData.class);
    }
}
