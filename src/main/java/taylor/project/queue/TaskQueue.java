package taylor.project.queue;


import taylor.project.model.Task;
import taylor.project.model.TaskStatus;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class TaskQueue {
    private static final TaskQueue INSTANCE = new TaskQueue();

    private final BlockingQueue<Task> queue;
    private final ConcurrentHashMap<UUID, TaskStatus> taskStatusMap;

    private TaskQueue() {
        this.queue = new PriorityBlockingQueue<>();
        this.taskStatusMap = new ConcurrentHashMap<>();
    }

    public static TaskQueue getInstance() {
        return INSTANCE;
    }

    public void submitTask(Task task) throws InterruptedException {
        queue.put(task);
        taskStatusMap.put(task.getId(), TaskStatus.SUBMITTED);
        System.out.printf("SUBMITTED: %s\n", task);
    }

    public Task takeTask() throws InterruptedException {
        Task task = queue.take();
        taskStatusMap.put(task.getId(), TaskStatus.PROCESSING);
        return task;
    }

    public void updateTaskStatus(UUID taskId, TaskStatus status) {
        taskStatusMap.put(taskId, status);
    }

    public int getQueueSize() {
        return queue.size();
    }

}