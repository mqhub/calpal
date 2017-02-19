import org.telegram.telegrambots.api.objects.Update;

/**
 * @author SzeYing
 * @since 2017-02-17
 */
public class Parser {
    private RequestHandler.Command command;
    private String taskName;
    private int numHours;
    private int numHoursOfSleep;
    private long chatId;
    private String message;

    public Parser(Update update) {
        this.chatId = update.getMessage().getChatId();
        this.message = update.getMessage().getText();

        this.numHoursOfSleep = 8; // Standard number of hours

        parse(message);
    }

    public long getChatId() {
        return chatId;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getNumHours() {
        return numHours;
    }

    public RequestHandler.Command getCommand() {
        return command;
    }

    public int getNumHoursOfSleep() {
        return numHoursOfSleep;
    }

    public void setNumHoursOfSleep(int num) {
        this.numHoursOfSleep = num;
    }

    private void parse(String message) {
        if (message.contains(",")) {
            this.command = RequestHandler.Command.ADD;

            String[] tokens = message.trim().split(",");

            if (tokens.length > 1) {
                this.taskName = tokens[0].trim();
                String time = tokens[1].trim();
                this.numHours = Integer.parseInt(time.substring(0, time.indexOf("hour")).trim());
            }

        } else if (message.matches(".*need.*[0-9].*hours.*sleep.*")) { // how many number of hours
            this.command = RequestHandler.Command.ROUTINE;

            setNumHoursOfSleep(Integer.parseInt(message.split("need")[1].split("hours")[0].trim()));

        } else if (message.matches(".*don't.*need.*sleep.*")) { // how many number of hours
            this.command = RequestHandler.Command.ROUTINE;

            setNumHoursOfSleep(0);

        } else if (message.toLowerCase().contains("view")) {
            this.command = RequestHandler.Command.VIEW;
        } else if (message.toLowerCase().contains("generate") || message.toLowerCase().contains("calendar")) {
            this.command = RequestHandler.Command.GENERATE;
        } else if (message.toLowerCase().contains("clear")) {
            this.command = RequestHandler.Command.CLEAR;
        } else if (message.matches(".*have to.*today.*")) {
            this.command = RequestHandler.Command.EDIT;
            this.taskName = message.replaceAll(".*have.*to", "").trim();
            this.taskName = message.replaceAll("today.*", "").trim();
        } else if (message.toLowerCase().contains("start") ||message.toLowerCase().contains("hello")
                || message.toLowerCase().contains("hey") || message.toLowerCase().contains("hi")) {
            this.command = RequestHandler.Command.GREETING;
        } else {
            this.command = RequestHandler.Command.UNKNOWN;
        }
    }

}
