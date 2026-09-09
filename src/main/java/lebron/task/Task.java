package lebron.task;

/**
 * A task the user is tracking: a description plus a done/not-done status.
 *
 * <p>Concrete kinds of task ({@link Todo}, {@link Deadline}, {@link Event})
 * extend this class and decide how they are displayed and how they are
 * encoded for the data file.
 */
public abstract class Task {
    /** What the task is about. */
    protected String description;
    /** Whether the task has been completed. */
    protected boolean isDone;

    /**
     * Creates a task with the given description, initially not done.
     *
     * @param description what the task is about
     */
    protected Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /** Returns the task's description text. */
    public String getDescription() {
        return description;
    }

    /** Returns {@code "X"} if the task is done, or a single space otherwise. */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /** Marks this task as done. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this task as not done. */
    public void markAsNotDone() {
        isDone = false;
    }

    /** Shared helper for subclasses: returns the {@code 1}/{@code 0} done flag. */
    protected String getDoneBit() {
        return isDone ? "1" : "0";
    }

    /**
     * Returns this task encoded as one line for the data file. The first
     * field is a single-letter type tag ({@code T}, {@code D}, {@code E})
     * that {@link #fromFileFormat(String)} dispatches on.
     *
     * @return the data-file line for this task
     */
    public abstract String toFileFormat();

    /**
     * Reconstructs a task from one line of the data file produced by
     * {@link #toFileFormat()}, choosing the subclass from the leading type
     * field.
     *
     * @param line one line of the data file
     * @return the task the line describes
     * @throws IllegalArgumentException if the line is not in the expected
     *     format (wrong field count, unknown type, bad done flag, empty
     *     description, or an unparseable date). Callers use this to detect a
     *     corrupted data file.
     */
    public static Task fromFileFormat(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 3) {
            throw new IllegalArgumentException("expected at least 3 fields but found " + parts.length);
        }
        String type = parts[0].trim();
        boolean done = parseDoneFlag(parts[1].trim());
        String description = parts[2].trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("description is empty");
        }

        Task task;
        switch (type) {
            case "T":
                requireFieldCount(parts, 3);
                task = new Todo(description);
                break;
            case "D":
                requireFieldCount(parts, 4);
                task = new Deadline(description, DateTime.parse(parts[3].trim()));
                break;
            case "E":
                requireFieldCount(parts, 5);
                task = new Event(description,
                        DateTime.parse(parts[3].trim()), DateTime.parse(parts[4].trim()));
                break;
            default:
                throw new IllegalArgumentException("unknown task type '" + type + "'");
        }
        if (done) {
            task.markAsDone();
        }
        return task;
    }

    private static boolean parseDoneFlag(String flag) {
        if (flag.equals("1")) {
            return true;
        }
        if (flag.equals("0")) {
            return false;
        }
        throw new IllegalArgumentException("done flag must be 0 or 1 but was '" + flag + "'");
    }

    private static void requireFieldCount(String[] parts, int expected) {
        if (parts.length != expected) {
            throw new IllegalArgumentException(
                    "expected " + expected + " fields but found " + parts.length);
        }
    }

    /** Returns the display form, e.g. {@code [X] read book}. */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
