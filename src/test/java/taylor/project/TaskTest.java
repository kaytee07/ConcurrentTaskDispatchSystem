package taylor.project;

import org.junit.jupiter.api.Test;
import taylor.project.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    @Test
    void compareTo_shouldPrioritizeHigherPriorityNumber() {
        Task highPriorityTask = new Task("High Prio", "Payload", 8);
        Task lowPriorityTask = new Task("Low Prio", "Payload", 4);

        assertThat(highPriorityTask.compareTo(lowPriorityTask)).isLessThan(0);
        assertThat(lowPriorityTask.compareTo(highPriorityTask)).isGreaterThan(0);
    }

    @Test
    void compareTo_shouldReturnZeroForEqualPriority() {
        int priority = ThreadLocalRandom.current().nextInt(1, 11);
        Task task1 = new Task("Task 1", "Payload", priority);
        Task task2 = new Task("Task 2", "Payload", priority);

        assertThat(task1.compareTo(task2)).isZero();
    }

    @Test
    void collectionsSort_shouldOrderTasksByPriorityDescending() {
        Task highPriorityTask = new Task("High Prio", "Payload", 10);
        Task mediumPriorityTask = new Task("Medium Prio", "Payload", 6);
        Task lowPriorityTask = new Task("Low Prio", "Payload", 3);

        List<Task> tasks = new ArrayList<>(List.of(lowPriorityTask, highPriorityTask, mediumPriorityTask));


        Collections.sort(tasks);


        assertThat(tasks).containsExactly(highPriorityTask, mediumPriorityTask, lowPriorityTask);
    }

    @Test
    void incrementRetryCount_shouldIncreaseRetryCountByOne() {
        Task task = new Task("Test Task","payload", 1);
        assertThat(task.getRetryCount()).isZero();

        task.incrementRetryCount();
        assertThat(task.getRetryCount()).isEqualTo(1);

        task.incrementRetryCount();
        assertThat(task.getRetryCount()).isEqualTo(2);
    }
}
