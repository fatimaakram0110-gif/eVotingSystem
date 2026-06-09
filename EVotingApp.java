package evoting.gui;

import evoting.service.VotingService;
import evoting.util.AppLogger;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Application entry point.
 *
 * Responsibilities:
 *  - Set look-and-feel.
 *  - Show the setup dialog to collect voter count.
 *  - Construct the VotingService with hard-coded candidates.
 *  - Build and display the main frame containing VotingFormPanel.
 *
 * Requirement satisfied:
 *  - Code Refactoring   : bootstrap separated from form/service logic.
 *  - Exception Handling : L&F errors caught and logged.
 */
public class EVotingApp {

    /** Candidate list – change here to update the ballot. */
    private static final List<String> CANDIDATES = List.of(
            "Nawaz Sharif",
            "Imran Khan",
            "Bilawal Bhutto Zardari"
    );

    public static void main(String[] args) {
        // Ensure all Swing work happens on the Event Dispatch Thread
        SwingUtilities.invokeLater(EVotingApp::launchApplication);
    }

    private static void launchApplication() {
        applyLookAndFeel();

        // 1. Collect voter count via the setup dialog
        SetupDialog setup = new SetupDialog(null);
        setup.setVisible(true);

        int voterCount = setup.getVoterCount();
        if (voterCount < 1) {
            AppLogger.info("User cancelled setup. Exiting.");
            System.exit(0);
        }

        // 2. Build service
        VotingService service = new VotingService(voterCount, CANDIDATES);

        // 3. Build main window
        JFrame frame = new JFrame("E-Voting System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(560, 640));

        VotingFormPanel panel = new VotingFormPanel(frame, service);
        frame.setContentPane(panel);
        frame.pack();
        frame.setMinimumSize(new Dimension(480, 560));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        AppLogger.info("Main window displayed.");
    }

    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            AppLogger.warning("Could not set system L&F: " + e.getMessage());
        }
    }
}
