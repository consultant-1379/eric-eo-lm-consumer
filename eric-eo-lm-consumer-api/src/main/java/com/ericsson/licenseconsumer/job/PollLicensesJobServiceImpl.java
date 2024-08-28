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
package com.ericsson.licenseconsumer.job;

import com.ericsson.licenseconsumer.component.LicenseProvider;
import com.ericsson.licenseconsumer.datalayer.exception.LicenseNotFoundException;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.datalayer.service.LicenseDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PollLicensesJobServiceImpl implements PollLicensesJobService {

    private final LicenseDbService licenseDbService;

    private final LicenseProvider licenseProvider;

    private final String[] productTypes;

    @Value("${license-manager.url}")
    private String licenseManagerHost;

    public PollLicensesJobServiceImpl(final LicenseDbService licenseDbService,
                                      final LicenseProvider licenseProvider,
                                      @Value("#{'${nels.productTypes}'.split(';')}") String[] productTypes) {
        this.licenseDbService = licenseDbService;
        this.licenseProvider = licenseProvider;
        this.productTypes = productTypes;
    }

    @Override
    public Set<LicenseToLKFMapping> processAllLicenses() {
        Set<LicenseToLKFMapping> licenses = new HashSet<>();
        try {
            for (String productType : productTypes) {
                licenses.addAll(licenseProvider.getLicensesFromLicenseManager(productType));
            }
            for (var license : licenses) {
                processLicense(license);
            }
            for (ApplicationType appType : ApplicationType.values()) {
                var existApplicationTypes = licenses.stream()
                        .map(LicenseToLKFMapping::getApplicationType)
                        .collect(Collectors.toSet());
                if (!existApplicationTypes.contains(appType)) {
                    deleteRegisteredLicenseIfPresent(appType);
                }
            }
            if (licenses.isEmpty()) {
                throw new LicenseNotFoundException("No license was received from license Manager");
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to connect to license manager. Bad url {}", licenseManagerHost, e);
        } catch (IOException e) {
            LOGGER.error("Failed to connect to license manager. Something wrong during connection", e);
        }
        return licenses;
    }

    @Override
    public LicenseToLKFMapping processLicenseForApplication(String application) {
        var licenses = this.processAllLicenses();
        return licenses
                .stream()
                .filter(licenseToLKF -> application.equals(licenseToLKF.getApplicationType().getName()))
                .findFirst().orElse(null);
    }

    private void processLicense(LicenseToLKFMapping license) {
        if (license != null) {
            try {
                storeLicense(license);
            } catch (LicenseNotFoundException lnfEx) {
                LOGGER.warn("License not found:", lnfEx);
                deleteRegisteredLicenseIfPresent(license.getApplicationType());
                throw new LicenseNotFoundException(license.getApplicationType().getName());
            } catch (DataIntegrityViolationException e) {
                LOGGER.error("Failed to insert license key due to data integrity violation", e);
            } catch (DataAccessResourceFailureException e) {
                LOGGER.error("Failed to insert license key due to database connection issues", e);
            }
        }
    }

    private void storeLicense(LicenseToLKFMapping license) {
        deleteRegisteredLicenseIfPresent(license.getApplicationType());
        LOGGER.info("Storing license with id: {}", license.getLicenseKeyId());
        licenseDbService.saveLicense(license.getApplicationType(), license);
    }

    private void deleteRegisteredLicenseIfPresent(ApplicationType application) {
        var registeredLicenseKey = licenseDbService.findByApplication(application.getName());
        if (registeredLicenseKey != null) {
            LOGGER.info("Deleting license with id: {}", registeredLicenseKey.getLicenseKeyId());
            licenseDbService.deleteLicenseKey(registeredLicenseKey);
        }
    }
}
