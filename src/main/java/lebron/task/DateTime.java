package lebron.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A calendar date, optionally with a time of day, as used by deadline and
 * event tasks.
 *
 * <p>Input is parsed from a few common formats -- ISO {@code yyyy-MM-dd} and
 * day-first {@code d/M/yyyy}, each optionally followed by a 24-hour
 * {@code HHmm} time (e.g. {@code 2/12/2019 1800}). It is printed back in a
 * friendlier form such as {@code Oct 15 2019} or {@code Dec 02 2019 6:00pm},
 * and stored in the data file in a single canonical form that always
 * re-parses.
 */
public class DateTime {
    /** Input formats that include a time; tried before the date-only ones. */
    private static final DateTimeFormatter[] DATE_TIME_INPUTS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm"),
        DateTimeFormatter.ofPattern("d/M/yyyy HHmm"),
    };
    /** Input formats without a time. */
    private static final DateTimeFormatter[] DATE_INPUTS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
    };
    private static final DateTimeFormatter DATE_OUTPUT = DateTimeFormatter.ofPattern("MMM dd yyyy");
    private static final DateTimeFormatter TIME_OUTPUT = DateTimeFormatter.ofPattern("h:mma");
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FILE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    private final LocalDate date;
    private final LocalTime time; // null when the input carried no time

    private DateTime(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    /**
     * Parses user input such as {@code 2019-10-15} or {@code 2/12/2019 1800}.
     *
     * @param input the date (optionally with a time) as the user typed it
     * @return the parsed date/time
     * @throws IllegalArgumentException if none of the accepted formats match;
     *     callers turn this into a user-facing error message
     */
    public static DateTime parse(String input) {
        String text = input.trim();
        for (DateTimeFormatter format : DATE_TIME_INPUTS) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(text, format);
                return new DateTime(parsed.toLocalDate(), parsed.toLocalTime());
            } catch (DateTimeParseException e) {
                // not this format -- try the next
            }
        }
        for (DateTimeFormatter format : DATE_INPUTS) {
            try {
                return new DateTime(LocalDate.parse(text, format), null);
            } catch (DateTimeParseException e) {
                // not this format -- try the next
            }
        }
        throw new IllegalArgumentException("unrecognised date/time: '" + input + "'");
    }

    /** Returns this date/time, one day later (same time of day, if any). */
    public DateTime plusDays(long days) {
        return new DateTime(date.plusDays(days), time);
    }

    /** Returns this date/time, one week later (same time of day, if any). */
    public DateTime plusWeeks(long weeks) {
        return new DateTime(date.plusWeeks(weeks), time);
    }

    /** Returns this date/time, one month later (same time of day, if any). */
    public DateTime plusMonths(long months) {
        return new DateTime(date.plusMonths(months), time);
    }

    /**
     * Returns the canonical string written to the data file: {@code yyyy-MM-dd}
     * for a date, or {@code yyyy-MM-dd HHmm} when a time is present.
     */
    public String toFileFormat() {
        return time == null
                ? date.format(FILE_DATE)
                : LocalDateTime.of(date, time).format(FILE_DATE_TIME);
    }

    /**
     * Returns the friendly display form, e.g. {@code Oct 15 2019} or
     * {@code Dec 02 2019 6:00pm}.
     */
    @Override
    public String toString() {
        String shown = date.format(DATE_OUTPUT);
        if (time != null) {
            // e.g. "6:00PM" -> "6:00pm"
            shown += " " + time.format(TIME_OUTPUT).toLowerCase();
        }
        return shown;
    }
}
