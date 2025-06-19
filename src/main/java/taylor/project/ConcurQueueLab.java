package taylor.project;


import taylor.project.consumer.TaskConsumer;
import taylor.project.monitor.SystemMonitor;
import taylor.project.producer.TaskProducer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurQueueLab {

    public static AtomicInteger atomicTaskProcessedCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting ConcurQueue System...");

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
            System.out.println("Shutdown hook initiated...");
            shutdown(consumerPool);
            monitorThread.interrupt();
            System.out.println("ConcurQueue System shut down.");
        }));

        for (Thread producerThread : producerThreads) {
            producerThread.join();
        }

        System.out.println("All producers have finished submitting tasks.");

        while (atomicTaskProcessedCount.get() < TOTAL_EXPECTED_TASKS) {
            System.out.printf("Main Thread: Waiting for tasks to complete. Processed: %d/%d\n",
                    atomicTaskProcessedCount.get(), TOTAL_EXPECTED_TASKS);
            TimeUnit.SECONDS.sleep(2);
        }

        System.out.println("All tasks have been processed. Shutting down the system.");
        shutdown(consumerPool);
        monitorThread.interrupt();

        System.out.printf("Final Processed Count: %d\n", atomicTaskProcessedCount.get());
        System.out.println("System execution finished.");
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
