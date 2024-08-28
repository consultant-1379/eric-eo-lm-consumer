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
package com.ericsson.licenseconsumer.integration.config;

import com.ericsson.licenseconsumer.lm.integration.LicenseConsumerApi;
import com.ericsson.licenseconsumer.lm.integration.LicenseConsumerApiImpl;
import com.ericsson.licenseconsumer.lm.integration.http.client.HttpClientAdapter;
import com.ericsson.licenseconsumer.lm.integration.util.JsonUtil;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@TestConfiguration
public class HttpClientAdapterMockTestConfig {
    private HttpClientAdapter httpClientAdapter = httpClientAdapter();

    @Autowired
    private JsonUtil jsonUtil;

    private HttpClientAdapter httpClientAdapter() {
        return Mockito.mock(HttpClientAdapter.class);
    }

    @Bean
    @Primary
    public LicenseConsumerApi licenseConsumerApi() {
        return new LicenseConsumerApiImpl(httpClientAdapter, jsonUtil);
    }

    @Component
    public class Mock {
        public HttpClientAdapter getHttpClientProxy() {
            return httpClientAdapter;
        }
    }

}
