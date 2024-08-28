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
package com.ericsson.licenseconsumer.integration.component;

import com.ericsson.licenseconsumer.component.LicenseProvider;
import com.ericsson.licenseconsumer.component.LicenseSelector;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.lm.integration.LicenseConsumerApi;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseData;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseInfo;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseModel;
import com.ericsson.licenseconsumer.lm.integration.model.OperationalStatusInfo;
import com.ericsson.licenseconsumer.lm.integration.model.request.ProductTypeLicenseRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.*;
import static com.ericsson.licenseconsumer.lm.integration.model.LicenseStatus.NOT_FOUND;
import static com.ericsson.licenseconsumer.lm.integration.model.LicenseStatus.VALID;
import static com.ericsson.licenseconsumer.lm.integration.model.LicenseStatus.VALID_IN_FUTURE;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
class LicenseProviderTest {

    private static final String CVNFM = ApplicationType.CVNFM.getName();
    private static final String PRODUCT_TYPE = "Ericsson_Orchestrator";

    @Mock
    private LicenseConsumerApi licenseConsumerApi;
    @Captor
    private ArgumentCaptor<Set<LicenseToLKFMapping>> licenseSetCaptor;

    @Test
    void successEnsuredLicenseReturnLicense() throws IOException, URISyntaxException {
        LicenseProvider licenseProvider = new LicenseProvider(licenseConsumerApi, new LicenseSelector(false));
        when(licenseConsumerApi.getLicenses(ProductTypeLicenseRequest.builder().productType(PRODUCT_TYPE).build()))
                .thenReturn(buildLicenseData(CVNFM_FULL_KEY, CVNFM_LIMITED_KEY));

        var actualLicense = licenseProvider.getLicensesFromLicenseManager("Ericsson_Orchestrator");
        await().atMost(60, TimeUnit.SECONDS).until(didTheThing());
        assertEquals(CVNFM_FULL_KEY, actualLicense.stream().findFirst().orElse(null));
    }

    @Test
    void failedEnsuredLicenseReturnLicense() throws IOException, URISyntaxException {
        LicenseProvider licenseProvider = new LicenseProvider(licenseConsumerApi, new LicenseSelector(false));
        when(licenseConsumerApi.getLicenses(ProductTypeLicenseRequest.builder().productType(PRODUCT_TYPE).build()))
                .thenReturn(buildLicenseData(CVNFM_FULL_KEY, CVNFM_LIMITED_KEY));
        when(licenseConsumerApi.ensureLicenses(any()))
                .thenThrow(URISyntaxException.class);

        var actualLicense = licenseProvider.getLicensesFromLicenseManager(PRODUCT_TYPE);
        await().atMost(60, TimeUnit.SECONDS).until(didTheThing());
        assertEquals(CVNFM_FULL_KEY, actualLicense.stream().findFirst().orElse(null));
    }

    @Test
    void testLicensesWithNonProcessableStatusesAreFiltered() throws IOException, URISyntaxException {
        LicenseSelector licenseSelector = mock(LicenseSelector.class);
        LicenseProvider licenseProvider = new LicenseProvider(licenseConsumerApi, licenseSelector);
        when(licenseConsumerApi.getLicenses(ProductTypeLicenseRequest.builder().productType(PRODUCT_TYPE).build()))
                .thenReturn(buildLicensesWithCustomStatuses());
        var actualLicense = licenseProvider.getLicensesFromLicenseManager(PRODUCT_TYPE);
        verify(licenseSelector).selectLicense(anyString(), licenseSetCaptor.capture());
        Set<LicenseToLKFMapping> licenseSet = licenseSetCaptor.getValue();
        assertEquals(1, licenseSet.size());
        assertTrue(licenseSet.stream().allMatch(license -> license.equals(CVNFM_LIMITED_KEY)));
    }

    private Callable<Boolean> didTheThing() {
        return () -> {
            try {
                verify(licenseConsumerApi).ensureLicenses(any());
                return true;
            } catch (Exception e) {
                return false;
            }
        };
    }

    private LicenseData buildLicenseData(LicenseToLKFMapping... licenses) {
        var infoList = new ArrayList<LicenseInfo>();
        Arrays.stream(licenses).forEach(l ->
                infoList.add(LicenseInfo.builder().licenseStatus(VALID.toString()).license(buildLicense(l)).build()));

        return buildCommonLicenseData(infoList);
    }

    private LicenseData buildLicensesWithCustomStatuses() {
        ArrayList<LicenseInfo> licenseInfoList = new ArrayList<>();
        licenseInfoList.add(LicenseInfo.builder().license(buildLicense(CVNFM_LIMITED_KEY))
                                    .licenseStatus(VALID.toString()).build());
        licenseInfoList.add(LicenseInfo.builder().license(buildLicense(CVNFM_FULL_KEY))
                                    .licenseStatus(VALID_IN_FUTURE.toString()).build());
        licenseInfoList.add(LicenseInfo.builder().license(buildLicense(CVNFM_GEORED_KEY))
                                    .licenseStatus(NOT_FOUND.toString()).build());

        return buildCommonLicenseData(licenseInfoList);
    }

    private LicenseData buildCommonLicenseData(final List<LicenseInfo> licenseInfoList) {
        return LicenseData.builder()
                .usageReportPeriodPeak(1)
                .licenseRequestPeriod(2)
                .usageReportPeriodCumulative(3)
                .licensesInfo(licenseInfoList)
                .operationalStatusInfo(new OperationalStatusInfo("operationalMode", 5))
                .build();
    }

    private LicenseModel buildLicense(LicenseToLKFMapping license) {
        return LicenseModel.builder()
                .keyId(license.getLicenseKeyId())
                .type("type")
                .build();
    }
}
