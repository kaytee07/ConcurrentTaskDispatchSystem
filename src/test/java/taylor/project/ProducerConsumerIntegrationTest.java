package taylor.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taylor.project.consumer.TaskConsumer;
import taylor.project.model.TaskStatus;
import taylor.project.producer.TaskProducer;
import taylor.project.queue.TaskQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ProducerConsumerIntegrationTest {

    private TaskQueue taskQueue;

    @BeforeEach
    void setUp() {
        // Reset shared state before each integration test
        taskQueue = TaskQueue.getInstance();
        taskQueue.resetForTest();
        ConcurQueueLab.atomicTaskProcessedCount = new AtomicInteger(0);
    }

    @Test
    void fullWorkflow_shouldProcessAllTasksSubmittedByProducers() throws InterruptedException {
        // Arrange
        final int numProducers = 2;
        final int tasksPerProducer = 5;
        final int totalTasks = numProducers * tasksPerProducer;
        final int numConsumers = 3;

        ExecutorService consumerPool = Executors.newFixedThreadPool(numConsumers);
        for (int i = 0; i < numConsumers; i++) {
            consumerPool.submit(new TaskConsumer());
        }

        Thread[] producerThreads = new Thread[numProducers];
        for (int i = 0; i < numProducers; i++) {
            producerThreads[i] = new Thread(new TaskProducer("TestProducer-" + i, tasksPerProducer));
        }

        // Act
        for (Thread producerThread : producerThreads) {
            producerThread.start();
        }

        // Wait for producers to finish submitting
        for (Thread producerThread : producerThreads) {
            producerThread.join();
        }

        // Assert
        // Use Awaitility to wait until all tasks are processed, avoiding flaky sleeps
        await().atMost(15, SECONDS).until(() -> ConcurQueueLab.atomicTaskProcessedCount.get() == totalTasks);

        // Shutdown the consumer pool gracefully
        consumerPool.shutdown();
        consumerPool.awaitTermination(5, SECONDS);

        // Final assertions
        assertThat(ConcurQueueLab.atomicTaskProcessedCount.get()).isEqualTo(totalTasks);
        assertThat(taskQueue.getQueueSize()).isZero();

        // Verify all tasks are marked as completed (assuming no failures in this test)
        long completedTasks = taskQueue.getTaskStatusMap().values().stream()
                .filter(status -> status == TaskStatus.COMPLETED)
                .count();
        assertThat(completedTasks).isEqualTo(totalTasks);
    }
}