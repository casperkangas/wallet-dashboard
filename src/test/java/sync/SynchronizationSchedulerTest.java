package sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SynchronizationSchedulerTest {

    @Mock
    private SynchronizationService synchronizationService;

    private SynchronizationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SynchronizationScheduler(synchronizationService);
    }

    @Test
    void triggerManualSync_executesSyncAll() throws Exception {
        scheduler.triggerManualSync();

        // Wait up to 1 second for the async task to execute
        verify(synchronizationService, timeout(1000)).syncAll();
    }

    @Test
    void startAutomaticSync_executesSyncAll() throws Exception {
        scheduler.startAutomaticSync(10, 10000, TimeUnit.MILLISECONDS);

        // Wait up to 1 second for the async task to execute
        verify(synchronizationService, timeout(1000)).syncAll();
        
        scheduler.stopAutomaticSync();
    }
}
