import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.io.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * @author SzeYing
 * @since 2017-02-17
 */
public class RequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private static final int BREAKFAST_START = 800;
    private static final int BREAKFAST_END = 1000;
    private static final int LUNCH_START = 1200;
    private static final int LUNCH_END = 1400;
    private static final int DINNER_START = 1800;
    private static final int DINNER_END = 2000;

    private static final int STANDARD_SLEEP = 2200;
    private static final int STANDARD_WAKE = 600;

    private int startTime;
    private int endTime;

    Parser parser;

    private long chatId;

    private Command command;


    public RequestHandler(Update update) {
        parser = new Parser(update);
    }

    public SendMessage execute() {
        this.command = parser.getCommand();
        this.chatId = parser.getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        switch (command) {
            case GREETING:
                return sendGreeting();
            case ADD:
                return addTask();
            case GENERATE:
                return generateCalendar();
            case VIEW:
                return viewTasks();
            case CLEAR:
                return clear();
            case ROUTINE:
                if (parser.getNumHoursOfSleep() == 8) {
                    sendMessage.setText("Healthy choice! \uD83D\uDE24");
                } else if (parser.getNumHoursOfSleep() < 8) {
                    sendMessage.setText("You should really get at least 8 hours of sleep, but I'll close an eye. \uD83D\uDE2B");
                } else {
                    sendMessage.setText("Wow! Perhaps you should think about staying awake more to do more productive things. \uD83D\uDE1E");
                }
                return sendMessage;
            case UNKNOWN:
                executePost();
            default:
                sendMessage.setText("Sorry, I didn't understand that... \uD83D\uDE28");
                return sendMessage;
        }
    }

    private SendMessage sendGreeting() {
        List<String> messages = new ArrayList<>();
        messages.add("Hi, I’m CalPal. You seem like a very busy person. " +
                "I can help you generate an optimal schedule. \uD83D\uDE0A \n\n" +
                "You should start by telling me what you have to do tomorrow!\n\n");
        messages.add("Hi, I’m CalPal. You seem like a very busy person. " +
                "I can help you generate an optimal schedule. \uD83D\uDE0A \n\n" +
                "You should start by telling me what you have to do tomorrow!\n\n");
        messages.add("Hi, I’m CalPal. You seem like a very busy person. " +
                "I can help you generate an optimal schedule. \uD83D\uDE0A \n\n" +
                "You should start by telling me what you have to do tomorrow!\n\n");
        messages.add("Hi, I'm CalPal! I'm so excited to meet you, and help you sort out your busy schedule! \uD83D\uDE0A \n\n" +
                "First, what do you need to do tomorrow?");
        messages.add("Hi, I'm CalPal! I'm so excited to meet you, and help you sort out your busy schedule! \uD83D\uDE0A \n\n" +
                "First, what do you need to do tomorrow?");
        messages.add("Hi, I'm CalPal! I'm so excited to meet you, and help you sort out your busy schedule! \uD83D\uDE0A \n\n" +
                "First, what do you need to do tomorrow?");
        messages.add("Sometimes I'm shy... Could you say hello again?");

        Random randomizer = new Random();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messages.get(randomizer.nextInt(messages.size())));

        return sendMessage;
    }

    private SendMessage addTask() {
        String taskName = parser.getTaskName();
        int numHours = parser.getNumHours();

        String outputFileName = String.valueOf(chatId) + ".csv";
        List<String> fileContent = new ArrayList<>();

        try {
            fileContent = readFile(outputFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        fileContent.add(taskName + "," + numHours);

        try {
            writeToFile(outputFileName, fileContent);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (isWork(taskName) || isNotAsUrgentWork(taskName)) {
            sendMessage.setText(getRandomPoorThingMessage());
        } else if (isBeforeBreakfast(taskName)) {
            sendMessage.setText(getRandomGoMessage());
        } else {
            sendMessage.setText(getRandomAddTaskMessage());
        }

        return sendMessage;
    }

    private String getRandomPoorThingMessage() {
        List<String> messages = new ArrayList<>();
        messages.add("Oh you poor thing...");
        messages.add("\uD83D\uDE2A");
        messages.add("Hang in there!");

        Random randomizer = new Random();
        return messages.get(randomizer.nextInt(messages.size()));
    }

    private String getRandomGoMessage() {
        List<String> messages = new ArrayList<>();
        messages.add("That's right \uD83D\uDE0D");
        messages.add("Let's work those muscles!");
        messages.add("Werk werk werk werk werk");

        Random randomizer = new Random();
        return messages.get(randomizer.nextInt(messages.size()));
    }

    private String getRandomAddTaskMessage() {
        List<String> messages = new ArrayList<>();
        messages.add("Cool. Give me more!");
        messages.add("I used to do that when I was young. My mum didn't like it.");

        Random randomizer = new Random();
        return messages.get(randomizer.nextInt(messages.size()));
    }


    private SendMessage viewTasks() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        String outputFileName = String.valueOf(chatId) + ".csv";
        List<String> fileContent = new ArrayList<>();

        try {
            fileContent = readFile(outputFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (fileContent.isEmpty()) {
            sendMessage.setText("You don't have any tasks... Add one?");
            return sendMessage;
        }

        StringBuilder builder = new StringBuilder();

        for (String line : fileContent) {
            if (line.endsWith("ROUTINE")) {
                line = line.replaceAll(",ROUTINE", "");
            }
            builder.append(line + "\n");
        }

        sendMessage.setText(builder.toString());
        return sendMessage;
    }

    private SendMessage generateCalendar() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        String outputFileName = String.valueOf(chatId) + ".csv";
        List<String> fileContent = new ArrayList<>();

        try {
            fileContent = readFile(outputFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (fileContent.isEmpty()) {
            sendMessage.setText("You don't have any tasks... Wanna add one?");
            return sendMessage;
        }

        List<Task> rankedTasks = rankTasks(fileContent);
        LOGGER.debug("WE RANKED YOUR TASKS");

        // Generate calendar
        List<Task> leftover = new ArrayList<>();
        String recommendations =
                putTasksOnCalendar(parser.getNumHoursOfSleep(), rankedTasks, leftover);
        LOGGER.debug("WE PREPARED YOUR CALENDAR");

//        return generateCalendarImage(rankedTasks, leftover);

        // TODO: DEBUG, should be photo
        StringBuilder builder = new StringBuilder();

        builder.append("I got your calendar all sorted out \uD83D\uDE09 \n\n");

        // Start time of the day - when to wake up
        int offset = (8 - parser.getNumHoursOfSleep());

        LOGGER.debug("Need {} hours of sleep - offset is {}", parser.getNumHoursOfSleep(), offset);

        int currTime = STANDARD_WAKE;
        int sleepTime = STANDARD_SLEEP;

        LOGGER.debug("Wake up at {}, Sleep at {}", currTime, sleepTime);

        if (offset != 0 && Math.abs(offset) >= 2) {
            sleepTime += (offset / 2) * 100;
            if (offset % 2 != 0) {
                currTime += (offset / 2) * 100 + 100;
            } else {
                currTime += (offset / 2) * 100;
            }
        } else if (Math.abs(offset) == 100) {
            currTime += offset * 100;
        }

        currTime = Math.max(currTime, 0);
        sleepTime = Math.min(sleepTime, 2400);
        int totalHours = (sleepTime - currTime) / 100;

        int hour = currTime;
        int numFreeHours = 0;

        while(hour <= sleepTime) {

            if (!rankedTasks.isEmpty() && rankedTasks.get(0).getStartTime() == hour) {
                Task printTask = rankedTasks.remove(0);
                int numHoursLeft = printTask.getNumHours();

//                if (!builder.toString().endsWith("-\n")) {
//                    builder.append(generateLine(0) + "\n");
//                }

                while (numHoursLeft > 0) {
                    String hourStr = String.valueOf(hour);

                    if (hour < 1000) {
                        hourStr = "0" + hourStr;
                    }

                    hourStr = hourStr.substring(0, 2) + ":" + hourStr.substring(2, 4);
                    if (numHoursLeft == printTask.getNumHours()) {
                        builder.append("`" + hourStr + "\t\t\t" + StringUtils.capitalize(printTask.getName()) + ""
                                + generateLine(printTask.getName().length() + 10) + "`\n");
                    } else {
                        builder.append("`" + hourStr + "\t\t\t" + generateLine(printTask.getName().length()) + "`\n");
                    }

                    numHoursLeft--;
                    hour+=100;
                }

//                builder.append(generateLine(0) + "\n");

            } else {
                String hourStr = String.valueOf(hour);

                if (hour < 1000) {
                    hourStr = "0" + hourStr;
                }

                hourStr = hourStr.substring(0, 2) + ":" + hourStr.substring(2, 4);

                builder.append(hourStr + "\n");
                hour+=100;
                numFreeHours++;
            }
        }

        double productivity = (((double) totalHours - numFreeHours) / totalHours) * 100;

        builder.append("\n");
        builder.append("You are using *" + new DecimalFormat("0.##").format(productivity) + "%* of your time.");
        builder.append("\n");

        if (!leftover.isEmpty()) {
            builder.append("\n");
            builder.append("*Take a break* and do these tomorrow, dear.\n");

            for (Task task : leftover) {
                builder.append("\uD83D\uDEAB \t\t\t" + StringUtils.capitalize(task.getName()) + " - " + task.getNumHours() +
                        (task.getNumHours() > 1 ? " hours" : " hour"));
                builder.append("\n");
            }
        } else if (!recommendations.isEmpty()) {
            builder.append("\n");
            builder.append("Since you have some time, maybe you want to check out these *FREE events at NYU* today!");
            builder.append("\n");
            builder.append(recommendations);
        }

        sendMessage.setParseMode("Markdown");
        sendMessage.setText(builder.toString());

        return sendMessage;
    }

    private String generateLine(int numChars) {
        String result = "";
        int num = 100 - numChars;

        for (int i = 0; i < num; i++) {
            result += "\t";
        }

        return result;
    }

    /**
     * Rank tasks
     *
     * @param input each line is of format "task name,num hours"
     * @return
     */
    private List<Task> rankTasks(List<String> input) {
        List<Task> rankedTasks = new ArrayList<>();

        for (String line : input) {
            int priority = computePriority(line);
            int secondPriority = computeSecondPriority(line, priority);

            Task newTask = new Task(line, priority, secondPriority);
            rankedTasks.add(newTask);
        }

        Collections.sort(rankedTasks);

        return rankedTasks;
    }

    /**
     * Generate calendar
     *
     * @param tasks
     * @param leftover
     */
    private String putTasksOnCalendar(int numHoursOfSleep, List<Task> tasks, List<Task> leftover) {

        List<Task> calendar = new ArrayList<>();

        // Start time of the day - when to wake up
        int offset = (8 - numHoursOfSleep);

        LOGGER.debug("Need {} hours of sleep - offset is {}", numHoursOfSleep, offset);

        int currTime = STANDARD_WAKE;
        int sleepTime = STANDARD_SLEEP;

        LOGGER.debug("Wake up at {}, Sleep at {}", currTime, sleepTime);

        if (offset != 0 && Math.abs(offset) >= 2) {
            sleepTime += (offset / 2) * 100;
            if (offset % 2 != 0) {
                currTime += (offset / 2) * 100 + 100;
            } else {
                currTime += (offset / 2) * 100;
            }
        } else if (Math.abs(offset) == 100) {
            currTime += offset * 100;
        }

        currTime = Math.max(currTime, 0);
        int wakeTime = currTime;
        sleepTime = Math.min(sleepTime, 2400);

        // Collect the same period tasks in list based on priority
        List<Task> beforeBreakfast = new ArrayList<>();
        List<Task> morning = new ArrayList<>();
        List<Task> afternoon = new ArrayList<>();
        List<Task> night = new ArrayList<>();
        List<Task> noTime = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getPriority() == 0) {
                beforeBreakfast.add(task);
            } else if (task.getPriority() == 1) {
                morning.add(task);
            } else if (task.getPriority() == 2) {
                afternoon.add(task);
            } else if (task.getPriority() == 3) {
                night.add(task);
            } else {
                noTime.add(task);
            }

        }

        LOGGER.debug("{} before breakfast, {} before lunch, {} before dinner, {} before sleep.",
                beforeBreakfast.size(), morning.size(), afternoon.size(), night.size());

        // Pick tasks for each time period
        calendar.addAll(pickTasksForTimePeriod(currTime, BREAKFAST_END, beforeBreakfast));
        if (!calendar.isEmpty()) {
            currTime = computeCurrTime(Math.max(calendar.get(calendar.size() - 1).getEndTime(), BREAKFAST_START), BREAKFAST_END);

            Task breakfast = new Task("Breakfast,1", 777, 777);
            breakfast.setStartTime(currTime);
            breakfast.setEndTime(currTime + 100);
            calendar.add(breakfast);

            currTime += 100;
        } else {
            Task breakfast = new Task("Breakfast,1", 777, 777);
            breakfast.setStartTime(BREAKFAST_START);
            breakfast.setEndTime(BREAKFAST_START + 100);
            calendar.add(breakfast);

            currTime = BREAKFAST_START + 100;
        }

        calendar.addAll(pickTasksForTimePeriod(currTime, LUNCH_END, morning));
        if (!calendar.isEmpty()) {
            currTime = computeCurrTime(Math.max(calendar.get(calendar.size() - 1).getEndTime(), LUNCH_START), LUNCH_END);

            Task lunch = new Task("Lunch,1", 777, 777);
            lunch.setStartTime(currTime);
            lunch.setEndTime(currTime + 100);
            calendar.add(lunch);

            currTime += 100;
        } else {
            Task lunch = new Task("Lunch,1", 777, 777);
            lunch.setStartTime(LUNCH_START);
            lunch.setEndTime(LUNCH_END + 100);
            calendar.add(lunch);

            currTime = LUNCH_START + 100;
        }

        calendar.addAll(pickTasksForTimePeriod(currTime, DINNER_END, afternoon));
        if (!calendar.isEmpty()) {
            currTime = computeCurrTime(Math.max(calendar.get(calendar.size() - 1).getEndTime(), DINNER_START), DINNER_END);

            Task dinner = new Task("Dinner,1", 777, 777);
            dinner.setStartTime(currTime);
            dinner.setEndTime(currTime + 100);
            calendar.add(dinner);

            currTime += 100;
        } else {
            Task dinner = new Task("Dinner,1", 777, 777);
            dinner.setStartTime(DINNER_START);
            dinner.setEndTime(DINNER_END + 100);
            calendar.add(dinner);

            currTime = DINNER_START + 100;
        }

        calendar.addAll(pickTasksForTimePeriod(currTime, sleepTime, night));

        slotInOtherTasks(wakeTime, sleepTime, calendar, noTime);

        String recommendations = collectRecommendations(wakeTime, sleepTime, calendar);

        tasks.clear();
        tasks.addAll(calendar);
        leftover.addAll(beforeBreakfast);
        leftover.addAll(morning);
        leftover.addAll(afternoon);
        leftover.addAll(night);
        leftover.addAll(noTime); // uncategorized

        return recommendations;
    }

    private int computeCurrTime(int lastEndTime, int fixedEndTime) {
        if (lastEndTime < fixedEndTime) {
            return lastEndTime;
        } else {
            return fixedEndTime;
        }
    }

    private String collectRecommendations(int startDay, int endDay, List<Task> calendar) {
        StringBuilder builder = new StringBuilder();

        JSONArray json = new JSONArray(executePost());
        List<JSONObject> rel = new ArrayList<>();

        for (Object obj : json) {
            JSONObject jsonObj = (JSONObject) obj;
            if (!jsonObj.getString("date").startsWith("February 19")) {
                break;
            }

            rel.add(jsonObj);
        }

        int ptr = startDay;
        int eventPtr = 0;

        while (ptr < endDay) {
            if (eventPtr < calendar.size() && ptr < calendar.get(eventPtr).getStartTime()) {
                // Free time before next event

                builder.append(getEventsBetweenRange(ptr, calendar.get(eventPtr).getStartTime(), rel));

                ptr = calendar.get(eventPtr).getEndTime();
                eventPtr++;

            } else if (eventPtr >= calendar.size()) {
                // Time between last event and sleep time

                builder.append(getEventsBetweenRange(ptr, endDay, rel));

                ptr = endDay;
            } else {
                // Go to the next event

                ptr = calendar.get(eventPtr).getEndTime();
                eventPtr++;
            }

        }
        return builder.toString();
    }

    private String getEventsBetweenRange(int start, int end, List<JSONObject> arr) {
        StringBuilder builder = new StringBuilder();
        for (JSONObject event : arr) {
            if (event.getString("date_time").contains("<span class=\"lw_date_separator\">-</span>")) {
                String[] dates = event.getString("date_time").trim().split(" <span class=\"lw_date_separator\">-</span>");
                LOGGER.debug("THIS IS THE START {} AND THE END {}", dates[0], dates[1]);

                String[] tokens1 = dates[0].trim().substring(0, dates[0].trim().length() - 2).split(":");
                int startTimeOfEvent = Integer.parseInt(tokens1[0]) * 100 + Integer.parseInt(tokens1[1]);

                if (dates[0].trim().endsWith("pm")) {
                    startTimeOfEvent += 1200;
                }

                String[] tokens2 = dates[1].trim().substring(0, dates[1].trim().length() - 2).split(":");
                int endTimeOfEvent = Integer.parseInt(tokens2[0]) * 100 + Integer.parseInt(tokens2[1]);

                if (dates[1].trim().endsWith("pm")) {
                    endTimeOfEvent += 1200;
                }

                // Found event!!!!!
                if (startTimeOfEvent >= start && endTimeOfEvent <= end) {
                    LOGGER.debug("Found event! {} after start {}, {} before end {}",
                            startTimeOfEvent, start, endTimeOfEvent, end);
                    builder.append("\uD83D\uDE4B \t\t\t[" + event.getString("title") + "]("
                            + event.getString("url") + ") at " + dates[0] + " - " + dates[1]);
                    builder.append("\n");
                }
            }
        }

        return builder.toString();
    }

    private void slotInOtherTasks(int startDay, int endDay, List<Task> calendar, List<Task> noTime) {
        Collections.sort(noTime);

        int ptr = startDay;
        int eventPtr = 0;

        // Loop through day
        while (ptr < endDay && noTime.size() != 0) {

            // In the middle of an event
            if (eventPtr < calendar.size() && ptr >= calendar.get(eventPtr).getStartTime()) {
                ptr = calendar.get(eventPtr).getEndTime();
                eventPtr++;
                continue;
            }

            // Not in the middle of an event
            if (eventPtr < calendar.size()) {
                if ((noTime.get(0).getNumHours() * 100) + ptr > calendar.get(eventPtr).getStartTime()) {
                    // Will exceed next event

                    ptr = calendar.get(eventPtr).getEndTime();
                    eventPtr++;

                } else {
                    // Won't exceed next event
                    Task addTask = noTime.remove(0);
                    addTask.setStartTime(ptr);
                    addTask.setEndTime(ptr + (addTask.getNumHours() * 100));

                    calendar.add(eventPtr, addTask);
                    eventPtr++;
                    ptr = addTask.getEndTime();

                }
            } else if (noTime.get(0).getNumHours() * 100 + ptr > endDay) {
                // Will exceed end of day

                ptr = endDay;
            } else {
                // Free time till end of day can fill

                Task addTask = noTime.remove(0);
                addTask.setStartTime(ptr);
                addTask.setEndTime(ptr + (addTask.getNumHours() * 100));

                calendar.add(eventPtr, addTask);
                eventPtr++;
                ptr = addTask.getEndTime();
            }
        }
    }

    private List<Task> pickTasksForTimePeriod(int start, int end, List<Task> tasks) {
        List<Task> selected = new ArrayList<>();

        LOGGER.debug("Start time for this period: {}, End time for this period: {}", start, end);

        while (start < end - 100 && stillPossible(tasks, end - start - 100)) {
            // Pick random task
            Task randomTask = getRandomTask(tasks);

            while ((randomTask.getNumHours() * 100) + start > end) {
                randomTask = getRandomTask(tasks);
            }
            randomTask.setStartTime(start);
            randomTask.setEndTime(start + (randomTask.getNumHours() * 100));
            selected.add(randomTask);
            tasks.remove(randomTask);
            start = randomTask.getEndTime();
        }

        return selected;
    }

    private boolean stillPossible(List<Task> tasks, int numHoursLeft) {
        for (Task task: tasks) {
            if (task.getNumHours() <= numHoursLeft) {
                return true;
            }
        }

        return false;
    }

    private int computePriority(String line) {
        if (isBeforeBreakfast(line)) {
            return 0;
        }

        if (isMorning(line)) {
            return 1;
        }

        if (isAfternoon(line)) {
            return 2;
        }

        if (isEvening(line)) {
            return 3;
        }

        return 4;
    }

    private int computeSecondPriority(String line, int priority) {
        if (isWork(line)) {
            return 0;
        } else if (isNotAsUrgentWork(line)) {
            return 1;
        }

        return 2;
    }

    private SendMessage clear() {
        String filename = chatId + ".csv";

        try {
            writeToFile(filename, new ArrayList<>());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Cleared all tasks. You're all ready to start afresh \uD83D\uDE0C");
        return sendMessage;
    }

    private boolean isWork(String task) {
        List<String> keywords = new ArrayList<>();
        keywords.add("assignment");
        keywords.add("exam");

        for (String keyword : keywords) {
            if (task.toLowerCase().contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isNotAsUrgentWork(String task) {
        List<String> keywords = new ArrayList<>();
        keywords.add("work");
        keywords.add("homework");
        keywords.add("tutorial");
        keywords.add("study");
        keywords.add("revise");
        keywords.add("revision");
        keywords.add("studying");

        for (String keyword : keywords) {
            if (task.toLowerCase().contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBeforeBreakfast(String task) {
        List<String> keywords = new ArrayList<>();
        keywords.add("workout");
        keywords.add("exercise");
        keywords.add("exercising");
        keywords.add("gym");
        keywords.add("swim");
        keywords.add("run");
        keywords.add("jog");
        keywords.add("walk");

        for (String keyword : keywords) {
            if (task.toLowerCase().contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isMorning(String task) {
        List<String> keywords = new ArrayList<>();
        keywords.add("groceries");

        for (String keyword : keywords) {
            if (task.toLowerCase().contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAfternoon(String task) {
        List<String> keywords = new ArrayList<>();

        for (String keyword : keywords) {
            if (task.toLowerCase().contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isEvening(String task) {
        List<String> keywords = new ArrayList<>();
        keywords.add("hackathon");
        keywords.add("hackathons");
        keywords.add("tv");
        keywords.add("movie");
        keywords.add("date");

        for (String keyword : keywords) {
            if (task.toLowerCase().contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasClosingTime(String task) {
        List<String> keywords = new ArrayList<>();
        keywords.add("gym");
        keywords.add("shop");
        keywords.add("shopping");
        keywords.add("grocery");
        keywords.add("groceries");

        for (String keyword : keywords) {
            if (task.toLowerCase().contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private Task getRandomTask(List<Task> tasks) {
        // Collect tasks of same priority
        List<Task> samePriority = new ArrayList<>();

        int i = 0;
        Task curr = tasks.get(i);
        samePriority.add(curr);
        i++;

        while (i < tasks.size()
                && tasks.get(i).getPriority() == curr.getPriority()
                && tasks.get(i).getSecondPriority() == curr.getSecondPriority()) {
            curr = tasks.get(i);
            samePriority.add(curr);
            i++;
        }

        Random randomizer = new Random();
        return samePriority.get(randomizer.nextInt(samePriority.size()));
    }

    private List<String> readFile(String filename) throws IOException {
        File file = new File(filename);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        BufferedReader reader = new BufferedReader(new FileReader(filename));

        List<String> input = new ArrayList<>();
        String line = reader.readLine();

        while (line != null) {
            input.add(line);
            line = reader.readLine();
        }

        reader.close();

        return input;
    }

    private void writeToFile(String filename, List<String> content) throws IOException {
        File file = new File(filename);

        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        for (String line : content) {
            writer.write(line);
            writer.newLine();
        }

        writer.flush();
        writer.close();
    }

    public String executePost() {
        String url = "http://events.nyu.edu/live/json/events/category/Free/";

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            byte[] responseBody = method.getResponseBody();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary data
            System.out.println(new String(responseBody));

            return new String(responseBody);

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    }


    public Command getCommand() {
        return command;
    }

    enum Command {
        GREETING,
        ADD,
        VIEW,
        EDIT,
        CLEAR,
        GENERATE,
        UNKNOWN,
        ROUTINE
    }

}
