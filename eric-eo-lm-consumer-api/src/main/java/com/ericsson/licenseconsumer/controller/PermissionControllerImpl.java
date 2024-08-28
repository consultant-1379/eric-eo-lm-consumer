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

import com.ericsson.licenseconsumer.datalayer.exception.LicenseNotFoundException;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.datalayer.service.LicenseDbService;
import com.ericsson.licenseconsumer.job.PollLicensesJobService;
import com.ericsson.licenseconsumer.mapping.service.LicenseMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@Profile("!stub")
@RestController
@RequestMapping("/lc/v1")
public class PermissionControllerImpl implements PermissionController {

    private final LicenseMappingService licenseMappingService;
    private final LicenseDbService licenseDbService;
    private final PollLicensesJobService pollLicensesJobService;

    @Autowired
    public PermissionControllerImpl(LicenseMappingService licenseMappingService,
                                    LicenseDbService licenseDbService,
                                    PollLicensesJobService pollLicensesJobService) {
        this.licenseMappingService = licenseMappingService;
        this.licenseDbService = licenseDbService;
        this.pollLicensesJobService = pollLicensesJobService;
    }

    @GetMapping(value = "/{application}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> listPermissions(@PathVariable("application") String application) {
        var license = getStoredLicense(application);
        if (license == null) {
            license = getLicenseFromLicenseManager(application);
        }
        if (license != null) {
            var permissions = licenseMappingService.mapLicenseToPermissions(application, license.getLicenseKeyId());
            if (permissions != null && !permissions.isEmpty()) {
                return ResponseEntity.ok(permissions);
            }
        }
        LOGGER.info("No licenses found");
        return ResponseEntity.ok(Collections.emptyList());
    }

    private LicenseToLKFMapping getLicenseFromLicenseManager(final String application) {
        LicenseToLKFMapping license = null;
        try {
            LOGGER.info("Fetching license from License Manager");
            license = pollLicensesJobService.processLicenseForApplication(application);
            LOGGER.info("Successfully fetched license from License Manager: {}", license);
        } catch (LicenseNotFoundException e) {
            LOGGER.error("No licenses for application: " + application, e);
        }
        return license;
    }

    private LicenseToLKFMapping getStoredLicense(final String application) {
        LicenseToLKFMapping license = null;
        try {
            LOGGER.info("Fetching stored license from database");
            license = licenseDbService.findByApplicationAndMap(ApplicationType.fromString(application).getName());
            LOGGER.info("Successfully fetched license from database: {}", license);
        } catch (DataAccessResourceFailureException e) {
            LOGGER.error("Failed to find license due to database connection issues", e);
        }
        return license;
    }
}
