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
package com.ericsson.licenseconsumer.integration.service;

import com.ericsson.licenseconsumer.EricLicenseConsumerApplication;
import com.ericsson.licenseconsumer.datalayer.entity.LicenseKey;
import com.ericsson.licenseconsumer.datalayer.exception.ApplicationNotFoundException;
import com.ericsson.licenseconsumer.datalayer.repository.LicenseKeyRepository;
import com.ericsson.licenseconsumer.datalayer.service.LicenseDbServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ericsson.licenseconsumer.datalayer.model.ApplicationType.CVNFM;
import static com.ericsson.licenseconsumer.datalayer.model.ApplicationType.VMVNFM;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.CVNFM_LIMITED_KEY;
import static com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping.VM_VNFM_FULL_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EricLicenseConsumerApplication.class)
@Transactional
class LicenseDbServiceTest {
    @Autowired
    private LicenseDbServiceImpl licenseDbService;
    @Autowired
    private LicenseKeyRepository licenseKeyRepository;

    private static final LicenseKey CVNFM_LICENSE_DUMMY_ID = new LicenseKey("1/FAL1153215", CVNFM.getName());
    private static final LicenseKey VMNFM_LICENSE_DUMMY_ID = new LicenseKey("CXC401/ELIS", VMVNFM.getName());
    private static final LicenseKey CVNFM_LIMITED_LICENSE_REAL_ID = new LicenseKey(
            CVNFM_LIMITED_KEY.getLicenseKeyId(), CVNFM.getName());
    private static final LicenseKey VMNFM_FULL_LICENSE_REAL_ID = new LicenseKey(
            VM_VNFM_FULL_KEY.getLicenseKeyId(), VMVNFM.getName());

    @Test
    void shouldReturnNullIfLicenseNotFound() {
        assertNull(licenseDbService.findByApplication(CVNFM.getName()));
    }

    @Test
    void shouldReturnCvfnmLicense() {
        insertLicenseKeys();
        var licenseKey = licenseDbService.findByApplication(CVNFM.getName());
        assertNotNull(licenseKey);
        assertEquals(CVNFM_LICENSE_DUMMY_ID.getLicenseKeyId(), licenseKey.getLicenseKeyId());
        assertEquals(CVNFM_LICENSE_DUMMY_ID.getApplication(), licenseKey.getApplication());
    }

    @Test
    void shouldReturnVmnfmLicense() {
        insertLicenseKeys();
        var licenseKey = licenseDbService.findByApplication(VMVNFM.getName());
        assertNotNull(licenseKey);
        assertEquals(VMNFM_LICENSE_DUMMY_ID.getLicenseKeyId(), licenseKey.getLicenseKeyId());
        assertEquals(VMNFM_LICENSE_DUMMY_ID.getApplication(), licenseKey.getApplication());
    }

    @Test
    void shouldMapVmnfmLicense() {
        licenseKeyRepository.saveAll(List.of(CVNFM_LIMITED_LICENSE_REAL_ID, VMNFM_FULL_LICENSE_REAL_ID));
        var license = licenseDbService.findByApplicationAndMap(VMVNFM.getName());
        assertNotNull(license);
        assertEquals(VM_VNFM_FULL_KEY, license);
    }

    @Test
    void shouldMapCvnfmLicense() {
        licenseKeyRepository.saveAll(List.of(CVNFM_LIMITED_LICENSE_REAL_ID, VMNFM_FULL_LICENSE_REAL_ID));
        var license = licenseDbService.findByApplicationAndMap(CVNFM.getName());
        assertNotNull(license);
        assertEquals(CVNFM_LIMITED_KEY, license);
    }

    @Test
    void shouldDeleteLicense() {
        insertLicenseKeys();
        var licenseKey = licenseDbService.findByApplication(VMVNFM.getName());
        assertNotNull(licenseKey);
        licenseDbService.deleteLicenseKey(licenseKey);

        licenseKey = licenseDbService.findByApplication(VMVNFM.getName());
        assertNull(licenseKey);
    }

    @Test
    void shouldDeleteAllLicenses() {
        insertLicenseKeys();
        var licenseKey = licenseDbService.findByApplication(VMVNFM.getName());
        assertNotNull(licenseKey);
        licenseKey = licenseDbService.findByApplication(CVNFM.getName());
        assertNotNull(licenseKey);

        licenseDbService.deleteAll();

        licenseKey = licenseDbService.findByApplication(VMVNFM.getName());
        assertNull(licenseKey);
        licenseKey = licenseDbService.findByApplication(CVNFM.getName());
        assertNull(licenseKey);
    }

    @Test
    void shouldThrowExceptionForNullApplicationType() {
        assertThrows(ApplicationNotFoundException.class, () -> licenseDbService.findByApplication(null));
    }

    private void insertLicenseKeys() {
        licenseKeyRepository.saveAll(List.of(CVNFM_LICENSE_DUMMY_ID, VMNFM_LICENSE_DUMMY_ID));
        licenseKeyRepository.flush();
    }

    @AfterEach
    public void cleanUpData() {
        licenseKeyRepository.deleteAll();
    }
}