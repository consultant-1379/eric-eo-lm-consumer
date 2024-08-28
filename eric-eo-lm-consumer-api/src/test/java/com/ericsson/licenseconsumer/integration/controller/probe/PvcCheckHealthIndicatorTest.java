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
package com.ericsson.licenseconsumer.integration.controller.probe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.licenseconsumer.controller.probe.PvcCheckHealthIndicator;

@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = {
    "tls.enabled=true",
    "spring.flyway.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration, " +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration" })
@ContextConfiguration(classes = PvcCheckHealthIndicator.class)
public class PvcCheckHealthIndicatorTest {

    @Value("${healthCheckEnv.mountPaths.readOnly.tlsDependant}")
    private String[] tldDependantReadOnlyMountPaths;

    @Value("${healthCheckEnv.mountPaths.readWrite}")
    private String[] readWriteMountPaths;

    @Autowired
    private PvcCheckHealthIndicator pvcCheckHealthIndicator;

    @Test
    public void shouldPerformCheckOnEachConfiguredPath() {
        Health healthStatus = pvcCheckHealthIndicator.health();
        int mountsCount = tldDependantReadOnlyMountPaths.length + readWriteMountPaths.length;

        assertEquals(mountsCount, healthStatus.getDetails().size());
        assertTrue(healthStatus.getDetails().keySet().containsAll(List.of(tldDependantReadOnlyMountPaths)));
        assertTrue(healthStatus.getDetails().keySet().containsAll(List.of(readWriteMountPaths)));
    }

    @Nested
    @TestPropertySource(properties = { "tls.enabled=false" })
    public class ChangedPropertiesSubtest {

        @Value("${healthCheckEnv.mountPaths.readWrite}")
        private String[] readWriteMountPaths;

        @Autowired
        private PvcCheckHealthIndicator pvcCheckHealthIndicator;

        @Test
        public void shouldSkipCheckOnSkipValidationFlagIsTrue() {
            Health healthStatus = pvcCheckHealthIndicator.health();
            int mountsCount = readWriteMountPaths.length;

            assertEquals(mountsCount, healthStatus.getDetails().size());
            assertTrue(healthStatus.getDetails().keySet().containsAll(List.of(readWriteMountPaths)));
        }
    }
}