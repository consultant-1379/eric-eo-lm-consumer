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
package com.ericsson.licenseconsumer.contracts.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import com.ericsson.licenseconsumer.EricLicenseConsumerApplication;
import com.ericsson.licenseconsumer.controller.PermissionControllerImpl;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EricLicenseConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext
@AutoConfigureMessageVerifier
public class GetPermissionsBase {

    @BeforeEach
    public void setup() {
        PermissionControllerImpl permissionController = Mockito.mock(PermissionControllerImpl.class);
        ResponseEntity<List<String>> permissionList
                = ResponseEntity.ok(List.of("onboarding", "enm_integration", "lcm_operations", "cluster_management"));
        ResponseEntity<List<String>> notFound = ResponseEntity.notFound().build();
        given(permissionController.listPermissions("cvnfm")).willReturn(permissionList);
        given(permissionController.listPermissions("test")).willReturn(notFound);
        StandaloneMockMvcBuilder standaloneMockMvcBuilder
                = MockMvcBuilders.standaloneSetup(permissionController);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }
}
