import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class PartialSchedule {

    int[][] _schedule;
    int numProcessors;
    //Scheduled on will need to be -1;

    public PartialSchedule(int[] startTimes, int[] scheduledOn, int numTasks, int numProcessors){
        _schedule = new int[numTasks][2];  //We only need to know the start time and the processor scheduled on
        this.numProcessors = numProcessors;
        for(int i = 0; i < startTimes.length; i++){
            if(startTimes[i] != -1){
                _schedule[i][0] = startTimes[i];
                _schedule[i][1] = scheduledOn[i];
            } else {
                _schedule[i][0] = -1;
                _schedule[i][1] = -1;
            }
        }
    }

    @Override
    public int hashCode() {
        // Initialize some stacks representing a processor and a set representing the schedule
        Set<Stack<Integer>> schedule = new HashSet<>();
        Stack<Integer>[] stacks = new Stack[numProcessors];

        // Initialize the stacks
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = new Stack<>();
        }
        // Add each processors tasks and start times to a stack
        for (int i = 0; i < _schedule.length; i++) {
            if (_schedule[i][0] != -1) {
                stacks[_schedule[i][1]].add(i);
                stacks[_schedule[i][1]].add(_schedule[i][0]);
            }
        }

        // Add the stacks to a set.
        for(Stack<Integer> stack : stacks) {
            schedule.add(stack);
        }

        return schedule.hashCode();

    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        PartialSchedule other = (PartialSchedule)obj;
        return other.hashCode() == hashCode();

    }

}