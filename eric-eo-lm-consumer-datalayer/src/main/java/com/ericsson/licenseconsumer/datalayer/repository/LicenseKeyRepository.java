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
package com.ericsson.licenseconsumer.datalayer.repository;

import com.ericsson.licenseconsumer.datalayer.entity.ApplicationLicenseKeyId;
import com.ericsson.licenseconsumer.datalayer.entity.LicenseKey;
import lombok.Generated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Generated
@Repository
public interface LicenseKeyRepository extends JpaRepository<LicenseKey, ApplicationLicenseKeyId> {
    LicenseKey findByApplication(String applicationType);
}
