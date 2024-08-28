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

import com.ericsson.licenseconsumer.component.LicenseProvider;
import com.ericsson.licenseconsumer.component.LicenseSelector;
import com.ericsson.licenseconsumer.datalayer.entity.LicenseKey;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.datalayer.repository.LicenseKeyRepository;
import com.ericsson.licenseconsumer.datalayer.service.LicenseDbService;
import com.ericsson.licenseconsumer.datalayer.service.LicenseDbServiceImpl;
import com.ericsson.licenseconsumer.integration.utils.LicenseBuilderUtils;
import com.ericsson.licenseconsumer.job.PollLicensesJobService;
import com.ericsson.licenseconsumer.job.PollLicensesJobServiceImpl;
import com.ericsson.licenseconsumer.lm.integration.LicenseConsumerApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.support.CronExpression;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static com.ericsson.licenseconsumer.lm.integration.model.request.ProductTypeLicenseRequest.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PollLicensesJobServiceImplTest {

    @Autowired
    private LicenseDbService licenceService;

    @Autowired
    private PollLicensesJobService pollLicensesJobService;

    @Mock
    private LicenseProvider licenseProvider;

    @Test
    void shouldCallApi() throws IOException, URISyntaxException {

        /* TEST DATA PREPARATION */
        var licenseData = LicenseBuilderUtils.buildLMResponse(LicenseToLKFMapping.CVNFM_FULL_KEY);

        /* TEST DATA PREPARATION FINISHED. MOCK SERVICES */
        var licenseConsumerApi = Mockito.mock(LicenseConsumerApi.class);
        Mockito.when(licenseConsumerApi.getLicenses(builder().productType("Ericsson_Orchestrator").build()))
                .thenReturn(licenseData);

        var licenseRepository = Mockito.mock(LicenseKeyRepository.class);
        var licenceService = new LicenseDbServiceImpl(licenseRepository);
        var licenseProvider = new LicenseProvider(licenseConsumerApi, new LicenseSelector(false));
        /* MOCK SERVICES FINISHED */

        pollLicensesJobService = new PollLicensesJobServiceImpl(licenceService, licenseProvider, new String[] {"Ericsson_Orchestrator"});
        pollLicensesJobService.processAllLicenses();
        verify(licenseConsumerApi).getLicenses(builder().productType("Ericsson_Orchestrator").build());
    }

    @Test
    void processForApplicationShouldCallApi() throws IOException, URISyntaxException {

        /* TEST DATA PREPARATION */
        var licenseData = LicenseBuilderUtils.buildLMResponse(LicenseToLKFMapping.CVNFM_FULL_KEY);

        /* TEST DATA PREPARATION FINISHED. MOCK SERVICES */
        var licenseConsumerApi = Mockito.mock(LicenseConsumerApi.class);
        Mockito.when(licenseConsumerApi.getLicenses(builder().productType("Ericsson_Orchestrator").build()))
                .thenReturn(licenseData);

        var licenseRepository = Mockito.mock(LicenseKeyRepository.class);
        var licenceService = new LicenseDbServiceImpl(licenseRepository);
        var licenseProvider = new LicenseProvider(licenseConsumerApi, new LicenseSelector(false));
        /* MOCK SERVICES FINISHED */

        pollLicensesJobService = new PollLicensesJobServiceImpl(licenceService, licenseProvider, new String[] {"Ericsson_Orchestrator"});
        pollLicensesJobService.processLicenseForApplication(ApplicationType.CVNFM.getName());
        verify(licenseConsumerApi).getLicenses(builder().productType("Ericsson_Orchestrator").build());
    }

    @Test
    void checkCronExpression() throws IOException {
        Properties prop = new Properties();
        var mainPropPath = Paths.get("src", "main", "resources", "application.yml");

        try (InputStream is = Files.newInputStream(mainPropPath)) {
            prop.load(is);
        }
        String cron = prop.getProperty("cron");

        Assertions.assertTrue(CronExpression.isValidExpression(cron));

        CronExpression cronExpression = CronExpression.parse(cron);
        LocalDateTime start = LocalDateTime.of(2022, 9, 19, 9, 0);
        LocalDateTime finish = LocalDateTime.of(2022, 9, 19, 9, 30);
        Assertions.assertTrue(Objects.requireNonNull(cronExpression.next(start)).isEqual(finish));
    }

    @Test
    void checkCronProcessWhenDataIntegrityViolation() throws IOException, URISyntaxException {
        LicenseProvider licenseProvider = Mockito.mock(LicenseProvider.class);
        LicenseDbService licenseDbService = Mockito.mock(LicenseDbService.class);

        Mockito.when(licenseProvider.getLicensesFromLicenseManager("Ericsson_Orchestrator"))
                .thenReturn(Set.of(LicenseToLKFMapping.CVNFM_FULL_KEY));
        LicenseKey licenseKey = new LicenseKey(LicenseToLKFMapping.CVNFM_FULL_KEY.getLicenseKeyId(), ApplicationType.CVNFM.getName());

        Mockito.when(licenseDbService.findByApplication(ApplicationType.CVNFM.getName())).thenReturn(licenseKey);
        Mockito.when(licenseDbService.saveLicense(ApplicationType.CVNFM, LicenseToLKFMapping.CVNFM_FULL_KEY))
                .thenThrow(new DataIntegrityViolationException(""));
        pollLicensesJobService = new PollLicensesJobServiceImpl(licenseDbService, licenseProvider, new String[] {"Ericsson_Orchestrator"});

        var actualLicense = pollLicensesJobService.processAllLicenses();

        assertNotNull(actualLicense);
        assertEquals(Set.of(LicenseToLKFMapping.CVNFM_FULL_KEY), actualLicense);
    }
}
