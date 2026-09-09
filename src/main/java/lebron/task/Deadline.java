package lebron.task;

/** A task that must be completed by a given date (and optionally time). */
public class Deadline extends Task {
    private final DateTime by;

    /**
     * Creates a deadline task.
     *
     * @param description what needs to be done
     * @param by when it is due
     */
    public Deadline(String description, DateTime by) {
        super(description);
        this.by = by;
    }

    /** Returns the data-file line, e.g. {@code D | 0 | return book | 2019-12-02}. */
    @Override
    public String toFileFormat() {
        return "D | " + getDoneBit() + " | " + description + " | " + by.toFileFormat();
    }

    /** Returns the display form, e.g. {@code [D][ ] return book (by: Dec 02 2019)}. */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
