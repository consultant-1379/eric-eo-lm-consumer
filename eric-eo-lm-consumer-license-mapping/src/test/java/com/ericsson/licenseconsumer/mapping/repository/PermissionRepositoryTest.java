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
package com.ericsson.licenseconsumer.mapping.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.mapping.model.Permission;

public class PermissionRepositoryTest {
    private final PermissionRepository permissionRepository = new PermissionRepositoryImpl();

    @Test
    public void shouldGetPermissionsWithLicencesForCVNFM() {
        Map<Permission, Set<LicenseToLKFMapping>> permissionsWithLicenses =
                permissionRepository.getAllPermissionsWithLicensesByApplication(ApplicationType.CVNFM);

        assertNotNull(permissionsWithLicenses);
        assertFalse(permissionsWithLicenses.isEmpty());

        Optional<Set<LicenseToLKFMapping>> emptyLicensesSet = permissionsWithLicenses.values().stream()
                .filter(Set::isEmpty)
                .findAny();

        assertFalse(emptyLicensesSet.isPresent());
    }

    @Test
    public void shouldGetPermissionsWithLicencesForVMVNFM() {
        Map<Permission, Set<LicenseToLKFMapping>> permissionsWithLicenses =
                permissionRepository.getAllPermissionsWithLicensesByApplication(ApplicationType.VMVNFM);

        assertNotNull(permissionsWithLicenses);
        assertFalse(permissionsWithLicenses.isEmpty());

        Optional<Set<LicenseToLKFMapping>> emptyLicensesSet = permissionsWithLicenses.values().stream()
                .filter(Set::isEmpty)
                .findAny();

        assertFalse(emptyLicensesSet.isPresent());

    }
}
