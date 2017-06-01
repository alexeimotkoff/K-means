import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class KmeansGUI {

    private class DoubleKmeansExt extends DoubleKmeans {

        public DoubleKmeansExt(double[][] centroids, double[][] points, boolean equal, DoubleDistanceFunction doubleDistanceFunction, Listener listener) {
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
    private DoubleKmeansExt eKmeans = null;
    private String[] lines = null;

    public KmeansGUI() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(RESOLUTION + 100, RESOLUTION + 100));
        frame.setPreferredSize(new Dimension(RESOLUTION * 2, RESOLUTION * 2));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        frame.setContentPane(contentPanel);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        contentPanel.add(toolBar, BorderLayout.NORTH);
        JLabel nLabel = new JLabel("n:");
        toolBar.add(nLabel);

        nTextField = new JTextField("1000");
        toolBar.add(nTextField);

        JButton randomButton = new JButton();
        randomButton.setAction(new AbstractAction(" Random ") {
            public void actionPerformed(ActionEvent ae) {
                random();
            }
        });
        toolBar.add(randomButton);

        JLabel kLabel = new JLabel("k:");
        toolBar.add(kLabel);

        kTextField = new JTextField("5");
        toolBar.add(kTextField);

        JLabel equalLabel = new JLabel("equal:");
        toolBar.add(equalLabel);

        equalCheckBox = new JCheckBox("");
        toolBar.add(equalCheckBox);

        JLabel debugLabel = new JLabel("debug:");
        toolBar.add(debugLabel);

        debugTextField = new JTextField("0");
        toolBar.add(debugTextField);

        JButton runButton = new JButton();
        runButton.setAction(new AbstractAction(" Start ") {
            public void actionPerformed(ActionEvent ae) {
                start();
            }
        });
        toolBar.add(runButton);

        canvaPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                KmeansGUI.this.paint(g, getWidth(), getHeight());
            }
        };
        contentPanel.add(canvaPanel, BorderLayout.CENTER);

        statusBar = new JLabel(" ");
        contentPanel.add(statusBar, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    private void enableToolBar(boolean enabled) {
        for (Component c : toolBar.getComponents()) {
            c.setEnabled(enabled);
        }
    }

    private void random() {
        enableToolBar(false);
        eKmeans = null;
        lines = null;
        int n = Integer.parseInt(nTextField.getText());
        points = new double[n][2];
        minmaxlens = new double[][]{
                {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
                {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY},
                {0d, 0d}
        };
        for (int i = 0; i < n; i++) {
            points[i][X] = RANDOM.nextDouble();
            points[i][Y] = RANDOM.nextDouble();
            if (points[i][X] < minmaxlens[MIN][X]) {
                minmaxlens[MIN][X] = points[i][X];
            }
            if (points[i][Y] < minmaxlens[MIN][Y]) {
                minmaxlens[MIN][Y] = points[i][Y];
            }
            if (points[i][X] > minmaxlens[MAX][X]) {
                minmaxlens[MAX][X] = points[i][X];
            }
            if (points[i][Y] > minmaxlens[MAX][Y]) {
                minmaxlens[MAX][Y] = points[i][Y];
            }
        }
        minmaxlens[LEN][X] = minmaxlens[MAX][X] - minmaxlens[MIN][X];
        minmaxlens[LEN][Y] = minmaxlens[MAX][Y] - minmaxlens[MIN][Y];
        canvaPanel.repaint();
        enableToolBar(true);
    }

    private void start() {
        if (points == null) {
            random();
        }
        new Thread(new Runnable() {
            public void run() {
                enableToolBar(false);
                try {
                    KmeansGUI.this.run();
                } finally {
                    enableToolBar(true);
                }
            }
        }).start();
    }

    private void run() {
        int k = Integer.parseInt(kTextField.getText());
        boolean equal = equalCheckBox.isSelected();
        int debugTmp = 0;
        try {
            debugTmp = Integer.parseInt(debugTextField.getText());
        } catch (NumberFormatException ignore) {
        }
        final int debug = debugTmp;
        centroids = new double[k][2];
        for (int i = 0; i < k; i++) {
            centroids[i][X] = minmaxlens[MIN][X] + (minmaxlens[LEN][X] / 2d);
            centroids[i][Y] = minmaxlens[MIN][Y] + (minmaxlens[LEN][Y] / 2d);
        }
        AbstractKmeans.Listener listener = null;
        if (debug > 0) {
            listener = new AbstractKmeans.Listener() {
                public void iteration(int iteration, int move) {
                    statusBar.setText(MessageFormat.format("iteration {0} move {1}", iteration, move));
                    canvaPanel.repaint();
                    try {
                        Thread.sleep(debug);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        eKmeans = new DoubleKmeansExt(centroids, points, equal, DoubleKmeans.EUCLIDEAN_DISTANCE_FUNCTION, listener);
        long time = System.currentTimeMillis();
        eKmeans.run();
        time = System.currentTimeMillis() - time;
        statusBar.setText(MessageFormat.format("EKmeans run in {0}ms", time));
        canvaPanel.repaint();
    }

    private void paint(Graphics g, int width, int height) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        if (minmaxlens == null) {
            return;
        }
        double widthRatio = (width - 6d) / minmaxlens[LEN][X];
        double heightRatio = (height - 6d) / minmaxlens[LEN][Y];
        if (points == null) {
            return;
        }
        g.setColor(Color.BLACK);
        for (int i = 0; i < points.length; i++) {
            int px = 3 + (int) (widthRatio * (points[i][X] - minmaxlens[MIN][X]));
            int py = 3 + (int) (heightRatio * (points[i][Y] - minmaxlens[MIN][Y]));
            g.drawRect(px - 2, py - 2, 4, 4);
        }
        if (eKmeans == null) {
            return;
        }
        int[] assignments = eKmeans.getAssignments();
        int[] counts = eKmeans.getCounts();
        int s = 225 / centroids.length;
        for (int i = 0; i < points.length; i++) {
            int assignment = assignments[i];
            if (assignment == -1) {
                continue;
            }
            int cx = 3 + (int) (widthRatio * (centroids[assignment][X] - minmaxlens[MIN][X]));
            int cy = 3 + (int) (heightRatio * (centroids[assignment][Y] - minmaxlens[MIN][Y]));
            int px = 3 + (int) (widthRatio * (points[i][X] - minmaxlens[MIN][X]));
            int py = 3 + (int) (heightRatio * (points[i][Y] - minmaxlens[MIN][Y]));
            int c = assignment * s;
            g.setColor(new Color(c, c, c));
            g.drawLine(cx, cy, px, py);
        }
        g.setColor(Color.GREEN);
        for (int i = 0; i < centroids.length; i++) {
            int cx = 3 + (int) (widthRatio * (centroids[i][X] - minmaxlens[MIN][X]));
            int cy = 3 + (int) (heightRatio * (centroids[i][Y] - minmaxlens[MIN][Y]));
            g.drawLine(cx, cy - 2, cx, cy + 2);
            g.drawLine(cx - 2, cy, cx + 2, cy);
            int count = counts[i];
            g.drawString(String.valueOf(count), cx, cy);
        }
    }

    public static void main(String[] args) {
        new KmeansGUI();
    }
}