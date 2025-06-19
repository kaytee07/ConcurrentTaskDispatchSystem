package taylor.project.monitor;

import lombok.extern.slf4j.Slf4j;
import taylor.project.ConcurQueueLab;
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
                System.out.println("==================== SYSTEM MONITOR ====================");
                System.out.printf("Queue Size: %d\n", taskQueue.getQueueSize());
                System.out.printf("Active Threads: %d\n", executor.getActiveCount());
                System.out.printf("Pool Size: %d\n", executor.getPoolSize());
                System.out.printf("Completed Tasks : %d\n", ConcurQueueLab.atomicTaskProcessedCount.get());
                System.out.println("========================================================");
                TimeUnit.SECONDS.sleep(5);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("System monitor was interrupted.");
        }
    }
}