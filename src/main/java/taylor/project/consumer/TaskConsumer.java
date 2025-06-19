package taylor.project.consumer;


import lombok.extern.slf4j.Slf4j;
import taylor.project.ConcurQueueLab;
import taylor.project.model.Task;
import taylor.project.model.TaskStatus;
import taylor.project.queue.TaskQueue;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TaskConsumer implements Runnable {
    private final TaskQueue taskQueue;

    public TaskConsumer() {
        this.taskQueue = TaskQueue.getInstance();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Task task = taskQueue.takeTask();
                processTask(task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Consumer thread {} was interrupted.\n", Thread.currentThread().getName());
        }
    }

    private void processTask(Task task) {
        String threadName = Thread.currentThread().getName();
        log.info("PROCESSING: {} by Thread [{}] at {}\n", task, threadName, Instant.now());

        try {

            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000));


            if (ThreadLocalRandom.current().nextDouble() < 0.1 && task.getRetryCount() < 3) {
                throw new RuntimeException("Simulated processing failure.");
            }

            taskQueue.updateTaskStatus(task.getId(), TaskStatus.COMPLETED);
            log.info("COMPLETED: {} by Thread [{}]\n", task, threadName);

            // Unsafe shared counter demonstration (before fix)
            // ConcurQueueLab.taskProcessedCount++;

            // FIX: Using AtomicInteger
            ConcurQueueLab.atomicTaskProcessedCount.incrementAndGet();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            taskQueue.updateTaskStatus(task.getId(), TaskStatus.FAILED);
            log.warn("FAILED (Interrupted): {} by Thread [{}]\n", task, threadName);
        } catch (Exception e) {
            task.incrementRetryCount();
            log.info("FAILED (Retrying): {}, Retry #{}\n", task, task.getRetryCount());
            try {
                taskQueue.submitTask(task); // Re-queue the task
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}