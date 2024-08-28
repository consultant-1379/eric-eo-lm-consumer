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
package com.ericsson.licenseconsumer.datalayer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ericsson.licenseconsumer.datalayer.entity.ApplicationLicenseKeyId;
import com.ericsson.licenseconsumer.datalayer.entity.LicenseKey;
import com.ericsson.licenseconsumer.datalayer.repository.LicenseKeyRepository;
import com.ericsson.licenseconsumer.datalayer.model.ApplicationType;
import com.ericsson.licenseconsumer.datalayer.model.LicenseToLKFMapping;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LicenseDbServiceImpl implements LicenseDbService {

    private final LicenseKeyRepository licenseKeyRepository;

    public LicenseDbServiceImpl(final LicenseKeyRepository licenseKeyRepository) {
        this.licenseKeyRepository = licenseKeyRepository;
    }

    @Override
    @Transactional
    public LicenseKey saveLicense(final ApplicationType application, final LicenseToLKFMapping license) {
        return licenseKeyRepository.save(new LicenseKey(license.getLicenseKeyId(), application.getName()));
    }

    @Override
    public void deleteLicenseKey(final LicenseKey licenseKey) {
        licenseKeyRepository.deleteById(new ApplicationLicenseKeyId(licenseKey.getLicenseKeyId(), licenseKey.getApplication()));
    }

    @Override
    public void deleteAll() {
        licenseKeyRepository.deleteAll();
    }

    @Override
    public LicenseToLKFMapping findByApplicationAndMap(final String application) {
        var licenseKey = licenseKeyRepository.findByApplication(
                ApplicationType.fromString(application).getName());
        return licenseKey != null ? LicenseToLKFMapping.fromString(licenseKey.getLicenseKeyId()) : null;
    }

    @Override
    public LicenseKey findByApplication(final String application) {
        return licenseKeyRepository.findByApplication(ApplicationType.fromString(application).getName());
    }

}
