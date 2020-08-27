# Milestone 2

## Optimization

### Pre-Processing
* #### Node Duplication
* #### Load Balancing
* #### Initial Greedy Schedule

### Recursive Search and Backtracking

#### Order

### Pruning

* #### Equivalent Schedule
* #### Fixed Task Order (FTO)
   Suppose we are in the process of scheduling our tasks. Let us call the list of tasks where there are either no dependencies, or their dependencies have been completed, our list of candidateTasks. These are the tasks that can currently be scheduled.
   
   There are special structures that can be present in a task graph such that we can fix the order of our tasks.
   
   These structures must fulfill several conditions:
   1. All the candidateTasks must have at most one parent and at most one child.
   2. All the candidateTasks must have either no child or the same child.
   3. All the candidateTasks must have either no parent, or their parents are scheduled on the same processor.
   4. The candidateTasks list can be sorted such that the list fulfills the following two conditions. This will be the fixed task order.
        1. The tasks in the list are in non-decreasing data ready time. Data ready time = finish time of parent + communication cost of parent to the task.
        2. The tasks in the list are in non-increasing out-edge costs. Out-edge cost = communication cost of task to its child, or 0 if the task does not have a child.
     
   The fixed task order means that among the tasks in candidateTasks, an optimal solution should contain these tasks scheduled in this order. By fixing the task order, we are able to prune our tree by a factor of the number of tasks in candidateTasks, as we no longer need to check every single ordering. 
   
   The fixed task order works because scheduling tasks in non-decreasing data ready time ensures the minimalisation of idle time of processors, and the scheduling of tasks in non-increasing out-edge costs ensures that the start time of any tasks that depend on our set of candidateTasks (which should be all tasks that are not currently scheduled and not in candidateTasks by definition) can be minimalised.
   
   **[insert proof here maybe?]**
   
   Once we get a FTO, we know that we can schedule the first task in our FTO safely. However, once the first task is scheduled, this may make changes to our list of candidate tasks. More specifically, if the task has a child, and the child becomes a candidate task, our candidateTasks may no longer form a valid FTO. For example, if the newly added child has two different children, our candidateTasks list would no longer satisfy condition i) for a FTO. If the scheduled task doesn't have a child however, our candidateTasks without the first scheduled task will still form a FTO because non of the conditions i), ii), iii) will be violated by the current tasks in candidateTasks, and the list is already in the order specified by iv).
   
* #### Load Balancing
* #### Critical Path
* #### Latest Finish Time

### Edge Cases
We check for edge cases in our algorithm to ensure that we can sort these edge cases in a faster way than other graphs.
#### Sequential
If there is only one processor, then all tasks should simply be scheduled sequentially on the processor with no idle time. The finish time of the optimal schedule is the sum of all the durations of the tasks. All we need to do is to find a valid order to schedule the tasks. 

## Acknowledgements