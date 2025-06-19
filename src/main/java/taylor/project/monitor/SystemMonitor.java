package taylor.project.monitor;

import lombok.extern.slf4j.Slf4j;
import taylor.project.queue.TaskQueue;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SystemMonitor implements Runnable {
    private final TaskQueue taskQueue;
    private final ThreadPoolExecutor executor;

    public SystemMonitor(ThreadPoolExecutor executor) {
        this.taskQueue = TaskQueue.getInstance();
        this.executor = executor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                log.info("==================== SYSTEM MONITOR ====================");
                log.info(this.toString());
                log.info("========================================================");
                TimeUnit.SECONDS.sleep(5);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("System monitor was interrupted.");
        }
    }
}