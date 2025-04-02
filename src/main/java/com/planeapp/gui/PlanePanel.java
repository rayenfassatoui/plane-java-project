package com.planeapp.gui;

import com.planeapp.data.DataManager;
import com.planeapp.model.Passenger;
import com.planeapp.model.Plane;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class PlanePanel extends JPanel {

    private final DataManager dataManager;
    private final PlaneAppGUI mainApp; // Reference to main GUI for coordinated refresh

    private JTable planeTable;
    private DefaultTableModel planeTableModel;
    private JTextField planeModelField;
    private JTextField planeRegField;
    private JTextField planeSearchField;
    private JButton deletePlaneButton; // Make accessible for enabling/disabling

    // Passenger components
    private JTable passengerTable;
    private DefaultTableModel passengerTableModel;
    private JTextField passengerNameField;
    private JTextField passengerPassportField;
    private JTextField passengerSeatField;
    private JButton addPassengerButton;
    private JButton deletePassengerButton;
    private JPanel passengerControlPanel; // Panel holding passenger controls

    public PlanePanel(DataManager dataManager, PlaneAppGUI mainApp) {
        super(new BorderLayout(10, 10)); // Use BorderLayout for the panel itself
        this.dataManager = dataManager;
        this.mainApp = mainApp;
        initComponents();
        enablePassengerControls(false); // Initially disable passenger controls
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top Panel: Plane Table and Controls ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        // --- Plane Table ---
        String[] planeColumnNames = {"ID", "Model", "Registration Number", "Pilot ID"};
        planeTableModel = new DefaultTableModel(planeColumnNames, 0) {
             @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        planeTable = new JTable(planeTableModel);
        planeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane planeScrollPane = new JScrollPane(planeTable);
        planeScrollPane.setPreferredSize(new Dimension(-1, 150)); // Give plane table a preferred height

        // --- Plane Input & Button Panel ---
        JPanel planeControlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcPlane = new GridBagConstraints();
        gbcPlane.insets = new Insets(5, 5, 5, 5);
        gbcPlane.anchor = GridBagConstraints.WEST;

        // Add Fields
        gbcPlane.gridx = 0; gbcPlane.gridy = 0; planeControlPanel.add(new JLabel("Model:"), gbcPlane);
        gbcPlane.gridx = 1; gbcPlane.fill = GridBagConstraints.HORIZONTAL; gbcPlane.weightx = 1.0; planeModelField = new JTextField(15); planeControlPanel.add(planeModelField, gbcPlane);
        gbcPlane.gridx = 0; gbcPlane.gridy = 1; gbcPlane.fill = GridBagConstraints.NONE; gbcPlane.weightx = 0.0; planeControlPanel.add(new JLabel("Registration:"), gbcPlane);
        gbcPlane.gridx = 1; gbcPlane.fill = GridBagConstraints.HORIZONTAL; gbcPlane.weightx = 1.0; planeRegField = new JTextField(15); planeControlPanel.add(planeRegField, gbcPlane);
        gbcPlane.gridx = 2; gbcPlane.gridy = 0; gbcPlane.gridheight = 2; gbcPlane.fill = GridBagConstraints.VERTICAL; gbcPlane.weightx = 0.0; JButton addPlaneButton = new JButton("Add Plane"); addPlaneButton.addActionListener(e -> addPlaneAction()); planeControlPanel.add(addPlaneButton, gbcPlane); gbcPlane.gridheight = 1;
        gbcPlane.gridx = 0; gbcPlane.gridy = 2; gbcPlane.gridwidth = 3; gbcPlane.fill = GridBagConstraints.HORIZONTAL; planeControlPanel.add(new JSeparator(), gbcPlane); gbcPlane.gridwidth = 1;
        gbcPlane.gridx = 0; gbcPlane.gridy = 3; gbcPlane.fill = GridBagConstraints.NONE; planeControlPanel.add(new JLabel("Search (Model/Reg):"), gbcPlane);
        gbcPlane.gridx = 1; gbcPlane.fill = GridBagConstraints.HORIZONTAL; gbcPlane.weightx = 1.0; planeSearchField = new JTextField(15); planeControlPanel.add(planeSearchField, gbcPlane);
        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); JButton searchPlaneButton = new JButton("Search"); JButton viewAllPlanesButton = new JButton("View All"); searchPlaneButton.addActionListener(e -> searchPlaneAction()); viewAllPlanesButton.addActionListener(e -> refreshTable()); searchButtonPanel.add(searchPlaneButton); searchButtonPanel.add(viewAllPlanesButton);
        gbcPlane.gridx = 2; gbcPlane.fill = GridBagConstraints.NONE; gbcPlane.anchor = GridBagConstraints.WEST; gbcPlane.weightx = 0.0; planeControlPanel.add(searchButtonPanel, gbcPlane);
        gbcPlane.gridx = 0; gbcPlane.gridy = 4; gbcPlane.gridwidth = 3; gbcPlane.anchor = GridBagConstraints.EAST; gbcPlane.fill = GridBagConstraints.NONE; deletePlaneButton = new JButton("Delete Selected Plane"); deletePlaneButton.addActionListener(e -> deletePlaneAction()); planeControlPanel.add(deletePlaneButton, gbcPlane);

        topPanel.add(planeScrollPane, BorderLayout.CENTER);
        topPanel.add(planeControlPanel, BorderLayout.SOUTH);

        // --- Bottom Panel: Passenger Table and Controls ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Passengers for Selected Plane")); // Add title

        // --- Passenger Table ---
        String[] passengerColumnNames = {"ID", "Name", "Passport Number", "Seat"};
        passengerTableModel = new DefaultTableModel(passengerColumnNames, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        passengerTable = new JTable(passengerTableModel);
        passengerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane passengerScrollPane = new JScrollPane(passengerTable);

        // --- Passenger Input & Button Panel ---
        passengerControlPanel = new JPanel(new GridBagLayout()); // Assign to member variable
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.insets = new Insets(5, 5, 5, 5);
        gbcPass.anchor = GridBagConstraints.WEST;

        // Passenger Fields
        gbcPass.gridx = 0; gbcPass.gridy = 0; passengerControlPanel.add(new JLabel("Name:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 0; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passengerNameField = new JTextField(15);
        passengerControlPanel.add(passengerNameField, gbcPass);

        gbcPass.gridx = 0; gbcPass.gridy = 1; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0.0;
        passengerControlPanel.add(new JLabel("Passport:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 1; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passengerPassportField = new JTextField(15);
        passengerControlPanel.add(passengerPassportField, gbcPass);

        gbcPass.gridx = 2; gbcPass.gridy = 0; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0.0;
        passengerControlPanel.add(new JLabel("Seat:"), gbcPass);
        gbcPass.gridx = 3; gbcPass.gridy = 0; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 0.5; // Adjust weight
        passengerSeatField = new JTextField(5);
        passengerControlPanel.add(passengerSeatField, gbcPass);

        // Passenger Buttons
        gbcPass.gridx = 2; gbcPass.gridy = 1; gbcPass.gridwidth=2; gbcPass.fill = GridBagConstraints.NONE; gbcPass.anchor = GridBagConstraints.EAST; gbcPass.weightx = 0.0;
        addPassengerButton = new JButton("Add Passenger");
        addPassengerButton.addActionListener(e -> addPassengerAction());
        passengerControlPanel.add(addPassengerButton, gbcPass);
        gbcPass.gridwidth = 1; // Reset

        gbcPass.gridx = 0; gbcPass.gridy = 2; gbcPass.gridwidth = 4; gbcPass.anchor = GridBagConstraints.EAST; gbcPass.fill = GridBagConstraints.NONE;
        deletePassengerButton = new JButton("Delete Selected Passenger");
        deletePassengerButton.addActionListener(e -> deletePassengerAction());
        passengerControlPanel.add(deletePassengerButton, gbcPass);

        bottomPanel.add(passengerScrollPane, BorderLayout.CENTER);
        bottomPanel.add(passengerControlPanel, BorderLayout.SOUTH);

        // --- Combine Panels using JSplitPane ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setResizeWeight(0.4); // Give more initial space to top panel potentially
        add(splitPane, BorderLayout.CENTER); // Add split pane to the main panel

        // --- Add Listener to Plane Table Selection ---
        planeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = planeTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int planeId = (int) planeTableModel.getValueAt(selectedRow, 0);
                        refreshPassengerTable(planeId);
                        enablePassengerControls(true);
                        deletePlaneButton.setEnabled(true); // Ensure delete plane is enabled
                    } else {
                        passengerTableModel.setRowCount(0); // Clear passenger table
                        enablePassengerControls(false);
                         deletePlaneButton.setEnabled(false); // Disable delete plane if none selected
                    }
                }
            }
        });
         // Initial state for delete plane button
         deletePlaneButton.setEnabled(false);
    }

    // Enable/disable passenger input fields and buttons
    private void enablePassengerControls(boolean enable) {
         if (passengerControlPanel != null) { // Check if initialized
            passengerNameField.setEnabled(enable);
            passengerPassportField.setEnabled(enable);
            passengerSeatField.setEnabled(enable);
            addPassengerButton.setEnabled(enable);
            deletePassengerButton.setEnabled(enable);
            // Optionally disable passenger table interaction too?
            // passengerTable.setEnabled(enable);
         }
    }

    // --- Refresh Methods ---
    public void refreshTable() {
        int selectedRow = planeTable.getSelectedRow(); // Preserve selection if possible
        Integer selectedPlaneId = null;
        if(selectedRow != -1) {
            selectedPlaneId = (Integer) planeTableModel.getValueAt(selectedRow, 0);
        }

        refreshTable(dataManager.getAllPlanes());
        if(planeSearchField != null) planeSearchField.setText("");

         // Re-select row if it still exists
         if (selectedPlaneId != null) {
            for (int i = 0; i < planeTableModel.getRowCount(); i++) {
                if (selectedPlaneId.equals(planeTableModel.getValueAt(i, 0))) {
                    planeTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
        // Update passenger panel based on new selection state
        handlePlaneSelectionChange();
    }

    private void refreshTable(List<Plane> planes) {
        planeTableModel.setRowCount(0);
        for (Plane plane : planes) {
            Object[] rowData = { plane.getId(), plane.getModel(), plane.getRegistrationNumber(), plane.getPilotId() == null ? "None" : plane.getPilotId() };
            planeTableModel.addRow(rowData);
        }
    }

    private void refreshPassengerTable(int planeId) {
        List<Passenger> passengers = dataManager.getPassengersForPlane(planeId);
        passengerTableModel.setRowCount(0); // Clear existing passenger data
        for (Passenger passenger : passengers) {
            Object[] rowData = { passenger.getId(), passenger.getName(), passenger.getPassportNumber(), passenger.getSeatNumber() };
            passengerTableModel.addRow(rowData);
        }
    }

     // Helper to update passenger panel based on current plane selection
     private void handlePlaneSelectionChange() {
         int selectedRow = planeTable.getSelectedRow();
         if (selectedRow != -1) {
             int planeId = (int) planeTableModel.getValueAt(selectedRow, 0);
             refreshPassengerTable(planeId);
             enablePassengerControls(true);
             deletePlaneButton.setEnabled(true);
         } else {
             passengerTableModel.setRowCount(0); // Clear passenger table
             enablePassengerControls(false);
             deletePlaneButton.setEnabled(false);
         }
     }

     // --- Action Methods ---
    private void addPlaneAction() {
        String model = planeModelField.getText().trim();
        String reg = planeRegField.getText().trim();
        if (model.isEmpty() || reg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Model and Registration Number cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Plane dummyPlane = new Plane(0, model, reg);
        Optional<Plane> addedPlaneOpt = dataManager.addPlane(dummyPlane);
        if (addedPlaneOpt.isPresent()) {
            mainApp.refreshAllPanels(); // Refresh all panels via main app
            planeModelField.setText("");
            planeRegField.setText("");
            JOptionPane.showMessageDialog(this, "Plane added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add plane. Check console (duplicate registration?).", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPlaneAction() {
        String query = planeSearchField.getText().trim();
        if (query.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Please enter search term (Model or Registration).", "Search Error", JOptionPane.WARNING_MESSAGE);
             return;
        }
        List<Plane> results = dataManager.searchPlanes(query);
        refreshTable(results); // Only refresh this panel's table
         if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No planes found matching '" + query + "'.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deletePlaneAction() {
        int selectedRow = planeTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Please select a plane to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE); return; }
        int planeId = (int) planeTableModel.getValueAt(selectedRow, 0);
        int confirmation = JOptionPane.showConfirmDialog(this, "Delete plane ID: " + planeId + "? (Passengers will also be deleted)", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean deleted = dataManager.deletePlane(planeId);
            if (deleted) {
                mainApp.refreshAllPanels(); // Refresh all panels via main app
                JOptionPane.showMessageDialog(this, "Plane deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Could not delete plane ID: " + planeId + ". Check console.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Passenger Action Methods ---
    private void addPassengerAction() {
        int selectedPlaneRow = planeTable.getSelectedRow();
        if (selectedPlaneRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a plane first to add a passenger.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int planeId = (int) planeTableModel.getValueAt(selectedPlaneRow, 0);

        String name = passengerNameField.getText().trim();
        String passport = passengerPassportField.getText().trim();
        String seat = passengerSeatField.getText().trim();

        if (name.isEmpty() || passport.isEmpty() || seat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Passenger Name, Passport, and Seat cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ID is assigned by DB
        Passenger dummyPassenger = new Passenger(0, name, passport, seat);
        Optional<Passenger> addedPassengerOpt = dataManager.addPassenger(dummyPassenger, planeId);

        if (addedPassengerOpt.isPresent()) {
            refreshPassengerTable(planeId); // Refresh only passenger table
            passengerNameField.setText("");
            passengerPassportField.setText("");
            passengerSeatField.setText("");
            JOptionPane.showMessageDialog(this, "Passenger added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add passenger. Check console (duplicate seat?)", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

     private void deletePassengerAction() {
        int selectedPlaneRow = planeTable.getSelectedRow();
         int selectedPassengerRow = passengerTable.getSelectedRow();

        if (selectedPlaneRow == -1 || selectedPassengerRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a plane and then a passenger to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int passengerId = (int) passengerTableModel.getValueAt(selectedPassengerRow, 0);
        int planeId = (int) planeTableModel.getValueAt(selectedPlaneRow, 0); // Get planeId again for refresh

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete passenger ID: " + passengerId + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            boolean deleted = dataManager.deletePassenger(passengerId);
            if (deleted) {
                refreshPassengerTable(planeId); // Refresh only passenger table
                JOptionPane.showMessageDialog(this, "Passenger deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Could not delete passenger ID: " + passengerId + ". Check console.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 