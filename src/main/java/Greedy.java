import java.util.*;

public class Greedy {

    private Map<Integer,Integer> taskBottomLevelMap = new HashMap<>();

    /**
     * Main method of the algorithm which schedules tasks on parallel processors
     * n is the number of tasks.
     * @param taskGraph object that encapsulates tasks and their dependencies.
     * @param numProcessors Number of processors.
     * @return List of scheduled tasks.
     */
    public Schedule run(TaskGraph taskGraph, int numProcessors) {
        int n = taskGraph.getNumberOfTasks();
        int finalFinishTime = 0;

        Task[] output = new Task[n];
        int[][] earliestScheduleTimes = new int[n][numProcessors]; // i,j indicates earliest time to schedule task i on processor j

        int[] inDegrees = new int[n];



        // get all tasks that have no dependencies
        Queue<Integer> candidateTasks = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                candidateTasks.add(i);
            }
        }

        // find bottom level for each task.
        for (int task: candidateTasks) {
            // the minimum time each leaf would take is simply
            // the duration it takes to run.
            taskBottomLevelMap.put(task, taskGraph.getDuration(task));
            // recursively find the bottom level of all nodes from
            // the leaves.
            findBottomLevel(taskGraph, task, taskGraph.getDuration(task));
        }

        Queue<Integer> sortedCandidateTasks = sortByBottomLevel(candidateTasks);

        while (!sortedCandidateTasks.isEmpty()) {
            // find a node with in degree 0
            int candidateTask = sortedCandidateTasks.poll();

            // Choose processor to schedule task on
            int minStartTime = earliestScheduleTimes[candidateTask][0];
            int minProcessor = 0;
            for (int i = 1; i < numProcessors; i++) {
                int currStartTime = earliestScheduleTimes[candidateTask][i];
                if (currStartTime < minStartTime) {
                    minStartTime = currStartTime;
                    minProcessor = i;
                }
            }

            // Schedule task
            int finishTime = minStartTime + taskGraph.getDuration(candidateTask);
            finalFinishTime = Math.max(finalFinishTime, finishTime);

            output[candidateTask] = new Task(candidateTask, minStartTime, finishTime, minProcessor);

            // Update earliest schedule times for children
            for (int child: taskGraph.getChildrenList(candidateTask)) {
                for (int i = 0; i < numProcessors; i++) {
                    if (i == minProcessor) {
                        // for the processor the candidate was applied to,
                        // the earliest schedule time could be right after the candidate finishes
                        earliestScheduleTimes[child][minProcessor] = Math.max(finishTime,
                                earliestScheduleTimes[child][minProcessor]);
                    } else {
                        earliestScheduleTimes[child][i] = Math.max(finishTime + taskGraph.getCommCost(candidateTask, child),
                                earliestScheduleTimes[child][i]);
                    }
                }

                // Decrement in-degree count of child and see if it can be a candidate
                inDegrees[child]--;
                if (inDegrees[child] == 0) {
                    sortedCandidateTasks.add(child);
                }
            }
            // Update earliest schedule times for the processor which the task was scheduled on (minProcessor)
            for (int i = 0; i < n; i++) {
                earliestScheduleTimes[i][minProcessor] = Math.max(finishTime, earliestScheduleTimes[i][minProcessor]);
            }
        }

        return new Schedule(output, finalFinishTime);
    }

    /**
     * recursively find the bottom level of all nodes.
     * @param taskGraph
     * @param parentTask
     * @param currentBottomLevel
     */
    private void findBottomLevel(TaskGraph taskGraph, int parentTask, int currentBottomLevel) {
        for (int childTask: taskGraph.getChildrenList(parentTask)) {
            int newBottomLevel = currentBottomLevel + taskGraph.getDuration(childTask);
            if (!taskBottomLevelMap.containsKey(childTask)) {
                taskBottomLevelMap.put(childTask, newBottomLevel);
            } else if (taskBottomLevelMap.get(childTask) < newBottomLevel) {
                taskBottomLevelMap.replace(childTask, newBottomLevel);
            }

            // if the task has children, recursively find its children's bottom levels.
            findBottomLevel(taskGraph, childTask, newBottomLevel);
        }
    }

    private Queue<Integer> sortByBottomLevel(Queue<Integer> candidateTasks) {
        Queue<Integer> sortedCandidateTasks = new PriorityQueue<>(Comparator.comparingInt(taskBottomLevelMap::get).reversed());
        for (int task: candidateTasks) {
            sortedCandidateTasks.add(task);
        }

        return sortedCandidateTasks;
    }

}
