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
package com.ericsson.licenseconsumer.datalayer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.ericsson.licenseconsumer.datalayer.exception.ApplicationNotFoundException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

class LicenseTest {
    @Test
    void shouldGetLicenseByName() {
        assertEquals(LicenseToLKFMapping.fromString("FAT1024423").getLicenseKeyId(),
                     LicenseToLKFMapping.CVNFM_FULL_KEY.getLicenseKeyId());
        assertEquals(LicenseToLKFMapping.fromString("FAT1024423/3").getLicenseKeyId(),
                     LicenseToLKFMapping.CVNFM_LIMITED_KEY.getLicenseKeyId());
        assertEquals(LicenseToLKFMapping.fromString("FAT1024423/5").getLicenseKeyId(),
                     LicenseToLKFMapping.CVNFM_GEORED_KEY.getLicenseKeyId());
    }

    @Test
    void shouldThrowApplicationNotFoundException() {
        assertThrows(ApplicationNotFoundException.class, () -> ApplicationType.fromString("Non-existent name"));

        assertThrows(ApplicationNotFoundException.class, () -> ApplicationType.fromString(null));
    }

    @Test
    void testLicenseToLKFMappingContainsOnlyUniquePrioritiesCVNFM() {
        var allPriorities = Arrays.stream(LicenseToLKFMapping.values())
                .filter(l -> ApplicationType.CVNFM.equals(l.getApplicationType()))
                .map(LicenseToLKFMapping::getPriority)
                .collect(Collectors.toList());
        var uniquePriorities = new HashSet<>(allPriorities);
        assertEquals(allPriorities.size(), uniquePriorities.size());
    }

    @Test
    void testLicenseToLKFMappingContainsOnlyUniquePrioritiesVMVNFM() {
        var allPriorities = Arrays.stream(LicenseToLKFMapping.values())
                .filter(l -> ApplicationType.VMVNFM.equals(l.getApplicationType()))
                .map(LicenseToLKFMapping::getPriority)
                .collect(Collectors.toList());
        var uniquePriorities = new HashSet<>(allPriorities);
        assertEquals(allPriorities.size(), uniquePriorities.size());
    }

}
