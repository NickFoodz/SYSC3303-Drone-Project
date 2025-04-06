import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GUI extends JFrame {
    private MapPanel mapPanel;
    private JButton addPointButton, clearButton;
    private JLabel statusLabel, faultLabel;
    private JTextArea faultTextArea;
    private JPanel droneStatusPanel;

    private static Integer MAX_WIDTH = 2000;
    private static Integer MAX_HEIGHT = 1500;

    private ConcurrentHashMap<String, DroneSubsystem.Drone.droneState> droneStates = new ConcurrentHashMap<>();


    public GUI() {
        setVisible(true);
        //update max width and height if needed


        // Set up the frame
        setTitle("Drone Simulation");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the map panel
        mapPanel = new MapPanel();
        mapPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(mapPanel, BorderLayout.CENTER);

        //panel on the right
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        //TOP LABEL
        gbc.gridy = 0;
        statusLabel = new JLabel("Drones ready to deploy");
        controlPanel.add(statusLabel, gbc);

        //SPACER
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        droneStatusPanel = new JPanel();
        droneStatusPanel.setLayout(new BoxLayout(droneStatusPanel, BoxLayout.Y_AXIS));
        JScrollPane droneScrollPane = new JScrollPane(droneStatusPanel);
        droneScrollPane.setPreferredSize(new Dimension(200, 300));
        controlPanel.add(droneScrollPane, gbc);

        //FAULT LABEL
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weighty = 0;
        faultLabel = new JLabel("Fault Log:");
        controlPanel.add(faultLabel, gbc);

        //FAULT TEXT BOX
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        faultTextArea = new JTextArea(6, 20);
        faultTextArea.setEditable(false);
        faultTextArea.setLineWrap(true);
        faultTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(faultTextArea);
        gbc.weighty = 0.3;
        controlPanel.add(scrollPane, gbc);

        mainPanel.add(controlPanel, BorderLayout.EAST);
        add(mainPanel);

        faultTextArea.append("No Faults Detected\n");
    }

    public void displayFault(String fault){
        faultTextArea.append(fault + "\n");
    }
    public void displayFaultReset(){
        faultTextArea.append("Fault Handled\n");
    }

    private void refreshDroneStatusDisplay() {
        droneStatusPanel.removeAll(); // Clear existing entries

        for (String id : mapPanel.droneLocations.keySet()) {
            Color c = mapPanel.droneColors.get(id);
            DroneSubsystem.Drone.droneState state = droneStates.getOrDefault(id, DroneSubsystem.Drone.droneState.IDLE);
            String status = state.toString();

            JPanel droneRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JLabel colorBox = new JLabel("  ");
            colorBox.setOpaque(true);
            colorBox.setBackground(c);
            colorBox.setPreferredSize(new Dimension(15, 15));

            JLabel info = new JLabel(id + ": " + status);

            droneRow.add(colorBox);
            droneRow.add(Box.createHorizontalStrut(5));
            droneRow.add(info);

            droneStatusPanel.add(droneRow);
        }

        droneStatusPanel.revalidate();
        droneStatusPanel.repaint();
    }

    public void updateDimensions(List<Zone> zoneList) {
        for(Zone z : zoneList){
            if(z.getEndX() > MAX_WIDTH) {MAX_WIDTH = z.getEndX()+5;}
            if(z.getEndY() > MAX_HEIGHT) {MAX_HEIGHT = z.getEndY()+5;}
        }
    }

    public void addCoords(List<Zone> zoneList) {
        for(Zone z : zoneList){
            mapPanel.addZone(z.getStartX(), z.getStartY(), z.getEndX(), z.getEndY());
        }
        mapPanel.repaint();
    }

    public void updateDrone(String id, int x, int y, DroneSubsystem.Drone.droneState d) {
        mapPanel.updateDroneLocation(id, x, y);
        droneStates.put(id, d);
        refreshDroneStatusDisplay();
    }

    public void updateDrone(String id, DroneSubsystem.Drone.droneState d){
        droneStates.put(id, d);
        refreshDroneStatusDisplay();
    }

    // Custom panel for drawing the map
    private class MapPanel extends JPanel {
        private List<List<Integer>> zones = new ArrayList<>();
        private ConcurrentHashMap<String, List<Integer>> droneLocations = new ConcurrentHashMap<>();
        private ConcurrentHashMap<String, Color> droneColors = new ConcurrentHashMap<>();
        public MapPanel() {
            setBackground(Color.WHITE);
        }

        private Color getRandomColor() {
            float r = (float)Math.random();
            float g = (float)Math.random();
            float b = (float)Math.random();
            return new Color(r, g, b);
        }

        public void addZone(int x1, int y1, int x2, int y2) {
            int width = getWidth();
            int height = getHeight();

            x1 = (int)((double) x1 / MAX_WIDTH * width);
            y1 = (int)((double) y1 / MAX_HEIGHT * height);
            x2 = (int)((double) x2 / MAX_WIDTH * width);
            y2 = (int)((double) y2 / MAX_HEIGHT * height);
            List<Integer> currZone = new ArrayList<>();
            currZone.add(x1);
            currZone.add(y1);
            currZone.add(x2);
            currZone.add(y2);
            zones.add(currZone);

        }

        private synchronized void updateDroneLocation(String id, int x, int y) {
            x = (int)((double) x / MAX_WIDTH * getWidth());
            y = (int)((double) y / MAX_HEIGHT * getHeight());
            List<Integer> newCoords = Arrays.asList(x, y);

            // check if there is a value already
            List<Integer> check = droneLocations.putIfAbsent(id, newCoords);
            if (check != null) {
                droneLocations.replace(id, newCoords);
            }
            droneColors.computeIfAbsent(id, k -> getRandomColor());
            System.out.printf("print new location of %s: (%d, %d)\n", id, x, y);

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable anti-aliasing for smoother drawing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(new Color(220, 220, 220));

            // Draw all the lines
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(2));
            for (List<Integer> zone : zones) {
                int x1 = zone.get(0);
                int y1 = zone.get(1);
                int x2 = zone.get(2);
                int y2 = zone.get(3);
                g2d.drawLine(x1, y1, x2, y1);
                g2d.drawLine(x2, y1, x2, y2);
                g2d.drawLine(x2, y2, x1, y2);
                g2d.drawLine(x1, y2, x1, y1);
            }

            // for drones use g2d.fillOval(x, y, width, height)
            for (String id : droneLocations.keySet()) {
                List<Integer> coord = droneLocations.get(id);
                int x = coord.get(0);
                int y = coord.get(1);
                g2d.setColor(droneColors.getOrDefault(id, Color.BLUE));
                g2d.fillOval(x - 8, y - 8, 16, 16); // Draw drone
            }
        }
    }
}