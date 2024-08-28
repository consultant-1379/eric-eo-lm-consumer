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

import com.ericsson.licenseconsumer.lm.integration.http.client.HttpClientAdapter;
import com.ericsson.licenseconsumer.lm.integration.http.client.model.HttpRequestModel;
import com.ericsson.licenseconsumer.lm.integration.model.request.ProductTypeLicenseRequest;
import com.ericsson.licenseconsumer.lm.integration.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseConsumerApiTest {

    @Mock
    private JsonUtil jsonUtil;

    @Mock
    private HttpClientAdapter httpClient;

    @InjectMocks
    private LicenseConsumerApiImpl licenseConsumerApi;

    @Value("${license-manager.url}")
    private String licenseManagerHost;

    private final Map<String, String> headers = Map.ofEntries(
            Map.entry("Content-type", "application/json"));

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        when(jsonUtil.serializeJson(Mockito.any(ProductTypeLicenseRequest.class))).thenReturn(Strings.EMPTY);
    }

    @Test
    void getLicenseRequestFillingTest() throws IOException, URISyntaxException {
        ProductTypeLicenseRequest productL = new ProductTypeLicenseRequest();
        ArgumentCaptor<HttpRequestModel> argument = ArgumentCaptor.forClass(HttpRequestModel.class);

        licenseConsumerApi.getLicenses(productL);

        Mockito.verify(httpClient).sendHttpRequest(argument.capture());
        Assertions.assertEquals(HttpMethod.GET.name(), argument.getValue().getMethod());
        String getLicenseUrl = "/license-manager/api/v1/licenses";
        Assertions.assertEquals(getLicenseUrl, argument.getValue().getPath());
        Assertions.assertEquals(licenseManagerHost, argument.getValue().getHost());
        Assertions.assertEquals(headers, argument.getValue().getHeaders());
    }

    @Test
    void ensureLicenseRequestFillingTest() throws IOException, URISyntaxException {
        ProductTypeLicenseRequest productL = new ProductTypeLicenseRequest();
        ArgumentCaptor<HttpRequestModel> argument = ArgumentCaptor.forClass(HttpRequestModel.class);

        licenseConsumerApi.ensureLicenses(productL);

        Mockito.verify(httpClient).sendHttpRequest(argument.capture());
        Assertions.assertEquals(HttpMethod.POST.name(), argument.getValue().getMethod());
        String ensureLicenseUrl = "/license-manager/api/v1/licenses/requests";
        Assertions.assertEquals(ensureLicenseUrl, argument.getValue().getPath());
        Assertions.assertEquals(licenseManagerHost, argument.getValue().getHost());
        Assertions.assertEquals(headers, argument.getValue().getHeaders());
    }
}
