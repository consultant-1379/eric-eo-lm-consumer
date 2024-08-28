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
package com.ericsson.licenseconsumer.mapping.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.licenseconsumer.mapping.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.mapping.model.Permission;

@Service
public class LicenseMappingServiceImpl implements LicenseMappingService {

    private final PermissionRepository permissionRepository;

    @Autowired
    public LicenseMappingServiceImpl(final PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<String> mapLicenseToPermissions(final String app, final String licenseKey)  {
        List<Permission> permissions = mapPermissions(ApplicationType.fromString(app),
                                                      LicenseToLKFMapping.fromString(licenseKey));
        return permissions.stream().map(Permission::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllPermissionsByApplication(final String app) {
        var permissionsWithLicenses = permissionRepository.getAllPermissionsWithLicensesByApplication(
                ApplicationType.fromString(app));
        return permissionsWithLicenses.keySet()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }

    private List<Permission> mapPermissions(final ApplicationType app, final LicenseToLKFMapping activeLicense) {
        var permissionsWithLicenses = permissionRepository.getAllPermissionsWithLicensesByApplication(app);
        return permissionsWithLicenses.keySet()
                .stream()
                .filter(permission -> checkIfPermissionsContainLicense(activeLicense, permissionsWithLicenses, permission))
                .collect(Collectors.toList());
    }

    private static boolean checkIfPermissionsContainLicense(final LicenseToLKFMapping activeLicense,
                                                            final Map<Permission, Set<LicenseToLKFMapping>> permissionsWithLicenses,
                                                            final Permission permission) {
        return permissionsWithLicenses.get(permission)
                .stream()
                .anyMatch(license -> license == activeLicense);
    }
}
