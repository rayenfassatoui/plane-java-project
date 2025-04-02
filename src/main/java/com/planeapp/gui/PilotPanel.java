package com.planeapp.gui;

import com.planeapp.dao.PilotDAO;
import com.planeapp.model.Pilot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class PilotPanel extends JPanel {

    private final PilotDAO pilotDAO;
    private final PlaneAppGUI mainApp; // Reference to main GUI

    private JTable pilotTable;
    private DefaultTableModel pilotTableModel;
    private JTextField pilotNameField;
    private JTextField pilotLicenseField;
    private JTextField pilotSearchField;

    public PilotPanel(PilotDAO pilotDAO, PlaneAppGUI mainApp) {
        super(new BorderLayout(10, 10));
        this.pilotDAO = pilotDAO;
        this.mainApp = mainApp;
        initComponents();
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Table ---
        String[] columnNames = {"ID", "Name", "License Number"};
         pilotTableModel = new DefaultTableModel(columnNames, 0) {
             @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        pilotTable = new JTable(pilotTableModel);
        pilotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(pilotTable);

        // --- Input & Button Panel ---
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add Fields
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        pilotNameField = new JTextField(15);
        controlPanel.add(pilotNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        controlPanel.add(new JLabel("License No:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        pilotLicenseField = new JTextField(15);
        controlPanel.add(pilotLicenseField, gbc);

        // Add Button
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 2; gbc.fill = GridBagConstraints.VERTICAL; gbc.weightx = 0.0;
        JButton addPilotButton = new JButton("Add Pilot");
        addPilotButton.addActionListener(e -> addPilotAction());
        controlPanel.add(addPilotButton, gbc);
        gbc.gridheight = 1; // Reset

        // Separator
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; // Reset

        // Search Field
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Search (Name/ID):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        pilotSearchField = new JTextField(15);
        controlPanel.add(pilotSearchField, gbc);

        // Search/View All Buttons Panel
        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton searchPilotButton = new JButton("Search");
        JButton viewAllPilotsButton = new JButton("View All");
        searchPilotButton.addActionListener(e -> searchPilotAction());
        viewAllPilotsButton.addActionListener(e -> refreshTable()); // Call local refresh
        searchButtonPanel.add(searchPilotButton);
        searchButtonPanel.add(viewAllPilotsButton);

        gbc.gridx = 2; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0.0;
        controlPanel.add(searchButtonPanel, gbc);

        // Delete Button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        JButton deletePilotButton = new JButton("Delete Selected Pilot");
        deletePilotButton.addActionListener(e -> deletePilotAction());
        controlPanel.add(deletePilotButton, gbc);

        // Add components to this JPanel
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    // --- Refresh Methods ---
    public void refreshTable() {
        refreshTable(pilotDAO.getAllPilots());
        if (pilotSearchField != null) pilotSearchField.setText("");
    }

    private void refreshTable(List<Pilot> pilots) {
        pilotTableModel.setRowCount(0);
        for (Pilot pilot : pilots) {
            Object[] rowData = { pilot.getId(), pilot.getName(), pilot.getLicenseNumber() };
            pilotTableModel.addRow(rowData);
        }
    }

    // --- Action Methods ---
    private void addPilotAction() {
        String name = pilotNameField.getText().trim();
        String license = pilotLicenseField.getText().trim();
        if (name.isEmpty() || license.isEmpty()) { JOptionPane.showMessageDialog(this, "Name and License Number cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE); return; }
        Pilot dummyPilot = new Pilot(0, name, license);
        Optional<Pilot> addedPilotOpt = pilotDAO.addPilot(dummyPilot);
        if (addedPilotOpt.isPresent()) {
            mainApp.refreshAllPanels(); // Refresh all panels via main app
            pilotNameField.setText("");
            pilotLicenseField.setText("");
            JOptionPane.showMessageDialog(this, "Pilot added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
             JOptionPane.showMessageDialog(this, "Failed to add pilot. Check console (duplicate license?).", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPilotAction() {
        String query = pilotSearchField.getText().trim();
        if (query.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter search term (Name or ID).", "Search Error", JOptionPane.WARNING_MESSAGE); return; }
        List<Pilot> results = pilotDAO.searchPilots(query);
        refreshTable(results); // Refresh only this panel's table
         if (results.isEmpty()) { JOptionPane.showMessageDialog(this, "No pilots found matching '" + query + "'.", "Search Result", JOptionPane.INFORMATION_MESSAGE); }
    }

    private void deletePilotAction() {
        int selectedRow = pilotTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Please select a pilot to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE); return; }
        int pilotId = (int) pilotTableModel.getValueAt(selectedRow, 0);
        String pilotName = (String) pilotTableModel.getValueAt(selectedRow, 1);
        int confirmation = JOptionPane.showConfirmDialog(this, "Delete pilot: " + pilotName + " (ID: " + pilotId + ")?\n(Will unassign from planes)", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean deleted = pilotDAO.deletePilot(pilotId);
            if (deleted) {
                mainApp.refreshAllPanels(); // Refresh all panels via main app
                JOptionPane.showMessageDialog(this, "Pilot deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Could not delete pilot ID: " + pilotId + ". Check console.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 