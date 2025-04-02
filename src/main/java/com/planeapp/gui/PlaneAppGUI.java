package com.planeapp.gui;

import com.planeapp.dao.JdbcPassengerDAO;
import com.planeapp.dao.JdbcPilotDAO;
import com.planeapp.dao.JdbcPlaneDAO;
import com.planeapp.dao.PassengerDAO; // Import DAO interfaces
import com.planeapp.dao.PilotDAO;
import com.planeapp.dao.PlaneDAO;
// Model imports might not be needed directly here anymore
// import com.planeapp.model.Pilot;
// import com.planeapp.model.Plane;

import javax.swing.*;
// import javax.swing.table.DefaultTableModel; // Moved to panels
import java.awt.*;
import java.net.URL; // Import for resource loading
// Other imports like List, Optional, event listeners also moved to panels

public class PlaneAppGUI extends JFrame {

    // Remove DataManager, add individual DAOs
    // private final DataManager dataManager;
    private final PlaneDAO planeDAO;
    private final PilotDAO pilotDAO;
    private final PassengerDAO passengerDAO;

    private JTabbedPane tabbedPane;
    private JLabel logoLabel; // Label for the logo
    // Panel instances
    private PlanePanel planePanel;
    private PilotPanel pilotPanel;
    private AssignmentPanel assignmentPanel;

    // Remove fields for components now inside panels
    // private JTable planeTable; ... etc.
    // private JComboBox<PlaneWrapper> assignPlaneCombo; ... etc.

    // Modified Constructor to accept a title
    public PlaneAppGUI(String windowTitle) {
        // Instantiate DAOs
        this.planeDAO = new JdbcPlaneDAO();
        this.pilotDAO = new JdbcPilotDAO();
        this.passengerDAO = new JdbcPassengerDAO();

        setTitle(windowTitle); // Use the passed title
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        // Initial refresh call for all panels
        refreshAllPanels();

        setVisible(true);
    }

    private void initComponents() {
        // --- Load Logo --- (Add error handling)
        try {
            URL logoUrl = getClass().getResource("/tunisair.png"); // Corrected filename
            if (logoUrl != null) {
                ImageIcon logoIcon = new ImageIcon(logoUrl);

                // --- Scale the image --- START ---
                int targetHeight = 50; // Desired height in pixels
                int originalWidth = logoIcon.getIconWidth();
                int originalHeight = logoIcon.getIconHeight();

                // Calculate new width to maintain aspect ratio
                int targetWidth = -1;
                if (originalHeight > 0) { // Avoid division by zero
                    targetWidth = (int) (((double) targetHeight / originalHeight) * originalWidth);
                }

                if (targetWidth > 0 && targetHeight > 0) {
                    Image image = logoIcon.getImage();
                    Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                    logoIcon = new ImageIcon(scaledImage);
                } else {
                    System.err.println("Could not determine original image dimensions for scaling.");
                }
                // --- Scale the image --- END ---

                logoLabel = new JLabel(logoIcon);
                logoLabel.setHorizontalAlignment(JLabel.CENTER);
                logoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Add some padding
            } else {
                System.err.println("Logo image 'tunisair.png' not found in resources."); // Updated error message
                logoLabel = new JLabel("Logo Missing", JLabel.CENTER); // Placeholder
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
            logoLabel = new JLabel("Logo Error", JLabel.CENTER); // Placeholder
        }

        // --- Tabs ---
        tabbedPane = new JTabbedPane();

        // Instantiate panels, passing DAOs and this main App reference
        planePanel = new PlanePanel(planeDAO, passengerDAO, this);
        pilotPanel = new PilotPanel(pilotDAO, this);
        assignmentPanel = new AssignmentPanel(planeDAO, pilotDAO, this);

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

        // --- Layout --- (Add Logo to NORTH)
        JPanel mainPanel = new JPanel(new BorderLayout());
        if (logoLabel != null) {
            mainPanel.add(logoLabel, BorderLayout.NORTH);
        }
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel); // Add the main panel containing logo and tabs
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
        // --- 1. Set Look and Feel (Try FlatLaf Light) ---
        try {
            // Attempt to set FlatLaf Light theme
            UIManager.setLookAndFeel( "com.formdev.flatlaf.FlatLightLaf" );
        } catch (Exception ex) {
            System.err.println( "Failed to initialize FlatLaf. Falling back..." );
            // Fallback to System Look and Feel if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Failed to set System Look and Feel. Using default.");
                // e.printStackTrace(); // Optional: print stack trace for debugging
            }
        }

        // --- 2. Prompt for Company Name & Create GUI ---
        SwingUtilities.invokeLater(() -> {
            // Prompt for company name before creating the GUI
            String companyName = JOptionPane.showInputDialog(
                null, // Parent component (null for default)
                "Please enter your company name:", // Message
                "Company Name Setup", // Dialog title
                JOptionPane.QUESTION_MESSAGE // Message type icon
            );

            String windowTitle;
            // Use default title if user cancels or enters nothing
            if (companyName == null || companyName.trim().isEmpty()) {
                windowTitle = "Plane & Pilot Management System"; // Default
            } else {
                windowTitle = companyName.trim() + " - Management System"; // Use entered name
            }

            // Create the GUI with the determined title
            new PlaneAppGUI(windowTitle);
        });
    }
} 