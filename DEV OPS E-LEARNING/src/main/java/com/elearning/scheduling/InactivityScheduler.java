package com.elearning.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.elearning.services.InactivityAlertService;

@Component
public class InactivityScheduler {

    private final InactivityAlertService alertService;
    private final Logger logger = LoggerFactory.getLogger(InactivityScheduler.class);

    public InactivityScheduler(InactivityAlertService alertService) {
        this.alertService = alertService;
    }

    // Runs every night at 02:00 AM server time
    @Scheduled(cron = "0 0 2 * * *")
    public void nightlyInactivityCheck() {
        logger.info("Starting nightly inactivity check...");
        try {
            alertService.runInactivityCheckDefault();
        } catch (Exception e) {
            logger.error("Error during inactivity check", e);
        }
        logger.info("Nightly inactivity check finished.");
    }

    // For development/testing: manual trigger (comment out in prod if not wanted)
    @Scheduled(cron = "0 0/10 * * * *") // every 10 minutes - useful for testing, remove or disable in prod
    public void frequentCheckForDev() {
        logger.debug("Running dev frequent inactivity check...");
        alertService.runInactivityCheckDefault();
    }
}