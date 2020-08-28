import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class Solution {
    protected TaskGraph taskGraph;
    protected int numProcessors;
    protected int numTasks;

    protected int[] nodePriorities;
    protected ArrayList<Integer>[] equivalentNodesList;
    protected int[] maxLengthToExitNode;

    protected volatile int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    protected volatile int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    protected volatile int bestFinishTime; // earliest finishing time of schedules we have searched
    protected volatile HashSet<Integer> seenSchedules = new HashSet<>();

    protected volatile long stateCount;
    protected volatile boolean isDone;

    public abstract Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime);

    protected synchronized void updateStateCount() {
        this.stateCount++;
    }

    protected synchronized void setDone() {
        isDone = true;
    }
}
