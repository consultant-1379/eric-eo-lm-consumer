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
package com.ericsson.licenseconsumer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.licenseconsumer.mapping.service.LicenseMappingService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("stub")
@RestController
@RequestMapping("/lc/v1")
public class StubPermissionController implements PermissionController {

    private final LicenseMappingService licenseMappingService;

    @Autowired
    public StubPermissionController(final LicenseMappingService licenseMappingService) {
        this.licenseMappingService = licenseMappingService;
    }

    @Override
    public ResponseEntity<List<String>> listPermissions(@PathVariable("application") String application) {
        List<String> stubResponse = licenseMappingService.getAllPermissionsByApplication(application);
        LOGGER.info("Stub response returned all licenses: {}", stubResponse);
        return ResponseEntity.ok(stubResponse);
    }

}
