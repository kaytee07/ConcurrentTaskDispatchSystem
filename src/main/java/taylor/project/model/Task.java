package taylor.project.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class Task implements Comparable<Task> {
    private final UUID id;
    private final String name;
    private final int priority;
    private final Instant createdTimestamp;
    private final String payload;
    private int retryCount = 0;

    public Task(String name, String payload, int priority) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.priority = priority;
        this.createdTimestamp = Instant.now();
        this.payload = payload;
    }

    @Override
    public  int compareTo(Task other) {
        return Integer.compare(other.priority, this.priority);
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    @Override
    public String toString() {
        return String.format("Task[id=%s, name=%s, priority=%d]", id, name, priority);
    }

}
