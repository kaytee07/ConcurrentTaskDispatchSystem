package taylor.project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taylor.project.model.Task;
import taylor.project.model.TaskStatus;
import taylor.project.queue.TaskQueue;

import static org.assertj.core.api.Assertions.assertThat;

class TaskQueueTest {

    private TaskQueue taskQueue;

    @BeforeEach
    void setUp() {
        taskQueue = TaskQueue.getInstance();

        taskQueue.resetForTest();
    }

    @Test
    void submitTask_shouldAddTaskToQueueAndSetStatusToSubmitted() throws InterruptedException {
        Task task = new Task("Test Task","payload", 5);

        taskQueue.submitTask(task);

        assertThat(taskQueue.getQueueSize()).isEqualTo(1);
        assertThat(taskQueue.getTaskStatusMap().get(task.getId())).isEqualTo(TaskStatus.SUBMITTED);
    }

    @Test
    void takeTask_shouldRemoveTaskFromQueueAndSetStatusToProcessing() throws InterruptedException {
        Task task = new Task("Test Task", "payload", 5);
        taskQueue.submitTask(task);

        Task takenTask = taskQueue.takeTask();

        assertThat(takenTask).isEqualTo(task);
        assertThat(taskQueue.getQueueSize()).isZero();
        assertThat(taskQueue.getTaskStatusMap().get(task.getId())).isEqualTo(TaskStatus.PROCESSING);
    }

    @Test
    void takeTask_shouldReturnHighestPriorityTaskFirst() throws InterruptedException {
        Task lowPriorityTask = new Task("Low Prio", "p1", 1);
        Task highPriorityTask = new Task("High Prio", "p2", 10);
        Task mediumPriorityTask = new Task("Medium Prio", "p3", 5);

        taskQueue.submitTask(lowPriorityTask);
        taskQueue.submitTask(highPriorityTask);
        taskQueue.submitTask(mediumPriorityTask);

        assertThat(taskQueue.takeTask().getPriority()).isEqualTo(10);
        assertThat(taskQueue.takeTask().getPriority()).isEqualTo(5);
        assertThat(taskQueue.takeTask().getPriority()).isEqualTo(1);
    }

    @Test
    void updateTaskStatus_shouldChangeTaskStatusInMap() {
        Task task = new Task("Test Task", "payload", 5);
        taskQueue.getTaskStatusMap().put(task.getId(), TaskStatus.SUBMITTED);

        taskQueue.updateTaskStatus(task.getId(), TaskStatus.COMPLETED);

        assertThat(taskQueue.getTaskStatusMap().get(task.getId())).isEqualTo(TaskStatus.COMPLETED);
    }
}