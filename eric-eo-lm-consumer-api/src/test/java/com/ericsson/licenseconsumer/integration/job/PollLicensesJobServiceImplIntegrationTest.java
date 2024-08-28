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
package com.ericsson.licenseconsumer.integration.job;

import com.ericsson.licenseconsumer.EricLicenseConsumerApplication;
import com.ericsson.licenseconsumer.component.LicenseProvider;
import com.ericsson.licenseconsumer.datalayer.exception.LicenseNotFoundException;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.datalayer.service.LicenseDbService;
import com.ericsson.licenseconsumer.integration.config.HttpClientAdapterMockTestConfig;
import com.ericsson.licenseconsumer.job.PollLicensesJobService;
import com.ericsson.licenseconsumer.lm.integration.http.client.model.HttpRequestModel;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseData;
import com.ericsson.licenseconsumer.lm.integration.model.request.ProductTypeLicenseRequest;
import com.ericsson.licenseconsumer.lm.integration.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import static com.ericsson.licenseconsumer.datalayer.model.ApplicationType.CVNFM;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_FULL_KEY;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_LIMITED_KEY;
import static com.ericsson.licenseconsumer.integration.utils.LicenseBuilderUtils.buildEmptyLicenseData;
import static com.ericsson.licenseconsumer.integration.utils.LicenseBuilderUtils.buildLMResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@Import(HttpClientAdapterMockTestConfig.class)
@SpringBootTest(classes = EricLicenseConsumerApplication.class)
class PollLicensesJobServiceImplIntegrationTest {

    @Autowired
    private PollLicensesJobService pollLicensesJobService;

    @Autowired
    private LicenseDbService licenseDbService;

    @Autowired
    private HttpClientAdapterMockTestConfig.Mock mock;

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private LicenseProvider licenseProvider;

    @Value("#{'${nels.productTypes}'.split(';')}")
    String[] productTypes;

    @Test
    void checkCronProcessSuccessForCvnfmLicense() throws IOException, URISyntaxException {
        mockLicenseManagerResponseSuccess(buildLMResponse(CVNFM_FULL_KEY));
        pollLicensesJobService.processAllLicenses();

        checkStoredLicenseKey(CVNFM, CVNFM_FULL_KEY);

        ArgumentCaptor<HttpRequestModel> argument = ArgumentCaptor.forClass(HttpRequestModel.class);
        Mockito.verify(mock.getHttpClientProxy(), Mockito.timeout(100).times(2)).sendHttpRequest(argument.capture());
        ProductTypeLicenseRequest productTypeLicenseRequest
                = jsonUtil.deserializeJson(argument.getValue().getBody(), ProductTypeLicenseRequest.class);

        assertEquals(productTypes[0], productTypeLicenseRequest.getProductType());
        assertEquals(CVNFM_FULL_KEY.getLicenseKeyId(), productTypeLicenseRequest.getLicenses().get(0).getKeyId());
    }

    @Test
    void checkLicenseChanged() throws URISyntaxException, JsonProcessingException {
        var licenseDataJson1 = jsonUtil.serializeJson(buildLMResponse(CVNFM_FULL_KEY));
        var licenseDataJson2 = jsonUtil.serializeJson(buildLMResponse(CVNFM_LIMITED_KEY));

        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any()))
                .thenReturn(licenseDataJson1, licenseDataJson2);

        pollLicensesJobService.processAllLicenses();
        checkStoredLicenseKey(CVNFM, CVNFM_FULL_KEY);

        pollLicensesJobService.processAllLicenses();
        checkStoredLicenseKey(CVNFM, CVNFM_LIMITED_KEY);
    }

    @Test
    void checkLicenseRemoved() throws URISyntaxException, JsonProcessingException {
        var licenseDataJson1 = jsonUtil.serializeJson(buildLMResponse(CVNFM_FULL_KEY));
        var licenseDataJson2 = jsonUtil.serializeJson(buildEmptyLicenseData());

        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any()))
                .thenReturn(licenseDataJson1, licenseDataJson2);

        pollLicensesJobService.processAllLicenses();
        checkStoredLicenseKey(CVNFM, CVNFM_FULL_KEY);

        Assertions.assertThrows(LicenseNotFoundException.class, () -> {
            pollLicensesJobService.processAllLicenses();
        });
        assertNull(licenseDbService.findByApplication(CVNFM.getName()));
    }

    @Test
    void checkCronProcessWhenLMGivesAnEmptyObject() throws IOException, URISyntaxException {
        mockLicenseManagerResponseSuccess(buildEmptyLicenseData());

        Assertions.assertThrows(LicenseNotFoundException.class, () -> {
            pollLicensesJobService.processAllLicenses();
        });
    }

    @Test
    void checkCronProcessWithURISyntaxException() throws URISyntaxException {
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any()))
                .thenThrow(new URISyntaxException("", ""));
        Assertions.assertEquals(Collections.emptySet(), pollLicensesJobService.processAllLicenses());
    }

    @Test
    void checkCronProcessWithIOException() throws URISyntaxException {
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any())).thenReturn("hey you");
        Assertions.assertEquals(Collections.emptySet(), pollLicensesJobService.processAllLicenses());
    }

    @Test
    void checkCronProcessForApplicationWithURISyntaxException() throws URISyntaxException {
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any()))
                .thenThrow(new URISyntaxException("", ""));
        Assertions.assertNull(pollLicensesJobService.processLicenseForApplication(CVNFM.getName()));
    }

    @Test
    void checkCronProcessForApplicationAndReturnNull() throws URISyntaxException {
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any())).thenReturn("hey you");
        Assertions.assertNull(pollLicensesJobService.processLicenseForApplication(CVNFM.getName()));
    }

    @AfterEach
    public void resetMock() {
        Mockito.reset(mock.getHttpClientProxy());
        licenseDbService.deleteAll();
    }

    private void mockLicenseManagerResponseSuccess(LicenseData licenseData) throws JsonProcessingException, URISyntaxException {
        var licenseDataJson = jsonUtil.serializeJson(licenseData);
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any())).thenReturn(licenseDataJson);
    }

    private void checkStoredLicenseKey(ApplicationType applicationType, LicenseToLKFMapping expectedLicense) {
        var licenseKey = licenseDbService.findByApplication(applicationType.getName());
        assertEquals(expectedLicense.getLicenseKeyId(), licenseKey.getLicenseKeyId());
    }
}