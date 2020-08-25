import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Solution {
    private TaskGraph taskGraph;
    private int numProcessors;
    private int numTasks;

    private int[] maxLengthToExitNode;
    private int[] inDegrees; // inDegrees[i] => number of unscheduled parent tasks of task i
    private int[] startTimes; // startTimes[i] => start time of task i
    private int[] scheduledOn;  // scheduledOn[i] => the processor task i is scheduled on
    private int[] processorFinishTimes; // processorAvailability[i] => finishing time of the last task scheduled on processor i
    private int remainingDuration = 0; // total duration of remaining tasks to be scheduled (used for pruning)

    private int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    private int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    private int bestTime; // earliest finishing time of schedules we have searched

    LinkedList<Integer> candidateTasks; // queue of tasks with no unprocessed dependencies

    /**
     * Creates an optimal scheduling of tasks on specified number of processors.
     * @param taskGraph Graph containing tasks as nodes and their dependencies as edges.
     * @param numProcessors Number of processors to schedule the tasks on.
     * @param upperBoundTime Upper bound of running time that the optimal solution should do at least as good as.
     * @return optimal schedule found by the run method.
     */
    public Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        initialize(taskGraph, numProcessors, upperBoundTime);
        recursiveSearch();

        return createOutput();
    }

    /**
     * Recursively try to schedule a task on a processor.
     * Uses DFS to try all possible schedules.
     */
    private void recursiveSearch() {
        // base case is when queue is empty, i.e. all tasks scheduled.
        if (candidateTasks.size() == 0) {
            int finishTime = findMaxInArray(processorFinishTimes);

            // check if this schedule has the best time
            if (finishTime < bestTime) {
                bestTime = finishTime;
                // update the best schedule if smaller schedule found
                for (int i = 0; i < bestStartTime.length; i++) {
                    bestScheduledOn[i] = scheduledOn[i];
                    bestStartTime[i] = startTimes[i];
                }
            }
            return;
        }

        // minimal remaining time IF all remaining tasks are evenly distributed amongst processors.
        int loadBalancedRemainingTime = (int)Math.ceil(remainingDuration/(double)numProcessors);

        // find the processor which finishes earliest in current schedule
        int earliestProcessorFinishTime = Integer.MAX_VALUE;
        int latestProcessorFinishTime = 0;
        for (int l = 0; l < numProcessors; l++) {
            earliestProcessorFinishTime = Math.min(processorFinishTimes[l], earliestProcessorFinishTime);
            latestProcessorFinishTime = Math.max(processorFinishTimes[l], latestProcessorFinishTime);
        }

        // recursively try different schedules
        for (int i = 0; i < candidateTasks.size(); i++) {
            int candidateTask = candidateTasks.remove(); // get task to try schedule

            // only continue processing this state if it is possible to do better than the current best time we have found
            boolean loadBalancingCond = earliestProcessorFinishTime + loadBalancedRemainingTime >= bestTime;
            boolean criticalPathCond = earliestProcessorFinishTime + maxLengthToExitNode[candidateTask] >= bestTime;
            boolean finishTimeCond = latestProcessorFinishTime >= bestTime;
            if(loadBalancingCond || criticalPathCond || finishTimeCond) {
                // we can't do better, backtrack.
                candidateTasks.add(candidateTask);
                continue;
            }

            remainingDuration -= taskGraph.getDuration(candidateTask); // remaining tasks to schedule does not include candidate task
            List<Integer> candidateChildren = taskGraph.getChildrenList(candidateTask); // get children of candidate task

            // update in degrees of children of candidate
            for (Integer candidateChild : candidateChildren) {
                inDegrees[candidateChild]--;
                // add child to queue if it has no more parents to process
                if (inDegrees[candidateChild] == 0) {
                    candidateTasks.add(candidateChild);
                }
            }

            // whether this task has been scheduled at time 0 on a processor before
            boolean hasBeenScheduledAtStart = false;

            // schedule candidate task on each processor
            for (int candidateProcessor = 0; candidateProcessor < numProcessors; candidateProcessor++) {
                if (processorFinishTimes[candidateProcessor] == 0) {
                    if (hasBeenScheduledAtStart) {
                        // state already checked, skip.
                        continue;
                    } else {
                        hasBeenScheduledAtStart = true;
                    }
                }

                List<Integer> parents = taskGraph.getParentsList(candidateTask);

                // find the earliest time the candidate task can be scheduled on the candidate processor
                int earliestStartTimeOnCurrentProcessor = Integer.MIN_VALUE;
                for (int parent : parents) {
                    // check constraints due to comm costs of parents on other processors
                    if (scheduledOn[parent] != candidateProcessor) {
                        earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, startTimes[parent] +
                                taskGraph.getDuration(parent) + taskGraph.getCommCost(parent, candidateTask));
                    }
                }

                // check constraint of latest finishing time of task on candidate processor
                earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, processorFinishTimes[candidateProcessor]);

                // only continue processing this state if it is possible to do better than the current best time we have found
                criticalPathCond = earliestStartTimeOnCurrentProcessor + maxLengthToExitNode[candidateTask] >= bestTime;
                if(criticalPathCond) {
                    // can't do better, skip.
                    continue;
                }

                // update state
                int prevFinishTime = processorFinishTimes[candidateProcessor];
                processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(candidateTask);
                scheduledOn[candidateTask] = candidateProcessor;
                startTimes[candidateTask] = earliestStartTimeOnCurrentProcessor;

                recursiveSearch();

                // backtrack so reset processor finish time.
                processorFinishTimes[candidateProcessor] = prevFinishTime;
            }

            // backtracking so revert totalSum to include candidate task
            remainingDuration += taskGraph.getDuration(candidateTask);
            for (Integer candidateChild : candidateChildren) {
                // revert changes made to children
                inDegrees[candidateChild]++;
                if (inDegrees[candidateChild] == 1) {
                    candidateTasks.removeLast();
                }
            }
            // add candidate task back to queue since it is now unscheduled
            candidateTasks.add(candidateTask);
        }
    }

    /**
     * Helper method to initialize all the fields required for the solution.
     */
    private void initialize(TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;

        maxLengthToExitNode = PreProcessor.maxLengthToExitNode(taskGraph);
        bestTime = upperBoundTime;
        numTasks = taskGraph.getNumberOfTasks();

        inDegrees = new int[numTasks];
        bestStartTime = new int[numTasks];
        bestScheduledOn = new int[numTasks];
        processorFinishTimes = new int[numProcessors];
        startTimes = new int[numTasks];
        scheduledOn = new int[numTasks];
        candidateTasks = new LinkedList<>();

        for (int i = 0; i < numTasks; i++) {
            // calculate remaining duration of tasks to be scheduled
            remainingDuration += taskGraph.getDuration(i);
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                candidateTasks.add(i);
            }
        }
    }

    /**
     * Helper method to create the output Schedule.
     * @return Optimal Schedule.
     */
    private Schedule createOutput() {
        Task[] optimalSchedule = new Task[numTasks];
        for (int i = 0; i < numTasks; i++) {
            Task t = new Task(i, bestStartTime[i],
                    bestStartTime[i] + taskGraph.getDuration(i), bestScheduledOn[i]);
            optimalSchedule[i] = t;
        }

        return new Schedule(optimalSchedule, bestTime);
    }

    /**
     * Find the maximum value integer in the array. Returns Integer.MIN_VALUE if array is empty.
     * @return maximum value.
     */
    private int findMaxInArray(int[] arr) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }

        return max;
    }



}
