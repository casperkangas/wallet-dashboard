package sync;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SynchronizationScheduler {
    private static final Logger LOGGER = Logger.getLogger(SynchronizationScheduler.class.getName());

    private final SynchronizationService synchronizationService;
    private final ScheduledExecutorService scheduler;

    public SynchronizationScheduler(SynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "SyncScheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void startAutomaticSync(long initialDelay, long period, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(this::performSync, initialDelay, period, unit);
        LOGGER.info("Automatic synchronization scheduled. Initial delay: " + initialDelay + ", period: " + period + " " + unit);
    }

    public void stopAutomaticSync() {
        scheduler.shutdown();
        LOGGER.info("Automatic synchronization stopped.");
    }

    public void triggerManualSync() {
        // Run sync asynchronously
        scheduler.submit(this::performSync);
    }

    private void performSync() {
        try {
            LOGGER.info("Starting synchronization process...");
            synchronizationService.syncAll();
            LOGGER.info("Synchronization process completed successfully.");
        } catch (SynchronizationException e) {
            LOGGER.log(Level.SEVERE, "Failed to perform synchronization", e);
        }
    }
}
