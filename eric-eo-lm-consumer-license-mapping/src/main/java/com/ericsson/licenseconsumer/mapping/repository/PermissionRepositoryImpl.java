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

import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;
import com.ericsson.licenseconsumer.mapping.model.Permission;
import org.springframework.stereotype.Repository;

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
import static java.util.Map.entry;
import static java.util.Set.of;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final Map<ApplicationType, Map<Permission, Set<LicenseToLKFMapping>>> licenseApplicationPermissions = Map.ofEntries(
            entry(CVNFM, Map.ofEntries(
                    entry(ENM_INTEGRATION, of(CVNFM_FULL_KEY, CVNFM_GEORED_KEY)),
                    entry(CLUSTER_MANAGEMENT, of(CVNFM_FULL_KEY, CVNFM_GEORED_KEY, CVNFM_LIMITED_KEY)),
                    entry(LCM_OPERATIONS, of(CVNFM_FULL_KEY, CVNFM_GEORED_KEY, CVNFM_LIMITED_KEY)),
                    entry(ONBOARDING, of(CVNFM_FULL_KEY, CVNFM_GEORED_KEY, CVNFM_LIMITED_KEY))
            )),
            entry(VMVNFM, Map.ofEntries(
                  entry(LCM_OPERATIONS, of(VM_VNFM_FULL_KEY, VM_VNFM_LIMITED_KEY, VM_VNFM_GEORED_KEY)),
                  entry(ENM_INTEGRATION, of(VM_VNFM_FULL_KEY, VM_VNFM_GEORED_KEY))))
    );

    @Override
    public Map<Permission, Set<LicenseToLKFMapping>> getAllPermissionsWithLicensesByApplication(final ApplicationType app) {
        return licenseApplicationPermissions.get(app);
    }
}
