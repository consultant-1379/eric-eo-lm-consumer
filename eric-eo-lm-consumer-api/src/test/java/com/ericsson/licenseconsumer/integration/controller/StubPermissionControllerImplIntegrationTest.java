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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.ericsson.licenseconsumer.datalayer.model.ApplicationType.CVNFM;
import static com.ericsson.licenseconsumer.datalayer.model.ApplicationType.VMVNFM;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_FULL_KEY;
import static com.ericsson.licenseconsumer.mapping.model.Permission.CLUSTER_MANAGEMENT;
import static com.ericsson.licenseconsumer.mapping.model.Permission.ENM_INTEGRATION;
import static com.ericsson.licenseconsumer.mapping.model.Permission.LCM_OPERATIONS;
import static com.ericsson.licenseconsumer.mapping.model.Permission.ONBOARDING;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.licenseconsumer.controller.PermissionController;
import com.ericsson.licenseconsumer.datalayer.exception.ApplicationNotFoundException;
import com.ericsson.licenseconsumer.integration.config.HttpClientAdapterMockTestConfig;
import com.ericsson.licenseconsumer.integration.utils.LicenseBuilderUtils;
import com.ericsson.licenseconsumer.lm.integration.util.JsonUtil;

@ExtendWith(SpringExtension.class)
@Import(HttpClientAdapterMockTestConfig.class)
@SpringBootTest
@ActiveProfiles("stub")
class StubPermissionControllerImplIntegrationTest {

    @Autowired
    private PermissionController permissionController;

    @Autowired
    private HttpClientAdapterMockTestConfig.Mock mock;

    @Autowired
    private JsonUtil jsonUtil;

    @Test
    void getCvnfmPermissionSuccess() throws IOException, URISyntaxException {
        var licenseDataJson = jsonUtil.serializeJson(LicenseBuilderUtils.buildLMResponse(CVNFM_FULL_KEY));
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any())).thenReturn(licenseDataJson);

        var actualResponse = permissionController.listPermissions(CVNFM.getName());

        Assertions.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        Assertions.assertNotNull(actualResponse.getBody());
        List<String> expectedPermissions = List.of(ONBOARDING.getName(),
                                                   ENM_INTEGRATION.getName(),
                                                   LCM_OPERATIONS.getName(),
                                                   CLUSTER_MANAGEMENT.getName());
        assertTrue(actualResponse.getBody().containsAll(expectedPermissions));

    }

    @Test
    void getVmvnfmPermissionSuccess() throws IOException, URISyntaxException {
        var licenseDataJson = jsonUtil.serializeJson(LicenseBuilderUtils.buildLMResponse(CVNFM_FULL_KEY));
        Mockito.when(mock.getHttpClientProxy().sendHttpRequest(Mockito.any())).thenReturn(licenseDataJson);

        var actualResponse = permissionController.listPermissions(VMVNFM.getName());

        Assertions.assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        Assertions.assertNotNull(actualResponse.getBody());
        List<String> expectedPermissions = List.of(ENM_INTEGRATION.getName(), LCM_OPERATIONS.getName());
        assertTrue(actualResponse.getBody().containsAll(expectedPermissions));
    }

    @Test
    void shouldThrowExceptionIfApplicationTypeIsNotSupported() {
        Exception exception = assertThrows(ApplicationNotFoundException.class, () ->
                permissionController.listPermissions("non-existent"));

        assertEquals("No license found by application name 'non-existent'.", exception.getMessage());

        assertThrows(ApplicationNotFoundException.class, () -> permissionController.listPermissions(null));
    }
}