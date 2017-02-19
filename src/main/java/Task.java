/**
 * @author SzeYing
 * @since 2017-02-18
 */
public class Task implements Comparable<Task> {
    private String name;
    private int numHours;
    private int priority;
    private int secondPriority;
    private int latestTime;

    private int startTime;
    private int endTime;

    public Task(String rawInput, int priority, int secondPriority) {
        this.name = rawInput.split(",")[0];
        this.numHours = Integer.parseInt(rawInput.split(",")[1].trim());
        this.priority = priority;
        this.secondPriority = secondPriority;

        setLatestTime();
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public void setSecondPriority(int secondPriority) {
        this.secondPriority = secondPriority;
        setLatestTime();
    }

    private void setLatestTime() {
        int decision;

        if (getPriority() < getSecondPriority()) {
            decision = secondPriority;
        } else {
            decision = priority;
        }

        switch(decision) {
            case 0:
                this.latestTime = 1200;
                break;
            case 1:
                this.latestTime = 1800;
                break;
            case 2:
                this.latestTime = 2400;
                break;
            case 3:
                this.latestTime = 2100;
            case 4:
                this.latestTime = 2000;
            default:
                this.latestTime = 2400;
        }
    }

    public String getName() {
        return name;
    }

    public int getNumHours() {
        return numHours;
    }

    public void setNumHours(int numHours) {
        this.numHours = numHours;
    }

    public int getSecondPriority() {
        return secondPriority;
    }

    public int getPriority() {
        return priority;
    }

    public int getPriorityScore() {
        return (this.priority * 100) + (this.secondPriority * 10) + this.numHours;
    }

    public int getLatestTime() {
        return latestTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    @Override
    public int compareTo(Task task) {
        int thisScore = this.getPriorityScore();
        int thatScore = task.getPriorityScore();

        if (thisScore > thatScore) {
            return 1;
        } else if (thisScore < thatScore) {
            return -1;
        } else {
            return 0;
        }
    }
}
