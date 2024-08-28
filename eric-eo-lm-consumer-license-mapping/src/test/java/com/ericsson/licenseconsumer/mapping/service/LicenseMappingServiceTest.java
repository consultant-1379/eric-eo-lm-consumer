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

import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.mapping.model.Permission;
import com.ericsson.licenseconsumer.mapping.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ericsson.licenseconsumer.datalayer.model.ApplicationType.CVNFM;
import static com.ericsson.licenseconsumer.datalayer.model.ApplicationType.VMVNFM;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_FULL_KEY;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_GEORED_KEY;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_LIMITED_KEY;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.VM_VNFM_FULL_KEY;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.VM_VNFM_GEORED_KEY;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.VM_VNFM_LIMITED_KEY;
import static com.ericsson.licenseconsumer.mapping.model.Permission.CLUSTER_MANAGEMENT;
import static com.ericsson.licenseconsumer.mapping.model.Permission.ENM_INTEGRATION;
import static com.ericsson.licenseconsumer.mapping.model.Permission.LCM_OPERATIONS;
import static com.ericsson.licenseconsumer.mapping.model.Permission.ONBOARDING;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class LicenseMappingServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private LicenseMappingServiceImpl licenseMappingService;

    @Test
    void shouldGetPermissionsByAppNameForCVNFMFullKey() {
        Map<Permission, Set<LicenseToLKFMapping>> map =
                Map.of(CLUSTER_MANAGEMENT, Set.of(CVNFM_LIMITED_KEY, CVNFM_FULL_KEY),
                       ONBOARDING, Set.of(CVNFM_LIMITED_KEY),
                       ENM_INTEGRATION, Set.of(CVNFM_FULL_KEY));

        given(permissionRepository.getAllPermissionsWithLicensesByApplication(CVNFM)).willReturn(map);

        List<String> permissions = licenseMappingService.mapLicenseToPermissions(CVNFM.getName(), CVNFM_FULL_KEY.getLicenseKeyId());
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        assertTrue(permissions.containsAll(List.of(CLUSTER_MANAGEMENT.getName(), ENM_INTEGRATION.getName())));
        assertFalse(permissions.contains(ONBOARDING.getName()));
    }

    @Test
    void shouldGetPermissionsByAppNameForCVNFMLimitedKey() {
        Map<Permission, Set<LicenseToLKFMapping>> map =
                Map.of(CLUSTER_MANAGEMENT, Set.of(CVNFM_LIMITED_KEY, CVNFM_FULL_KEY),
                       ONBOARDING, Set.of(CVNFM_LIMITED_KEY),
                       ENM_INTEGRATION, Set.of(CVNFM_FULL_KEY));

        given(permissionRepository.getAllPermissionsWithLicensesByApplication(CVNFM)).willReturn(map);

        List<String> permissions = licenseMappingService.mapLicenseToPermissions(CVNFM.getName(), CVNFM_LIMITED_KEY.getLicenseKeyId());
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        assertTrue(permissions.containsAll(List.of(CLUSTER_MANAGEMENT.getName(), ONBOARDING.getName())));
        assertFalse(permissions.contains(ENM_INTEGRATION.getName()));
    }

    @Test
    void shouldGetPermissionsByAppNameForCVNFMGeoredKey() {
        Map<Permission, Set<LicenseToLKFMapping>> map =
                Map.of(CLUSTER_MANAGEMENT, Set.of(CVNFM_GEORED_KEY, CVNFM_FULL_KEY),
                       ONBOARDING, Set.of(CVNFM_LIMITED_KEY),
                       ENM_INTEGRATION, Set.of(CVNFM_GEORED_KEY));

        given(permissionRepository.getAllPermissionsWithLicensesByApplication(CVNFM)).willReturn(map);

        List<String> permissions = licenseMappingService.mapLicenseToPermissions(CVNFM.getName(), CVNFM_GEORED_KEY.getLicenseKeyId());
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        assertTrue(permissions.containsAll(List.of(CLUSTER_MANAGEMENT.getName(), ENM_INTEGRATION.getName())));
        assertFalse(permissions.contains(ONBOARDING.getName()));
    }

    @Test
    void shouldGetPermissionsByAppNameForVMVNFMFullKey() {
        Map<Permission, Set<LicenseToLKFMapping>> map =
                Map.of(LCM_OPERATIONS, Set.of(VM_VNFM_LIMITED_KEY, VM_VNFM_GEORED_KEY),
                       ENM_INTEGRATION, Set.of(VM_VNFM_FULL_KEY, VM_VNFM_GEORED_KEY));

        given(permissionRepository.getAllPermissionsWithLicensesByApplication(VMVNFM)).willReturn(map);

        List<String> permissions = licenseMappingService.mapLicenseToPermissions(VMVNFM.getName(), VM_VNFM_FULL_KEY.getLicenseKeyId());
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        assertTrue(permissions.contains(ENM_INTEGRATION.getName()));
        assertFalse(permissions.contains(LCM_OPERATIONS.getName()));
    }

    @Test
    void shouldGetPermissionsByAppNameForVMVNFMLimitedKey() {
        Map<Permission, Set<LicenseToLKFMapping>> map =
                Map.of(LCM_OPERATIONS, Set.of(VM_VNFM_LIMITED_KEY, VM_VNFM_GEORED_KEY),
                        ENM_INTEGRATION, Set.of(VM_VNFM_FULL_KEY, VM_VNFM_GEORED_KEY));

        given(permissionRepository.getAllPermissionsWithLicensesByApplication(VMVNFM)).willReturn(map);

        List<String> permissions = licenseMappingService.mapLicenseToPermissions(VMVNFM.getName(), VM_VNFM_LIMITED_KEY.getLicenseKeyId());
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        assertFalse(permissions.contains(ENM_INTEGRATION.getName()));
        assertTrue(permissions.contains(LCM_OPERATIONS.getName()));
    }

    @Test
    void shouldGetPermissionsByAppNameForVMVNFMGeoredKey() {
        Map<Permission, Set<LicenseToLKFMapping>> map =
                Map.of(LCM_OPERATIONS, Set.of(VM_VNFM_LIMITED_KEY),
                        ENM_INTEGRATION, Set.of(VM_VNFM_FULL_KEY, VM_VNFM_GEORED_KEY));

        given(permissionRepository.getAllPermissionsWithLicensesByApplication(VMVNFM)).willReturn(map);

        List<String> permissions = licenseMappingService.mapLicenseToPermissions(VMVNFM.getName(), VM_VNFM_GEORED_KEY.getLicenseKeyId());
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        assertTrue(permissions.contains(ENM_INTEGRATION.getName()));
        assertFalse(permissions.contains(LCM_OPERATIONS.getName()));
    }
}