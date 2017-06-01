import javax.swing.*;
import java.util.Random;

public class KMeansGUI {

    private class DoubleEKmeansExt extends DoubleKmeans {

        public DoubleEKmeansExt(double[][] centroids, double[][] points, boolean equal, DoubleDistanceFunction doubleDistanceFunction, Listener listener) {
            super(centroids, points, equal, doubleDistanceFunction, listener);
        }

        public int[] getAssignments() {
            return assignments;
        }

        public int[] getCounts() {
            return counts;
        }
    }

    private static final int MIN = 0;
    private static final int MAX = 1;
    private static final int LEN = 2;

    private static final int X = 0;
    private static final int Y = 1;

    private static final int RESOLUTION = 300;
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private JToolBar toolBar;
    private JTextField nTextField;
    private JTextField kTextField;
    private JCheckBox equalCheckBox;
    private JTextField debugTextField;
    private JPanel canvaPanel;
    private JLabel statusBar;
    private double[][] centroids = null;
    private double[][] points = null;
    private double[][] minmaxlens = null;
    private DoubleEKmeansExt eKmeans = null;
    private String[] lines = null;
}