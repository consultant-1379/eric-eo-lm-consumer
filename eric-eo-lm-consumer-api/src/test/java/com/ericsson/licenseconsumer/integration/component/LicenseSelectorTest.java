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

import com.ericsson.licenseconsumer.component.LicenseSelector;
import com.ericsson.licenseconsumer.datalayer.exception.LicenseNotFoundException;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class LicenseSelectorTest {

    private LicenseSelector licenseSelector;

    private static final String CVNFM = ApplicationType.CVNFM.getName();
    private static final String VMVNFM = ApplicationType.VMVNFM.getName();

    @Test
    void shouldReturnFullLicenseWhenAllAvailable() {
        licenseSelector = new LicenseSelector(false);
        var licenses = Set.of(CVNFM_FULL_KEY, CVNFM_LIMITED_KEY,
                CVNFM_GEORED_KEY, VM_VNFM_GEORED_KEY, VM_VNFM_LIMITED_KEY);

        var actualLicense = licenseSelector.selectLicense(CVNFM, licenses);
        assertEquals(CVNFM_FULL_KEY, actualLicense);
    }

    @Test
    void shouldReturnGeoRedLicenseWhenFullLicenseIsNotAvailable() {
        licenseSelector = new LicenseSelector(false);
        var licenses = Set.of(CVNFM_LIMITED_KEY, CVNFM_GEORED_KEY, VM_VNFM_FULL_KEY);

        var actualLicense = licenseSelector.selectLicense(CVNFM, licenses);
        assertEquals(CVNFM_GEORED_KEY, actualLicense);
    }

    @Test
    void shouldReturnLimitedLicenseWhenHigherPriorityNotAvailable() {
        licenseSelector = new LicenseSelector(false);
        var licenses = Set.of(CVNFM_LIMITED_KEY, VM_VNFM_FULL_KEY);

        var actualLicense = licenseSelector.selectLicense(CVNFM, licenses);
        assertEquals(CVNFM_LIMITED_KEY, actualLicense);
    }

    @Test
    void shouldReturnGeoRedPermissionWhenGeoRedFlagIsTrue() {
        licenseSelector = new LicenseSelector(true);
        var licenses = Set.of(CVNFM_LIMITED_KEY, CVNFM_FULL_KEY, CVNFM_GEORED_KEY, VM_VNFM_FULL_KEY);

        var actualLicense = licenseSelector.selectLicense(CVNFM, licenses);
        assertEquals(CVNFM_GEORED_KEY, actualLicense);
    }

    @Test
    void shouldReturnVMGeoRedPermissionWhenGeoRedFlagIsTrue() {
        licenseSelector = new LicenseSelector(true);
        var licenses = Set.of(VM_VNFM_GEORED_KEY, CVNFM_FULL_KEY, CVNFM_GEORED_KEY, VM_VNFM_FULL_KEY);

        var actualLicense = licenseSelector.selectLicense(VMVNFM, licenses);
        assertEquals(VM_VNFM_GEORED_KEY, actualLicense);
    }

    @Test
    void shouldReturnVMFullPermissionWhenAllAvailable() {
        licenseSelector = new LicenseSelector(false);
        var licenses = Set.of(CVNFM_LIMITED_KEY, CVNFM_FULL_KEY, CVNFM_GEORED_KEY,
                VM_VNFM_FULL_KEY, VM_VNFM_GEORED_KEY, VM_VNFM_LIMITED_KEY);

        var actualLicense = licenseSelector.selectLicense(VMVNFM, licenses);
        assertEquals(VM_VNFM_FULL_KEY, actualLicense);
    }

    @Test
    void shouldReturnVMLimitedLicenseWhenHigherPriorityNotAvailable() {
        licenseSelector = new LicenseSelector(false);
        var licenses = Set.of(CVNFM_LIMITED_KEY, CVNFM_FULL_KEY, CVNFM_GEORED_KEY, VM_VNFM_LIMITED_KEY);

        var actualLicense = licenseSelector.selectLicense(VMVNFM, licenses);
        assertEquals(VM_VNFM_LIMITED_KEY, actualLicense);
    }

    @Test
    void shouldThrowExceptionWhenNoLicenseFoundAndGeoRedTrue() {
        licenseSelector = new LicenseSelector(true);
        var licenses = Set.of(CVNFM_FULL_KEY, CVNFM_LIMITED_KEY, VM_VNFM_FULL_KEY);

        assertThrows(LicenseNotFoundException.class, () -> licenseSelector.selectLicense(CVNFM, licenses));
    }
}
