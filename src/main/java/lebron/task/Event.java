package lebron.task;

/** A task that spans a period, from a start date/time to an end date/time. */
public class Event extends Task {
    private final DateTime from;
    private final DateTime to;

    /**
     * Creates an event task.
     *
     * @param description what the event is
     * @param from when it starts
     * @param to when it ends
     */
    public Event(String description, DateTime from, DateTime to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /** Returns the data-file line, e.g. {@code E | 0 | camp | 2019-06-01 | 2019-06-03}. */
    @Override
    public String toFileFormat() {
        return "E | " + getDoneBit() + " | " + description + " | "
                + from.toFileFormat() + " | " + to.toFileFormat();
    }

    /** Returns the display form, e.g. {@code [E][ ] camp (from: Jun 01 2019 to: Jun 03 2019)}. */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
