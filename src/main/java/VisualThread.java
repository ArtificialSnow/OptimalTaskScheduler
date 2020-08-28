public class VisualThread extends Thread {
    private Solution solution;
    private TaskGraph taskGraph;
    private int numProcessors;
    private int upperBoundTime;

    public VisualThread(Solution solution, TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        super();
        this.solution = solution;
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;
        this.upperBoundTime = upperBoundTime;
    }

    public void run() {
        solution.run(taskGraph, numProcessors, upperBoundTime);
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
