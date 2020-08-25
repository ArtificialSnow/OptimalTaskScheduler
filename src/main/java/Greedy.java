import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Greedy {

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
        // order the candidate tasks such that we will schedule the largest task first.
        PriorityQueue<CandidateTask> candidateTasks = new PriorityQueue<>();
        for (int i = 0; i < n; i++) {
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                candidateTasks.add(new CandidateTask(taskGraph, i));
            }
        }

        while (!candidateTasks.isEmpty()) {
            // find a node with in degree 0
            CandidateTask candidateTask = candidateTasks.poll();

            // Choose processor to schedule task on
            int minStartTime = earliestScheduleTimes[candidateTask.getTaskId()][0];
            int minProcessor = 0;
            for (int i = 1; i < numProcessors; i++) {
                int currStartTime = earliestScheduleTimes[candidateTask.getTaskId()][i];
                if (currStartTime < minStartTime) {
                    minStartTime = currStartTime;
                    minProcessor = i;
                }
            }

            // Schedule task
            int finishTime = minStartTime + candidateTask.getDuration();
            finalFinishTime = Math.max(finalFinishTime, finishTime);

            output[candidateTask.getTaskId()] = new Task(candidateTask.getTaskId(), minStartTime, finishTime, minProcessor);

            // Update earliest schedule times for children
            for (int child: candidateTask.getChildrenList()) {
                for (int i = 0; i < numProcessors; i++) {
                    if (i == minProcessor) {
                        // for the processor the candidate was applied to,
                        // the earliest schedule time could be right after the candidate finishes
                        earliestScheduleTimes[child][minProcessor] = Math.max(finishTime,
                                earliestScheduleTimes[child][minProcessor]);
                    } else {
                        earliestScheduleTimes[child][i] = Math.max(finishTime + candidateTask.getCommCost(child),
                                earliestScheduleTimes[child][i]);
                    }
                }

                // Decrement in-degree count of child and see if it can be a candidate
                inDegrees[child]--;
                if (inDegrees[child] == 0) {
                    candidateTasks.add(new CandidateTask(taskGraph, child));
                }
            }
            // Update earliest schedule times for the processor which the task was scheduled on (minProcessor)
            for (int i = 0; i < n; i++) {
                earliestScheduleTimes[i][minProcessor] = Math.max(finishTime, earliestScheduleTimes[i][minProcessor]);
            }
        }

        return new Schedule(output, finalFinishTime);
    }

    private class CandidateTask implements  Comparable{
        int taskId;
        TaskGraph taskGraph;


        public CandidateTask(TaskGraph taskGraph, int taskId) {
            this.taskId = taskId;
            this.taskGraph = taskGraph;
        }

        public int getTaskId() {
            return taskId;
        }

        public int getDuration() {
            return taskGraph.getDuration(taskId);
        }

        public List<Integer> getChildrenList() {
            return taskGraph.getChildrenList(taskId);
        }

        public int getCommCost(int child) {
            return taskGraph.getCommCost(taskId, child);
        }

        @Override
        public int compareTo(Object o) {
            CandidateTask otherTask = (CandidateTask)o;
            if (this.getDuration() > otherTask.getDuration()) {
                return -1;
            } else if (this.getDuration() == otherTask.getDuration()) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
