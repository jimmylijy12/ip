package lebron;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import lebron.exception.LebronException;
import lebron.parser.ParsedCommand;
import lebron.parser.Parser;
import lebron.storage.Storage;
import lebron.task.Task;
import lebron.task.TaskList;
import lebron.task.Todo;
import lebron.ui.Ui;

/**
 * The chatbot's backend: holds the task list and storage, and turns one line
 * of user input into a response. Used by both the console entry point
 * ({@link #main(String[])}) and the JavaFX GUI ({@link Main}).
 */
public class Lebron {
    private final Storage storage;
    private final TaskList tasks;
    private boolean isExit = false;

    /**
     * Creates a chatbot backed by the given data file.
     *
     * @param dataFile where tasks are loaded from and saved to
     */
    public Lebron(Path dataFile) {
        this.storage = new Storage(dataFile);
        this.tasks = new TaskList(storage.load());
    }

    /**
     * Processes one line of user input and returns the text to show the
     * user. Never throws -- a {@link LebronException}'s message becomes the
     * response text, the same text the console prints for an error.
     *
     * @param input the raw line the user typed
     * @return the response text
     */
    public String getResponse(String input) {
        isExit = false;
        try {
            ParsedCommand command = Parser.parse(input);
            isExit = command.getType() == ParsedCommand.Type.BYE;
            return execute(command);
        } catch (LebronException e) {
            return e.getMessage();
        }
    }

    /**
     * Returns whether the most recent {@link #getResponse(String)} call was
     * a {@code bye} command, i.e. whether the caller should now exit.
     *
     * @return true if the chatbot should exit
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * Carries out one parsed command against the task list.
     *
     * @param command the command to run
     * @return the response text
     * @throws LebronException if the command cannot be completed (e.g. a
     *     task number that is out of range)
     */
    private String execute(ParsedCommand command) throws LebronException {
        switch (command.getType()) {
            case LIST: {
                List<Task> all = tasks.asList();
                StringBuilder message = new StringBuilder("Here are the tasks in your list:");
                for (int i = 0; i < all.size(); i++) {
                    message.append(System.lineSeparator()).append(i + 1).append('.').append(all.get(i));
                }
                return message.toString();
            }
            case TODO:
                tasks.add(new Todo(command.getDescription()));
                storage.save(tasks);
                return "added: " + command.getDescription();
            case DEADLINE:
            case EVENT:
                tasks.add(command.getTask());
                storage.save(tasks);
                return "added: " + command.getTask();
            case MARK:
            case UNMARK: {
                boolean isMark = command.getType() == ParsedCommand.Type.MARK;
                Task task = isMark ? tasks.mark(command.getIndex()) : tasks.unmark(command.getIndex());
                storage.save(tasks);
                String header = isMark
                        ? "Nice! I've marked this task as done:"
                        : "OK, I've marked this task as not done yet:";
                return header + System.lineSeparator() + "  " + task;
            }
            case DELETE: {
                Task removed = tasks.delete(command.getIndex());
                storage.save(tasks);
                return "Noted. I've removed this task:" + System.lineSeparator() + "  " + removed
                        + System.lineSeparator() + "Now you have " + tasks.size() + " tasks in the list.";
            }
            case FIND: {
                List<Task> matches = tasks.find(command.getDescription());
                StringBuilder message = new StringBuilder("Here are the matching tasks in your list:");
                for (int i = 0; i < matches.size(); i++) {
                    message.append(System.lineSeparator()).append(i + 1).append('.').append(matches.get(i));
                }
                return message.toString();
            }
            case BYE:
                return "Bye. Hope to see you again soon!";
            default:
                // Parser only ever returns the types handled above, so this
                // is unreachable given correct code; the assert documents
                // that and catches a regression (e.g. a new ParsedCommand.Type
                // added without a case here) under -ea, while the exception
                // still gives defined (if generic) behaviour when it's not.
                assert false : "Unreachable: unhandled command type " + command.getType();
                throw new LebronException("OOPS!!! I don't understand that command.");
        }
    }

    /**
     * Runs the console loop: reads and executes commands until the user
     * exits, printing each response through {@code ui}.
     *
     * @param ui the console UI to read commands from and print responses to
     */
    private void runCli(Ui ui) {
        boolean exit = false;
        while (!exit) {
            String fullCommand = ui.readCommand();
            ui.showLine();
            ui.showMessage(getResponse(fullCommand));
            exit = isExit();
            ui.showLine();
        }
        ui.close();
    }

    /**
     * Console entry point. Tasks are persisted to {@code ./data/lebron.txt}
     * (relative to the working directory); the path is built from segments
     * so it works on any OS, and {@link Storage} handles the file/folder not
     * existing yet.
     *
     * <p>The greeting is shown, and only then is the data file loaded (by
     * constructing {@code Lebron}), so a "your data file is corrupted"
     * warning prints after the banner, not before it.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();
        Lebron lebron = new Lebron(Paths.get("data", "lebron.txt"));
        lebron.runCli(ui);
    }
}
