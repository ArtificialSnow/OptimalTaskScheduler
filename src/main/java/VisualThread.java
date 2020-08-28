import org.graphstream.graph.Graph;

public class VisualThread extends Thread {
    private Solution solution;
    private TaskGraph taskGraph;
    private int numProcessors;
    private String outputFilePath;
    private Graph dotGraph;

    public VisualThread(Solution solution, TaskGraph taskGraph, int numProcessors, String outputFilePath, Graph dotGraph) {
        super();
        this.solution = solution;
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;
        this.outputFilePath = outputFilePath;
        this.dotGraph = dotGraph;
    }

    public void run() {
        Schedule result;
        Schedule greedySchedule = null;

        // if the number of processors is one, then the optimal solution is just everything run
        // sequentially.
        if (numProcessors == 1) {
            SequentialScheduler scheduler = new SequentialScheduler(taskGraph);
            result = scheduler.getSchedule();
        } else {
            // Run greedy algorithm to determine lower bound of optimal solution
            Greedy g = new Greedy();
            greedySchedule = g.run(taskGraph, numProcessors);

            // Run algorithm to find optimal schedule
            long startTime = System.currentTimeMillis();
            result = solution.run(taskGraph, numProcessors, greedySchedule.getFinishTime());
            System.out.println(System.currentTimeMillis() - startTime);
        }

        // Our solution ignores all schedules that are >= than the greedy schedule,
        // so this is to ensure if nothing is faster, we return the greedy schedule.
        if (greedySchedule != null && result.getFinishTime() >= greedySchedule.getFinishTime()) {
            IOParser.write(outputFilePath, dotGraph, greedySchedule.getTasks());
        } else {
            IOParser.write(outputFilePath, dotGraph, result.getTasks());
        }
    }

    public synchronized int getCurrentBest() {
        return solution.currentBest;
    }

    public synchronized int getStateCount() {
        return solution.stateCount;
    }

    public synchronized boolean isDone() {
        return solution.isDone;
    }
}
