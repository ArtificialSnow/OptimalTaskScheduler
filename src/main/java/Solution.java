import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Deque;
import java.util.List;
import java.util.*;

public class Solution {

    int count = 0;


    List<Integer>[] parentsList;
    List<Integer>[] childrenList;
    int[] durations;
    int[][] commCosts;
    LinkedList<Integer> scheduleCandidates;

    //Info for BackTracking
    int[] inDegrees;
    int[] startTimes;
    int[] scheduledOn;
    int[] processorAvailability;
    int[] bestStartTime;
    int[] bestScheduledOn;
    int totalSum = 0;
    int numProcessors = 0;
    int bestTime;
    boolean firstRecurseDone = false; //Note not 100% sure

    //count the number of tasks remaining and their run times and if that plus the schedule time + currentnode run time is > the best time we have return.
    public Solution(TaskGraph taskGraph, int numProcessors, int greedySolution){
        bestTime = Integer.MAX_VALUE;
        int n = taskGraph.getNumberOfTasks();
        inDegrees = new int[n];
        bestStartTime = new int[n];
        bestScheduledOn = new int[n];
        this.numProcessors = numProcessors;
        processorAvailability = new int[numProcessors];
        parentsList = taskGraph.getParentsList();
        childrenList = taskGraph.getChildrenList();
        durations = taskGraph.getDurations();
        startTimes = new int[n];
        scheduledOn = new int[n];
        Arrays.fill(scheduledOn, -1);
        commCosts = taskGraph.getCommCosts();
        scheduleCandidates = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            totalSum += durations[i];
            inDegrees[i] = parentsList[i].size();
            if (inDegrees[i] == 0) {
                scheduleCandidates.add(i);
            }
        }
    }
    public void run(){
        //Base Case
        if(scheduleCandidates.size() == 0){
            int max = Integer.MIN_VALUE;
            for(int i = 0; i<processorAvailability.length; i++){
                max = Math.max(processorAvailability[i], max);
            }
            if(max < bestTime){
                bestTime = max;
                for(int i = 0; i<bestStartTime.length; i++){
                    bestScheduledOn[i] = scheduledOn[i];
                    bestStartTime[i] = startTimes[i];
                }
            }
            return;
        }
        int minRemainingTime = (int)Math.ceil(totalSum /(double) numProcessors);
        int fastestProcessorFinish = Integer.MAX_VALUE;
        for(int l = 0; l<processorAvailability.length; l++){
            fastestProcessorFinish = Math.min(processorAvailability[l], fastestProcessorFinish);
        }
        //Pruning.
        if(fastestProcessorFinish + minRemainingTime < bestTime) {
            //Recursive part
            for (int i = 0; i < scheduleCandidates.size(); i++) {
                int task = scheduleCandidates.remove(); //Want to try schedule this task
                totalSum -= durations[task];
                List<Integer> children = childrenList[task];
                //Reducing and possibly adding children to the candidates list.
                for (int j = 0; j < children.size(); j++) {
                    inDegrees[children.get(j)]--;
                    if (inDegrees[children.get(j)] == 0) {
                        scheduleCandidates.add(children.get(j));
                    }
                }
                //Going to try schedule task on all processors.
                boolean scheduledOnZero = false;
                for (int j = 0; j < processorAvailability.length; j++) {
                    if(processorAvailability[j] == 0){
                        if(scheduledOnZero){
                            continue;
                        } else {
                            scheduledOnZero = true;
                        }
                    }
                    List<Integer> parents = parentsList[task];
                    int maxTransferTime = Integer.MIN_VALUE;
                    for (int k = 0; k < parents.size(); k++) {
                        int parent = parents.get(k);
                        if (scheduledOn[parent] != j) {
                            maxTransferTime = Math.max(maxTransferTime, startTimes[parent] + durations[parent] + commCosts[parent][task]);
                        }
                    }
                    int earliestStartTime = Math.max(maxTransferTime, processorAvailability[j]);

                    int pJEndTime = processorAvailability[j];
                    processorAvailability[j] = earliestStartTime + durations[task];
                    scheduledOn[task] = j;
                    startTimes[task] = earliestStartTime;
                    count++;
                    run();
                    count--;
                    //Set the processor back to its origional time.
                    processorAvailability[j] = pJEndTime;
                }
                //BackTracking part.
                totalSum += durations[task];
                scheduledOn[task] = -1;
                for (int j = 0; j < children.size(); j++) {
                    inDegrees[children.get(j)]++;
                    if (inDegrees[children.get(j)] == 1) {
                        scheduleCandidates.removeLast();
                    }
                }
                scheduleCandidates.add(task); //Note how I add it back, cause now its unscheduled.
            }
        } else {
            //System.out.println(count);
            return;
        }
    }
    public Task[] getOptimalSchedule(){
        Task[] schedule = new Task[bestStartTime.length];
        for(int i = 0; i<schedule.length; i++){
            Task t = new Task(i, bestStartTime[i], bestStartTime[i]+durations[i], bestScheduledOn[i]);
            schedule[i] = t;
        }
        return schedule;
    }

}
