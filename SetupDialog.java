package evoting.gui;

import evoting.util.AppLogger;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog that collects the number of voters before the session begins.
 *
 * Requirement satisfied:
 *  - Event Handling     : OK/Cancel button events.
 *  - Exception Handling : non-integer and out-of-range input caught with
 *                         clear messages.
 *  - GUI                : polished dark-themed dialog.
 */
public class SetupDialog extends JDialog {

    private static final Color BG   = new Color(28, 36, 54);
    private static final Color FG   = new Color(230, 240, 255);
    private static final Color ACC  = new Color(64, 196, 255);
    private static final Color ERR  = new Color(255, 90, 100);
    private static final Color MUTED= new Color(130, 150, 190);

    private int    result   = -1;  // -1 means cancelled
    private final JTextField txtCount;
    private final JLabel     lblError;

    public SetupDialog(Frame owner) {
        super(owner, "E-Voting Setup", true);
        setBackground(BG);
        getContentPane().setBackground(BG);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets   = new Insets(10, 20, 5, 20);
        gbc.fill     = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth= 2;

        // Title
        JLabel title = new JLabel("🗳  E-Voting System", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACC);
        gbc.gridx = 0; gbc.gridy = 0;
        add(title, gbc);

        // Sub-heading
        JLabel sub = new JLabel("Enter the number of voters participating today:", JLabel.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(MUTED);
        gbc.gridy = 1;
        add(sub, gbc);

        // Input field
        txtCount = new JTextField("3");
        txtCount.setBackground(new Color(38, 50, 72));
        txtCount.setForeground(FG);
        txtCount.setCaretColor(ACC);
        txtCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCount.setHorizontalAlignment(JTextField.CENTER);
        txtCount.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 75, 110), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridy = 2;
        add(txtCount, gbc);

        // Error label
        lblError = new JLabel(" ", JLabel.CENTER);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(ERR);
        gbc.gridy = 3;
        add(lblError, gbc);

        // Buttons
        JButton btnOk     = buildButton("Start Voting", ACC, BG);
        JButton btnCancel = buildButton("Exit", new Color(55, 75, 110), FG);

        gbc.gridwidth = 1; gbc.gridy = 4; gbc.gridx = 0; gbc.insets = new Insets(8, 20, 16, 8);
        add(btnOk, gbc);
        gbc.gridx = 1; gbc.insets = new Insets(8, 8, 16, 20);
        add(btnCancel, gbc);

        // Event handling
        btnOk.addActionListener(e -> attemptConfirm());
        btnCancel.addActionListener(e -> { result = -1; dispose(); });
        txtCount.addActionListener(e -> attemptConfirm()); // Enter key

        pack();
        setMinimumSize(new Dimension(360, 0));
        setLocationRelativeTo(owner);
        txtCount.selectAll();
    }

    private void attemptConfirm() {
        try {
            int n = Integer.parseInt(txtCount.getText().trim());
            if (n < 1) {
                lblError.setText("Please enter a number greater than 0.");
                return;
            }
            result = n;
            dispose();
            AppLogger.info("Session started with " + n + " voter(s).");
        } catch (NumberFormatException ex) {
            lblError.setText("Please enter a valid whole number.");
            AppLogger.warning("Invalid voter count input: " + txtCount.getText());
        }
    }

    /** @return the number of voters entered, or -1 if cancelled. */
    public int getVoterCount() { return result; }

    private JButton buildButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
