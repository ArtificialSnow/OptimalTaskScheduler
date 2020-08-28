import java.io.IOException;

public interface Solution {

    Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime) throws IOException, ClassNotFoundException;
}
