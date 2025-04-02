package com.planeapp.gui;

import com.planeapp.data.DataManager;
// Model imports might not be needed directly here anymore
// import com.planeapp.model.Pilot;
// import com.planeapp.model.Plane;

import javax.swing.*;
// import javax.swing.table.DefaultTableModel; // Moved to panels
import java.awt.*;
// Other imports like List, Optional, event listeners also moved to panels

public class PlaneAppGUI extends JFrame {

    private final DataManager dataManager;

    private JTabbedPane tabbedPane;
    // Panel instances
    private PlanePanel planePanel;
    private PilotPanel pilotPanel;
    private AssignmentPanel assignmentPanel;

    // Remove fields for components now inside panels
    // private JTable planeTable; ... etc.
    // private JComboBox<PlaneWrapper> assignPlaneCombo; ... etc.

    public PlaneAppGUI() {
        this.dataManager = new DataManager();

        setTitle("Plane & Pilot Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        // Initial refresh call for all panels
        refreshAllPanels();

        setVisible(true);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Instantiate panels, passing DataManager and this main App reference
        planePanel = new PlanePanel(dataManager, this);
        pilotPanel = new PilotPanel(dataManager, this);
        assignmentPanel = new AssignmentPanel(dataManager, this);

        // Add panel instances as tabs
        tabbedPane.addTab("Planes", planePanel);
        tabbedPane.addTab("Pilots", pilotPanel);
        tabbedPane.addTab("Assignments", assignmentPanel);

        // Listener to refresh assignment tab specifically when selected (optional, but good UX)
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == assignmentPanel) {
                assignmentPanel.refreshData(); // Call assignment panel's specific refresh
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    // Central method to refresh data in all relevant panels
    public void refreshAllPanels() {
        // Use SwingUtilities.invokeLater if calling from non-EDT thread, but likely okay here
        if (planePanel != null) {
            planePanel.refreshTable();
        }
        if (pilotPanel != null) {
            pilotPanel.refreshTable();
        }
        if (assignmentPanel != null) {
            // Avoid refreshing assignment panel if it's the currently selected one,
            // as the change listener might handle it, preventing double refresh.
            if (tabbedPane.getSelectedComponent() != assignmentPanel) {
                 assignmentPanel.refreshData();
            }
        }
    }

    // Remove panel creation methods (createPlanePanel, createPilotPanel, createAssignmentPanel)
    // Remove refresh methods for specific tables/combos (refreshPlaneTable, refreshPilotTable, etc.)
    // Remove action methods (addPlaneAction, deletePilotAction, assignPilotAction, etc.)
    // Remove wrapper classes (PlaneWrapper, PilotWrapper)

    // --- Main Method ---
    public static void main(String[] args) {
        // Set System Look and Feel for a more native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set System Look and Feel. Using default.");
            // e.printStackTrace(); // Optional: print stack trace for debugging
        }

        // Ensure the GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new PlaneAppGUI());
    }
} 