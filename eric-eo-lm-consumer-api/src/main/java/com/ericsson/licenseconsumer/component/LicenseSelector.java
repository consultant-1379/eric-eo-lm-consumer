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

import com.ericsson.licenseconsumer.datalayer.exception.LicenseNotFoundException;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LicenseSelector {

    private final boolean isGeoRed;

    public LicenseSelector(@Value("${license-manager.isGeoRed:false}") final boolean isGeoRed) {
        this.isGeoRed = isGeoRed;
    }

    public LicenseToLKFMapping selectLicense(final String application, final Set<LicenseToLKFMapping> licenses) {
        Objects.requireNonNull(licenses);
        return isGeoRed
                ? selectGeoRedLicense(application, licenses)
                : selectLicenseByPriority(application, licenses);
    }

    private static LicenseToLKFMapping selectLicenseByPriority(final String application, final Set<LicenseToLKFMapping> licenses) {
        LOGGER.info("Selecting license for application: {}", application);
        var orderedLicenses = new TreeSet<>(Comparator.comparing(LicenseToLKFMapping::getPriority));
        orderedLicenses.addAll(licenses.stream()
                .filter(l -> ApplicationType.fromString(application).equals(l.getApplicationType()))
                .collect(Collectors.toList()));

        return orderedLicenses.stream()
                .findFirst()
                .orElseThrow(() -> new LicenseNotFoundException(application));
    }

    private static LicenseToLKFMapping selectGeoRedLicense(final String application, final Set<LicenseToLKFMapping> licenses) {
        LOGGER.info("Selecting Geo Red license for application: {}", application);
        return licenses.stream()
                .filter(l -> l.equals(getGeoRedLicenseByApplication(application)))
                .findFirst()
                .orElseThrow(() -> new LicenseNotFoundException(application));
    }

    private static LicenseToLKFMapping getGeoRedLicenseByApplication(final String application) {
        var applicationType = ApplicationType.fromString(application);
        return ApplicationType.CVNFM.equals(applicationType)
                ? LicenseToLKFMapping.CVNFM_GEORED_KEY
                : LicenseToLKFMapping.VM_VNFM_GEORED_KEY;
    }
}
