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
package com.ericsson.licenseconsumer.integration.controller;

import com.ericsson.licenseconsumer.EricLicenseConsumerApplication;
import com.ericsson.licenseconsumer.component.LicenseProvider;
import com.ericsson.licenseconsumer.controller.PermissionController;
import com.ericsson.licenseconsumer.controller.PermissionControllerImpl;
import com.ericsson.licenseconsumer.datalayer.entity.LicenseKey;
import com.ericsson.licenseconsumer.datalayer.exception.ApplicationNotFoundException;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.service.LicenseDbService;
import com.ericsson.licenseconsumer.datalayer.service.LicenseDbServiceImpl;
import com.ericsson.licenseconsumer.integration.config.HttpClientAdapterMockTestConfig;
import com.ericsson.licenseconsumer.integration.utils.LicenseBuilderUtils;
import com.ericsson.licenseconsumer.job.PollLicensesJobService;
import com.ericsson.licenseconsumer.job.PollLicensesJobServiceImpl;
import com.ericsson.licenseconsumer.lm.integration.util.JsonUtil;
import com.ericsson.licenseconsumer.mapping.service.LicenseMappingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static com.ericsson.licenseconsumer.datalayer.model.ApplicationType.CVNFM;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_FULL_KEY;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_LIMITED_KEY;
import static com.ericsson.licenseconsumer.mapping.model.Permission.CLUSTER_MANAGEMENT;
import static com.ericsson.licenseconsumer.mapping.model.Permission.ENM_INTEGRATION;
import static com.ericsson.licenseconsumer.mapping.model.Permission.LCM_OPERATIONS;
import static com.ericsson.licenseconsumer.mapping.model.Permission.ONBOARDING;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@Import(HttpClientAdapterMockTestConfig.class)
@SpringBootTest(classes = EricLicenseConsumerApplication.class)
class PermissionControllerImplIntegrationTest {

    @Autowired
    private PermissionController permissionController;

    @Autowired
    private LicenseMappingService licenseMappingService;

    @Autowired
    private PollLicensesJobService pollLicensesJobService;

    @Autowired
    private LicenseProvider licenseProvider;

    @Autowired
    private LicenseDbServiceImpl licenseDbService;

    @SpyBean
    private LicenseDbServiceImpl licenseDbServiceMock;

    @Autowired
    private HttpClientAdapterMockTestConfig.Mock mock;

    @Autowired
    private JsonUtil jsonUtil;

