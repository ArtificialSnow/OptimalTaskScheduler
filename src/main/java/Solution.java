import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Finds an optimal schedule of the input tasks.
 */
public interface Solution {

    /**
     * Starts the finding of the solution.
     * @param taskGraph the input graph.
     * @param numProcessors the number of processors to schedule on.
     * @param upperBoundTime a bound on the finish time of the optimal schedule. Used for pruning.
     * @return an optimal schedule.
     */
    Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime);
}
