
# # ConcurrentTaskDispatchSystem – A Multithreaded Job Processing Platform

## Features

-   **Producer-Consumer Pattern**: Utilizes multiple producer threads to simulate job submissions and a pool of consumer threads to process them.
-   **Priority-Based Job Processing**: Employs a `PriorityBlockingQueue` to ensure tasks with higher priority are processed first.
-   **Concurrent Worker Pool**: Manages consumer threads efficiently using a fixed-size `ExecutorService`.
-   **Real-time Task Status Tracking**: Tracks the lifecycle of each task (`SUBMITTED`, `PROCESSING`, `COMPLETED`, `FAILED`) using a `ConcurrentHashMap`.
-   **System Health Monitoring**: A dedicated monitor thread periodically logs queue size and thread pool status.
-   **Race Condition Demonstration & Fix**: Deliberately includes an unsafe shared counter to demonstrate a race condition and provides a fix using `AtomicInteger` to ensure thread safety.
-   **Fault Tolerance**: Implements a simple retry mechanism for failed tasks, re-queuing them up to a certain limit.
-   **Graceful Shutdown**: Includes a JVM shutdown hook to ensure the application terminates cleanly, draining the queue and allowing active tasks to complete.

## Design and Implementation Details

### 1. Task Model (`model/`)

-   **`Task.java`**: Represents a single job. It contains an `id`, `name`, `priority`, `createdTimestamp`, and `payload`. Crucially, it implements the `Comparable<Task>` interface. The `compareTo` method is implemented to give precedence to tasks with a higher integer `priority` value, making it suitable for a priority queue.
-   **`TaskStatus.java`**: An `enum` that defines the possible states of a task, ensuring type-safe status tracking.

### 2. Concurrent Queue (`queue/`)

-   **`TaskQueue.java`**: A singleton class that encapsulates the shared resources:
    -   **`PriorityBlockingQueue<Task>`**: This is the heart of the producer-consumer implementation. It's a thread-safe queue that blocks producers if the queue is full (for bounded queues) and blocks consumers if the queue is empty. It also automatically orders elements according to the `Task`'s natural ordering (based on priority).
    -   **`ConcurrentHashMap<UUID, TaskStatus>`**: This map provides a thread-safe way to track the status of each task without needing explicit `synchronized` blocks for simple updates.

### 3. Producers and Consumers

-   **`TaskProducer.java`**: A `Runnable` that simulates a client. Each producer thread creates a set number of tasks with random priorities and submits them to the `TaskQueue`. A small delay is added between submissions to simulate a real-world workload.
-   **`TaskConsumer.java`**: A `Runnable` representing a worker. It runs in an infinite loop, continuously pulling tasks from the `TaskQueue` using the blocking `take()` method. It simulates work with `Thread.sleep()` and handles task completion or failure.

### 4. Concurrency Management

-   **`ExecutorService`**: The `ConcurQueueLab` creates a `Executors.newFixedThreadPool()`. This service manages a pool of worker threads, reducing the overhead of thread creation and destruction. It's responsible for executing the `TaskConsumer` runnables.

-   **Synchronization and Race Conditions**:
    -   **The Problem**: To illustrate a common concurrency bug, the `TaskConsumer` can be configured to update a simple `volatile int taskProcessedCount`. The `++` operation is not atomic (it's a three-step read-modify-write process). When multiple threads execute it concurrently, updates can be lost, leading to an incorrect final count. `volatile` only ensures that threads see the most recent value, but it doesn't prevent them from interfering with each other during the read-modify-write cycle.
    -   **The Solution**: The corrected implementation uses an **`AtomicInteger`**. Its `incrementAndGet()` method is an atomic operation, guaranteeing that the increment happens as a single, indivisible step. This ensures the final count is always accurate, regardless of how many threads are updating it.

### 5. Monitoring and Shutdown

-   **`SystemMonitor.java`**: This `Runnable` provides a continuous view into the system's health. By periodically logging the queue size and `ThreadPoolExecutor` stats (like active threads and pool size), it helps in diagnosing bottlenecks or stalls.

-   **Graceful Shutdown**: The `Runtime.getRuntime().addShutdownHook(...)` in `ConcurQueueLab` registers a thread to be run when the JVM is about to exit. This hook initiates a graceful shutdown of the `ExecutorService`, first by calling `shutdown()` (which stops accepting new tasks and waits for old ones to finish) and then, if necessary, `shutdownNow()` (which attempts to interrupt running tasks). This prevents tasks from being abruptly terminated and ensures system consistency.

## How to Compile and Run

You can compile and run this project from any standard Java IDE or via the command line.

### Prerequisites

-   Java Development Kit (JDK) 8 or higher.
-   Apache Maven (for dependency management and running tests).

### Command Line Instructions

1.  **Navigate to the project's root directory** (where the `pom.xml` file is located).

2.  **Compile the source code and run tests:**
    ```bash
    mvn clean install
    ```

3.  **Run the application:**
    Use the `exec-maven-plugin` (you would need to add it to your `pom.xml`) or run the main class directly after compilation.

    To run directly after `mvn install`:
    ```bash
    java -cp target/classes taylor.project.main.ConcurQueueLab
    ```

### Demonstrating the Race Condition

To see the race condition in action:

1.  **Modify `TaskConsumer.java`**:
    -   Comment out this line: `ConcurQueueLab.atomicTaskProcessedCount.incrementAndGet();`
    -   Uncomment this line: `ConcurQueueLab.taskProcessedCount++;`

2.  **Modify `ConcurQueueLab.java`**:
    -   Comment out the `AtomicInteger` declaration.
    -   Uncommentt the `public static volatile int taskProcessedCount = 0;` declaration.
    -   Change the wait loop and final print statement to use `taskProcessedCount`.

3.  **Re-compile and Run**: Follow the steps above. You will notice that the "Final Processed Count" is often less than the total expected number of tasks, demonstrating lost updates.

**Remember to revert the changes to use `AtomicInteger` to see the corrected, thread-safe behavior.**

## Expected Output

When you run the application, you will see a real-time log of the system's activity:

-   Producers announcing they have started and submitted tasks.
-   Consumers logging when they start and complete processing a task, including which thread processed it.
-   Tasks being marked as `FAILED` and re-queued for a retry.
-   The `SYSTEM MONITOR` printing status reports every 5 seconds.
-   The main thread waiting for all tasks to be completed.
-   A final confirmation of the total tasks processed and the system shutting down.

The interleaved nature of the output clearly demonstrates the concurrent execution of producers and consumers.

