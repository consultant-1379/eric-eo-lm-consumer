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
package com.ericsson.licenseconsumer.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.scheduled", name = "enabled", havingValue = "true")
public class CronJob {
    private final PollLicensesJobService pollLicensesJobService;

    private final int initialDelay;

    private final ScheduledExecutorService executorService;

    public CronJob(final PollLicensesJobService pollLicensesJobService,
                   @Value("${spring.scheduled.initialDelay:5}") final int initialDelay) {
        this.pollLicensesJobService = pollLicensesJobService;
        this.initialDelay = initialDelay;
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void proceedInitialStartupJob() {
        executorService.schedule(pollLicensesJobService::processAllLicenses, initialDelay, TimeUnit.SECONDS);
    }

    @Scheduled(cron = "${spring.scheduled.cron}")
    public void proceed() {
        pollLicensesJobService.processAllLicenses();
    }
}
