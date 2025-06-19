package taylor.project;


import lombok.extern.slf4j.Slf4j;
import taylor.project.consumer.TaskConsumer;
import taylor.project.monitor.SystemMonitor;
import taylor.project.producer.TaskProducer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConcurQueueLab {

    public static AtomicInteger atomicTaskProcessedCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting ConcurQueue System...");

        final int NUM_PRODUCERS = 3;
        final int NUM_CONSUMERS = 5;
        final int TASKS_PER_PRODUCER = 10;
        final int TOTAL_EXPECTED_TASKS = NUM_PRODUCERS * TASKS_PER_PRODUCER;

        ExecutorService consumerPool = Executors.newFixedThreadPool(NUM_CONSUMERS);

        Thread[] producerThreads = new Thread[NUM_PRODUCERS];
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            producerThreads[i] = new Thread(new TaskProducer("Producer-" + (i + 1), TASKS_PER_PRODUCER));
            producerThreads[i].start();
        }


        for (int i = 0; i < NUM_CONSUMERS; i++) {
            consumerPool.submit(new TaskConsumer());
        }

        Thread monitorThread = new Thread(new SystemMonitor((ThreadPoolExecutor) consumerPool));
        monitorThread.start();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook initiated...");
            shutdown(consumerPool);
            monitorThread.interrupt();
            log.info("ConcurQueue System shut down.");
        }));

        for (Thread producerThread : producerThreads) {
            producerThread.join();
        }

        log.info("All producers have finished submitting tasks.");

        while (atomicTaskProcessedCount.get() < TOTAL_EXPECTED_TASKS) {
            log.info("Main Thread: Waiting for tasks to complete. Processed: {}/{}\n",
                    atomicTaskProcessedCount.get(), TOTAL_EXPECTED_TASKS);
            TimeUnit.SECONDS.sleep(2);
        }

        log.info("All tasks have been processed. Shutting down the system.");
        shutdown(consumerPool);
        monitorThread.interrupt();

        log.info("Final Processed Count: {}\n", atomicTaskProcessedCount.get());
        log.info("System execution finished.");
    }

    private static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {

            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();

                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();

            Thread.currentThread().interrupt();
        }
    }
}
