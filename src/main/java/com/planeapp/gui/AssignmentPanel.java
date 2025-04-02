package com.planeapp.gui;

import com.planeapp.data.DataManager;
import com.planeapp.model.Pilot;
import com.planeapp.model.Plane;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class AssignmentPanel extends JPanel {

    private final DataManager dataManager;
    private final PlaneAppGUI mainApp; // Reference to main GUI

    private JComboBox<PlaneWrapper> assignPlaneCombo;
    private JComboBox<PilotWrapper> assignPilotCombo;
    private JLabel currentAssignmentLabel;
    private JButton assignButton;
    private JButton unassignButton;

    public AssignmentPanel(DataManager dataManager, PlaneAppGUI mainApp) {
        super(new GridBagLayout());
        this.dataManager = dataManager;
        this.mainApp = mainApp;
        initComponents();
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Plane Selection
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Select Plane:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        assignPlaneCombo = new JComboBox<>();
        add(assignPlaneCombo, gbc);

        // Current Assignment Label
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0;
        currentAssignmentLabel = new JLabel("Current Pilot: (Select a plane)");
        currentAssignmentLabel.setFont(currentAssignmentLabel.getFont().deriveFont(Font.ITALIC));
        add(currentAssignmentLabel, gbc);
        gbc.gridwidth = 1; // reset

        // Pilot Selection
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Assign Pilot:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        assignPilotCombo = new JComboBox<>();
        add(assignPilotCombo, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        assignButton = new JButton("Assign Selected Pilot");
        unassignButton = new JButton("Unassign Current Pilot");
        buttonPanel.add(assignButton);
        buttonPanel.add(unassignButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        add(buttonPanel, gbc);

        // --- Event Listeners ---
        assignPlaneCombo.addActionListener(e -> updateCurrentAssignmentLabel());
        assignButton.addActionListener(e -> assignPilotAction());
        unassignButton.addActionListener(e -> unassignPilotAction());
    }

    // --- Refresh and Populate Methods ---
    public void refreshData() {
        populatePlaneCombo();
        populatePilotCombo();
        // updateCurrentAssignmentLabel(); // Called by populatePlaneCombo's listener
    }

    private void populatePlaneCombo() {
        PlaneWrapper selectedPlaneWrapper = (PlaneWrapper) assignPlaneCombo.getSelectedItem();
        assignPlaneCombo.removeAllItems();
        List<Plane> planes = dataManager.getAllPlanes();
        PlaneWrapper toReselect = null;
        for (Plane plane : planes) {
            PlaneWrapper wrapper = new PlaneWrapper(plane);
            assignPlaneCombo.addItem(wrapper);
            if (selectedPlaneWrapper != null && wrapper.equals(selectedPlaneWrapper)) {
                toReselect = wrapper;
            }
        }
         if (toReselect != null) {
            assignPlaneCombo.setSelectedItem(toReselect);
        } else if (assignPlaneCombo.getItemCount() > 0) {
             assignPlaneCombo.setSelectedIndex(0); // Select first if previous selection gone or first time
        } else {
             updateCurrentAssignmentLabel(); // Ensure label updates if combo becomes empty
        }
        // Note: Selecting an item fires the ActionListener, which calls updateCurrentAssignmentLabel
    }

    private void populatePilotCombo() {
         PilotWrapper selectedPilotWrapper = (PilotWrapper) assignPilotCombo.getSelectedItem();
         assignPilotCombo.removeAllItems();
         List<Pilot> pilots = dataManager.getAllPilots();
         PilotWrapper toReselect = null;

         // Add a placeholder/instruction item
         PilotWrapper placeholder = new PilotWrapper(null); // Use null pilot for placeholder
         assignPilotCombo.addItem(placeholder);

         for (Pilot pilot : pilots) {
             PilotWrapper wrapper = new PilotWrapper(pilot);
             assignPilotCombo.addItem(wrapper);
              if (selectedPilotWrapper != null && wrapper.equals(selectedPilotWrapper)) {
                toReselect = wrapper;
             }
         }
         if (toReselect != null) {
            assignPilotCombo.setSelectedItem(toReselect);
         } else {
             assignPilotCombo.setSelectedItem(placeholder); // Default to placeholder
         }
    }

     private void updateCurrentAssignmentLabel() {
        PlaneWrapper selectedPlaneWrapper = (PlaneWrapper) assignPlaneCombo.getSelectedItem();
        if (selectedPlaneWrapper == null) {
            currentAssignmentLabel.setText("Current Pilot: (Select a plane)");
            unassignButton.setEnabled(false);
            return;
        }

        int planeId = selectedPlaneWrapper.getPlane().getId();
        Optional<Pilot> assignedPilotOpt = dataManager.getAssignedPilotForPlane(planeId);

        if (assignedPilotOpt.isPresent()) {
            Pilot pilot = assignedPilotOpt.get();
            currentAssignmentLabel.setText("Current Pilot: " + pilot.getName() + " (ID: " + pilot.getId() + ")");
            unassignButton.setEnabled(true);
        } else {
            currentAssignmentLabel.setText("Current Pilot: None");
            unassignButton.setEnabled(false);
        }
    }

    // --- Action Methods ---
    private void assignPilotAction() {
        PlaneWrapper selectedPlaneWrapper = (PlaneWrapper) assignPlaneCombo.getSelectedItem();
        PilotWrapper selectedPilotWrapper = (PilotWrapper) assignPilotCombo.getSelectedItem();

        if (selectedPlaneWrapper == null) {
            JOptionPane.showMessageDialog(this, "Please select a plane.", "Assignment Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Check if the selected pilot is the placeholder
        if (selectedPilotWrapper == null || selectedPilotWrapper.getPilot() == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid pilot to assign.", "Assignment Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int planeId = selectedPlaneWrapper.getPlane().getId();
        int pilotId = selectedPilotWrapper.getPilot().getId();

        Optional<Pilot> currentPilot = dataManager.getAssignedPilotForPlane(planeId);
        if (currentPilot.isPresent() && currentPilot.get().getId() == pilotId) {
            JOptionPane.showMessageDialog(this, "This pilot is already assigned to this plane.", "Assignment Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean success = dataManager.assignPilotToPlane(planeId, pilotId);

        if (success) {
            mainApp.refreshAllPanels(); // Refresh all panels via main app
            JOptionPane.showMessageDialog(this, "Pilot assigned successfully.", "Assignment Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to assign pilot. Check console.", "Assignment Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void unassignPilotAction() {
         PlaneWrapper selectedPlaneWrapper = (PlaneWrapper) assignPlaneCombo.getSelectedItem();

        if (selectedPlaneWrapper == null) {
             JOptionPane.showMessageDialog(this, "Please select a plane first.", "Unassign Error", JOptionPane.WARNING_MESSAGE);
             return;
        }

        int planeId = selectedPlaneWrapper.getPlane().getId();

         if (dataManager.getAssignedPilotForPlane(planeId).isEmpty()) {
             JOptionPane.showMessageDialog(this, "No pilot is currently assigned to this plane.", "Unassign Info", JOptionPane.INFORMATION_MESSAGE);
             unassignButton.setEnabled(false);
             return;
         }

         int confirmation = JOptionPane.showConfirmDialog(this,
                "Unassign pilot from plane " + selectedPlaneWrapper + "?",
                "Confirm Unassignment", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = dataManager.assignPilotToPlane(planeId, null);
            if (success) {
                mainApp.refreshAllPanels(); // Refresh all panels via main app
                JOptionPane.showMessageDialog(this, "Pilot unassigned successfully.", "Unassignment Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to unassign pilot. Check console.", "Unassignment Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Wrapper Classes for ComboBoxes ---
    private static class PlaneWrapper {
        private final Plane plane;
        public PlaneWrapper(Plane plane) { this.plane = plane; }
        public Plane getPlane() { return plane; }
        @Override public String toString() { return "ID: " + plane.getId() + " - " + plane.getModel() + " (" + plane.getRegistrationNumber() + ")"; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; PlaneWrapper that = (PlaneWrapper) o; return plane.getId() == that.plane.getId(); }
        @Override public int hashCode() { return Integer.hashCode(plane.getId()); }
    }

    private static class PilotWrapper {
        private final Pilot pilot;
        // Allow null pilot for placeholder item
        public PilotWrapper(Pilot pilot) { this.pilot = pilot; }
        public Pilot getPilot() { return pilot; }
        @Override public String toString() { return pilot == null ? "<Select Pilot>" : "ID: " + pilot.getId() + " - " + pilot.getName() + " (" + pilot.getLicenseNumber() + ")"; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; PilotWrapper that = (PilotWrapper) o; if (pilot == null) return that.pilot == null; if (that.pilot == null) return false; return pilot.getId() == that.pilot.getId(); }
        @Override public int hashCode() { return pilot != null ? Integer.hashCode(pilot.getId()) : 0; }
    }
} 