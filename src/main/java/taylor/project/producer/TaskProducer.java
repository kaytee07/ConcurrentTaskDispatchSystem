package taylor.project.producer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import taylor.project.model.Task;
import taylor.project.queue.TaskQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TaskProducer implements Runnable {
    private final TaskQueue taskQueue;
    private final int numberOfTasks;
    private final String producerName;

    public TaskProducer(TaskQueue taskQueue, String producerName, int numberOfTasks) {
        this.taskQueue = TaskQueue.getInstance();
        this.producerName = producerName;
        this.numberOfTasks = numberOfTasks;
    }


    @Override
    public void run() {
        log.info("Producer [%s] started.{}", producerName);
        for (int i = 0; i < numberOfTasks; i++) {
            try {
                int priority = ThreadLocalRandom.current().nextInt(1, 11);
                Task task = new Task("Task-" + (i + 1) + " by " + producerName, "payload data." , priority);
                taskQueue.submitTask(task);
                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Producer [%s] was interrupted.{}", producerName);
                break;
            }
        }
        log.info("Producer [%s] finished producing tasks.{}", producerName);
    }
}
