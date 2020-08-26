import java.util.List;

public class PreProcessor {

    public static int[] maxLengthToExitNode(TaskGraph taskGraph){
        int numberOfTasks = taskGraph.getNumberOfTasks();
        int[] lengths = new int[numberOfTasks];

        for(int node = 0; node < numberOfTasks; node++){
            bLevels(node, lengths, taskGraph);
        }

        return lengths;
    }

    // Finds distances from exit node recursively with memoization
    private static int bLevels(int node, int[] lengths, TaskGraph taskGraph){
        if(lengths[node] != 0){
            return lengths[node];
        }

        List<Integer> childrenList  = taskGraph.getChildrenList(node);
        if(childrenList.isEmpty()){
            lengths[node] = taskGraph.getDuration(node);
            return lengths[node];
        }

        int maxLength = 0;
        for(int child : childrenList){
            maxLength = Math.max(maxLength, bLevels(child, lengths, taskGraph));
        }

        lengths[node] = maxLength + taskGraph.getDuration(node);
        return lengths[node];
    }
}
