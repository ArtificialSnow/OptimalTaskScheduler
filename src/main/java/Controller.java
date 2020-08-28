import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.*;

/**
 * Controller class for the visualisation (GUI). It connects to the fxml file.
 * Used to manage and modify information presented to the user in the GUI.
 */
public class Controller {

    // These labels are updated with information received by an object of the class in the GUI
    @FXML
    private Label inputGraphLabel;  // shows the name of the input graph
    @FXML
    private Label totalTasksLabel;  // shows the number of tasks (nodes) in the input graph
    @FXML
    private Label threadCountLabel; // shows the number of threads used to compute the solution
    @FXML
    private Label currentBestLabel; // shows the finish time of the shortest computed schedule so far
    @FXML
    private Label stateCountLabel;  // shows the number of states searched so far
    @FXML
    private Label timerLabel;   // shows the elapsed time so far
    @FXML
    private Label statusLabel;  // shows whether the program is running or has finished

    @FXML
    private Button startButton;

    // stackedBarChart has bars equal to the number of processors, and each bar is used to
    // visualise tasks being added to the processor in the GUI
    @FXML
    private StackedBarChart<String, Number> stackedBarChart;
    private CategoryAxis xAxis;
    private int[] processorFinishTimes;

    private int numProcessors;

    private VisualThread visualThread;
    private Timer poller;
    private Timer timer;

    /**
     * This method initialises the stackedBarChart and timer for the GUI.
     */
    @FXML
    private void initialize() {
        xAxis = (CategoryAxis) stackedBarChart.getXAxis();
        stackedBarChart.setLegendVisible(false);
        stackedBarChart.setAnimated(false);
        timerLabel.setText("00:00:00:00");
    }

    /**
     * This method sets up initial values for labels in the GUI. It also sets up data structures used when adding
     * and removing tasks from the stackedBarChart.
     * @param visualThread thread on which the algorithm runs
     * @param numProcessors number of processors
     * @param inputGraphName name of the input file
     * @param numTasks number of tasks (nodes) in the input file
     * @param numThreads number of threads the solution is being run on
     */
    public void setUpArgs(VisualThread visualThread, int numProcessors, String inputGraphName, int numTasks, int numThreads) {
        this.visualThread = visualThread;
        inputGraphLabel.setText(inputGraphName);
        totalTasksLabel.setText(numTasks + "");
        threadCountLabel.setText(numThreads + "");
        this.numProcessors = numProcessors;

        // Add each processor to the xAxis for the stackedBarChart
        List<String> xAxisProcessors = new ArrayList<>();
        for (int i = 0; i < numProcessors; i++) {
            xAxisProcessors.add("Processor " + (i + 1));
        }
        xAxis.setCategories(FXCollections.observableArrayList(xAxisProcessors));
        processorFinishTimes = new int[numProcessors];
    }

    @FXML
    private void start() {
        visualThread.start();
        startButton.setDisable(true);
        poller = new Timer();
        poller.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long stateCount = visualThread.getStateCount();
                boolean isDone = visualThread.isDone();

                Platform.runLater(() -> {
                    stateCountLabel.setText(stateCount/1000 + "k");
                    if (visualThread.getBestChanged()) {
                        currentBestLabel.setText(visualThread.getCurrentBest() + "");
                        updateStackedBarChart(visualThread.getBestSchedule());
                        visualThread.setBestChanged(false);
                    }
                    if (isDone) {
                        stop();
                    }
                });
            }
        }, 0, 50);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            // Get the current time, from which elapsed time will be calculated
            final long startTime = System.currentTimeMillis();

            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                // Calculate the milliseconds, seconds, and minutes passed since the start of the program.
                int milliseconds = (int) (elapsedMillis % 100);
                int seconds = (int) ((elapsedMillis / 1000) % 60);
                int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
                int hours = (int) (elapsedMillis / (1000 * 60 * 60));
                Platform.runLater(() -> {
                    timerLabel.setText(String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds));
                });
            }
        }, 0, 50);

    }

    private void stop() {
        poller.cancel();
        timer.cancel();
    }

    /**
     * This method add tasks to specific processors in the startBarChart on the GUI. When
     * there is a gap between the start time of the task and the current finish time of the
     * processor, this method creates idle time to be added to the stackedBarChart, and
     * makes it invisible, as the bar chart cannot have gaps.
     */
    public void updateStackedBarChart(List<Task>[] bestSchedule) {
        stackedBarChart.getData().clear();
        for (int processor = 0; processor < numProcessors; processor++) {
            processorFinishTimes[processor] = 0;
            for (Task task : bestSchedule[processor]) {
                addTask(task, processor);
            }
        }
    }

    private void addTask(Task task, int processor) {
        // calculate the time difference between the task start time and processor finish time
        // update the finish time of the processor to match the finish time of the task
        int idleTime = task.startTime - processorFinishTimes[processor];
        processorFinishTimes[processor] = task.startTime + task.duration;

        // create data for the idle time to the correct processor on the stackedBarChart
        XYChart.Series<String, Number> idleSeries = new XYChart.Series<>();
        idleSeries.getData().add(new XYChart.Data<>("Processor " + (numProcessors - processor), idleTime));

        // create data for the task itself to the correct processor on the stackedBarChart
        XYChart.Series<String, Number> taskSeries = new XYChart.Series<>();
        taskSeries.getData().add(new XYChart.Data<>("Processor " + (numProcessors - processor), task.duration));

        //  add the idle and task data to the stackedBarChart
        stackedBarChart.getData().add(idleSeries);
        stackedBarChart.getData().add(taskSeries);
    }

    /**
     * This method stops the timer and changes the status of the visualisation
     * from RUNNING to FINISHED.
     */
    public void setStatusFinished() {
        statusLabel.setText("FINISHED");
        statusLabel.setStyle("-fx-text-fill: forestgreen");
    }
}
