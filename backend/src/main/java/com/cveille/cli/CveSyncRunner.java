package com.cveille.cli;

import com.cveille.service.CveSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CveSyncRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CveSyncRunner.class);

    private final CveSyncService cveSyncService;
    private final ApplicationContext applicationContext;

    public CveSyncRunner(CveSyncService cveSyncService, ApplicationContext applicationContext) {
        this.cveSyncService = cveSyncService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        boolean syncRequested = Arrays.asList(args).contains("--sync")
                || "true".equalsIgnoreCase(System.getenv("CVEILLE_AUTO_SYNC"));

        if (syncRequested) {
            log.info("Batch CLI mode triggered (--sync or CVEILLE_AUTO_SYNC=true)");
            try {
                cveSyncService.runSync();
                log.info("Batch sync completed successfully.");
            } catch (Exception e) {
                log.error("Batch sync failed: {}", e.getMessage(), e);
                System.exit(1);
            }
            System.exit(0);
        } else {
            log.info("CVEille backend running in REST Server mode. Access endpoints at http://localhost:8080/api/cves");
        }
    }
}