    @Test
    void getCvnfmPermissionSuccess() throws IOException, URISyntaxException {
        var licenseDataJson = jsonUtil.serializeJson(LicenseBuilderUtils.buildLMResponse(CVNFM_FULL_KEY));
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any())).thenReturn(licenseDataJson);

        LicenseKey licenseKey = null;
        pollLicensesJobService = new PollLicensesJobServiceImpl(licenseDbService, licenseProvider, new String[] {"Ericsson_Orchestrator"});
        try {
            var actualResponse = permissionController.listPermissions(CVNFM.getName());

            Assertions.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
            Assertions.assertNotNull(actualResponse.getBody());
            assertTrue(actualResponse.getBody().containsAll(List.of(ONBOARDING.getName(),
                                                                    ENM_INTEGRATION.getName(),
                                                                    LCM_OPERATIONS.getName(),
                                                                    CLUSTER_MANAGEMENT.getName())));

            licenseKey = licenseDbService.findByApplication(CVNFM.getName());
            assertNotNull(licenseKey);
            assertEquals(licenseKey.getLicenseKeyId(), CVNFM_FULL_KEY.getLicenseKeyId());
            assertEquals(licenseKey.getApplication(), CVNFM_FULL_KEY.getApplicationType().getName());
        } finally {
            licenseDbService.deleteLicenseKey(licenseKey);
        }
    }

    @Test
    void getCvnfmPermissionSuccessForAlreadyStoredLicense() {
        LicenseKey licenseKey = licenseDbService.saveLicense(CVNFM, CVNFM_LIMITED_KEY);
        try {
            ResponseEntity<List<String>> response = permissionController.listPermissions(CVNFM.getName());
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

            List<String> permissions = response.getBody();
            assertNotNull(permissions);
            assertTrue(permissions.containsAll(List.of(ONBOARDING.getName(),
                                                       LCM_OPERATIONS.getName(),
                                                       CLUSTER_MANAGEMENT.getName())));
            assertFalse(permissions.contains(ENM_INTEGRATION.getName()));
        } finally {
            licenseDbService.deleteLicenseKey(licenseKey);
        }
    }

    @Test
    void getPermissionWhenLMGivesAnEmptyObject() throws IOException, URISyntaxException {
        String licenseDataJson = jsonUtil.serializeJson(LicenseBuilderUtils.buildEmptyLicenseData());
        pollLicensesJobService = new PollLicensesJobServiceImpl(licenseDbService, licenseProvider, new String[] {"Ericsson_Orchestrator"});
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any())).thenReturn(licenseDataJson);

        ResponseEntity<List<String>> response = permissionController.listPermissions(CVNFM.getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        List<String> permissions = response.getBody();
        assertNotNull(permissions);
        assertTrue(permissions.isEmpty());
    }

    @Test
    void shouldThrowExceptionIfApplicationTypeIsNotSupported() {
        Exception exception = assertThrows(ApplicationNotFoundException.class, () ->
                permissionController.listPermissions("non-existent"));
        assertEquals("No license found by application name 'non-existent'.", exception.getMessage());

        assertThrows(ApplicationNotFoundException.class, () -> {
            permissionController.listPermissions(null);
        });
    }

    @Test
    void shouldReturnPermissionsFromLMWhenDbInaccessible() throws URISyntaxException, JsonProcessingException {
        var licenseDataJson = jsonUtil.serializeJson(LicenseBuilderUtils.buildLMResponse(CVNFM_FULL_KEY));
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any())).thenReturn(licenseDataJson);
        Mockito.when(licenseDbServiceMock.findByApplicationAndMap(CVNFM.getName()))
                .thenThrow(new DataAccessResourceFailureException(""));
        Mockito.when(licenseDbServiceMock.findByApplication(CVNFM.getName()))
                .thenThrow(new DataAccessResourceFailureException(""))
                .thenCallRealMethod();
        pollLicensesJobService = new PollLicensesJobServiceImpl(licenseDbService, licenseProvider, new String[] {"Ericsson_Orchestrator"});

        var actualResponse = permissionController.listPermissions(CVNFM.getName());

        Assertions.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        Assertions.assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody().containsAll(List.of(ONBOARDING.getName(),
                                                                ENM_INTEGRATION.getName(),
                                                                LCM_OPERATIONS.getName(),
                                                                CLUSTER_MANAGEMENT.getName())));
        var licenseKey = licenseDbService.findByApplication(CVNFM.getName());
        assertNull(licenseKey);
    }

    @Test
    void shouldReturnEmptyListWhenLicenseIsNotFound() {
        Mockito.when(licenseDbServiceMock.findByApplicationAndMap(CVNFM.getName())).thenReturn(null);
        PollLicensesJobServiceImpl pollLicensesJobServiceImpl = Mockito.mock(PollLicensesJobServiceImpl.class);
        Mockito.when(pollLicensesJobServiceImpl.processAllLicenses()).thenReturn(null);
        permissionController = new PermissionControllerImpl(licenseMappingService, licenseDbService, pollLicensesJobServiceImpl);

        ResponseEntity<List<String>> actualResponse = permissionController.listPermissions(CVNFM.getName());
        assertTrue(actualResponse.getBody().isEmpty());
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    void shouldThrowLicenseNotFoundExceptionWithoutStoreInDatabase() throws IOException, URISyntaxException {
        LicenseDbService licenseDbService = Mockito.mock(LicenseDbService.class);
        LicenseProvider licenseProvider = Mockito.mock(LicenseProvider.class);

        Mockito.when(licenseDbService.findByApplicationAndMap(ApplicationType.CVNFM.getName())).thenReturn(null);
        Mockito.when(licenseProvider.getLicensesFromLicenseManager("Ericsson_Orchestrator"))
                .thenReturn(Collections.emptySet());

        pollLicensesJobService = new PollLicensesJobServiceImpl(licenseDbService, licenseProvider, new String[] {"Ericsson_Orchestrator"});

        permissionController = new PermissionControllerImpl(licenseMappingService, licenseDbService, pollLicensesJobService);

        ResponseEntity<List<String>> response = permissionController.listPermissions(CVNFM.getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        List<String> permissions = response.getBody();
        assertNotNull(permissions);
        assertTrue(permissions.isEmpty());

        verify(licenseProvider, times(1))
                .getLicensesFromLicenseManager("Ericsson_Orchestrator");
        verify(licenseDbService).findByApplicationAndMap(ApplicationType.CVNFM.getName());
    }

    @Test
    void shouldReturnEmptyListWhenCvnfmPermissionNull() {
        LicenseDbService licenseDbService = Mockito.mock(LicenseDbServiceImpl.class);
        Mockito.when(licenseDbService.findByApplicationAndMap(CVNFM.getName())).thenReturn(CVNFM_FULL_KEY);

        LicenseMappingService licenseMappingService = Mockito.mock(LicenseMappingService.class);
        Mockito.when(licenseMappingService.mapLicenseToPermissions(CVNFM.getName(), CVNFM_FULL_KEY.getLicenseKeyId()))
                .thenReturn(null);
        permissionController = new PermissionControllerImpl(licenseMappingService, licenseDbService, pollLicensesJobService);

        ResponseEntity<List<String>> actualResponse = permissionController.listPermissions(CVNFM.getName());
        assertTrue(actualResponse.getBody().isEmpty());
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    void shouldReturnEmptyListWhenCvnfmPermissionEmpty() {
        LicenseDbService licenseDbService = Mockito.mock(LicenseDbServiceImpl.class);
        Mockito.when(licenseDbService.findByApplicationAndMap(CVNFM.getName())).thenReturn(CVNFM_LIMITED_KEY);

        LicenseMappingService licenseMappingService = Mockito.mock(LicenseMappingService.class);
        Mockito.when(licenseMappingService.mapLicenseToPermissions(CVNFM.getName(), CVNFM_LIMITED_KEY.getLicenseKeyId()))
                .thenReturn(Collections.emptyList());
        permissionController = new PermissionControllerImpl(licenseMappingService, licenseDbService, pollLicensesJobService);

        ResponseEntity<List<String>> actualResponse = permissionController.listPermissions(CVNFM.getName());
        assertTrue(actualResponse.getBody().isEmpty());
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }
}