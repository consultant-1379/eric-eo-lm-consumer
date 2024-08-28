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
package com.ericsson.licenseconsumer.component;

import static com.ericsson.licenseconsumer.lm.integration.model.LicenseStatus.NOT_FOUND;
import static com.ericsson.licenseconsumer.lm.integration.model.LicenseStatus.VALID_IN_FUTURE;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ericsson.licenseconsumer.datalayer.exception.LicenseNotFoundException;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.lm.integration.LicenseConsumerApi;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseData;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseInfo;
import com.ericsson.licenseconsumer.lm.integration.model.LicenseStatus;
import com.ericsson.licenseconsumer.lm.integration.model.request.ProductTypeLicenseRequest;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LicenseProvider {

    private static final List<LicenseStatus> NON_PROCESSABLE_STATUSES = List.of(NOT_FOUND, VALID_IN_FUTURE);

    private final LicenseConsumerApi licenseConsumerApi;
    private final LicenseSelector licenseSelector;

    public LicenseProvider(final LicenseConsumerApi licenseConsumerApi, final LicenseSelector licenseSelector) {
        this.licenseConsumerApi = licenseConsumerApi;
        this.licenseSelector = licenseSelector;
    }

    public Set<LicenseToLKFMapping> getLicensesFromLicenseManager(final String productType) throws IOException, URISyntaxException {
        LOGGER.info("Fetching licenses from License Manager");
        var licenses = licenseConsumerApi.getLicenses(ProductTypeLicenseRequest.builder().productType(productType).build());
        LOGGER.info("Successfully fetched licenses: {}", licenses);
        var licenseSet = licenses.getLicensesInfo()
                .stream()
                .filter(licenseInfo -> !NON_PROCESSABLE_STATUSES.contains(LicenseStatus.valueOf(licenseInfo.getLicenseStatus())))
                .map(licenseData -> LicenseToLKFMapping.fromString(licenseData.getLicense().getKeyId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        var setOfApplications = licenseSet.stream()
                .map(LicenseToLKFMapping::getApplicationType)
                .collect(Collectors.toSet());
        LOGGER.info("Processing licenses for applications: {}", setOfApplications);
        Set<LicenseToLKFMapping> setOfSelectedLicenses = new HashSet<>();
        for (ApplicationType application : setOfApplications) {
            selectLicenseForApplication(productType, licenses, licenseSet, setOfSelectedLicenses, application);
        }
        return setOfSelectedLicenses;
    }

    private void selectLicenseForApplication(final String productType,
                           final LicenseData licenses,
                           final Set<LicenseToLKFMapping> licenseSet,
                           final Set<LicenseToLKFMapping> setOfSelectedLicenses,
                           final ApplicationType application) {
        try {
            var selectedLicense = licenseSelector.selectLicense(application.getName(), licenseSet);
            LOGGER.info("Selected license: {}", selectedLicense);
            CompletableFuture.runAsync(() -> ensureLicense(licenses, selectedLicense, productType));
            setOfSelectedLicenses.add(selectedLicense);
        } catch (LicenseNotFoundException e) {
            LOGGER.error(String.format("No licenses found for application '%s'", application.getName()), e);
        }
    }

    private void ensureLicense(final LicenseData licenses, final LicenseToLKFMapping selectedLicense, final String productType) {
        try {
            LOGGER.info("Ensuring license \"{}\" for application \"{}\"", selectedLicense.getLicenseKeyId(), selectedLicense.getApplicationType());
            var licenseModels = licenses.getLicensesInfo().stream()
                    .filter(licenseData -> selectedLicense.getLicenseKeyId().equals(licenseData.getLicense().getKeyId()))
                    .map(LicenseInfo::getLicense)
                    .collect(Collectors.toList());
            licenseConsumerApi.ensureLicenses(ProductTypeLicenseRequest.builder()
                                                      .productType(productType)
                                                      .licenses(licenseModels)
                                                      .build());
            LOGGER.info("Successfully ensured license \"{}\" for application \"{}\"",
                    selectedLicense.getLicenseKeyId(), selectedLicense.getApplicationType());
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Failed to connect to License manager: ", e);
            LOGGER.warn("Failed to ensure license + \"" + selectedLicense.getLicenseKeyId() + "\" for application \""
                                + selectedLicense.getApplicationType() + "\"");
        }
    }
}
