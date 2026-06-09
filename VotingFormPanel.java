package evoting.gui;

import evoting.model.Voter;
import evoting.service.VotingService;
import evoting.service.VotingService.DuplicateCnicException;
import evoting.service.VotingService.UnknownCandidateException;
import evoting.util.AppLogger;
import evoting.validation.InputValidator;
import evoting.validation.InputValidator.ValidationResult;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

/**
 * Main voting form panel.
 *
 * Requirement satisfied:
 *  - Event Handling     : ActionListener on Submit, KeyAdapter for CNIC
 *                         auto-formatting.
 *  - Exception Handling : validation, duplicate CNIC, file I/O all caught
 *                         with user-friendly dialogs.
 *  - Code Refactoring   : GUI split into focused panel class; business
 *                         logic is NOT here.
 *  - GUI                : polished look-and-feel with custom colours,
 *                         progress bar, and results dialog.
 */
public class VotingFormPanel extends JPanel {

    // ---------------------------------------------------------------
    // Palette
    // ---------------------------------------------------------------
    private static final Color BG_DARK      = new Color(18, 24, 38);
    private static final Color BG_CARD      = new Color(28, 36, 54);
    private static final Color ACCENT       = new Color(64, 196, 255);
    private static final Color ACCENT_HOVER = new Color(100, 220, 255);
    private static final Color TEXT_PRIMARY = new Color(230, 240, 255);
    private static final Color TEXT_MUTED   = new Color(130, 150, 190);
    private static final Color INPUT_BG     = new Color(38, 50, 72);
    private static final Color BORDER_COLOR = new Color(55, 75, 110);
    private static final Color SUCCESS      = new Color(60, 210, 130);
    private static final Color ERROR_COLOR  = new Color(255, 90, 100);

    // ---------------------------------------------------------------
    // Form fields
    // ---------------------------------------------------------------
    private final JTextField  txtName;
    private final JTextField  txtCNIC;
    private final JTextField  txtAddress;
    private final JTextField  txtEmail;
    private final JComboBox<String> cmbCandidate;
    private final JLabel      lblProgress;
    private final JProgressBar progressBar;
    private final JLabel      lblStatus;

    // ---------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------
    private final VotingService   service;
    private final InputValidator  validator = new InputValidator();
    private final JFrame          parentFrame;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public VotingFormPanel(JFrame parentFrame, VotingService service) {
        this.parentFrame = parentFrame;
        this.service     = service;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);

        // ---- Header ----
        add(buildHeader(), BorderLayout.NORTH);

        // ---- Form body ----
        JPanel bodyWrapper = new JPanel(new GridBagLayout());
        bodyWrapper.setBackground(BG_DARK);
        bodyWrapper.setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(30, 35, 30, 35)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // Progress label
        lblProgress = styledLabel("Voter 1 of " + service.getTotalVoters(), Font.BOLD, 13, ACCENT);
        progressBar = new JProgressBar(0, service.getTotalVoters());
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(ACCENT);
        progressBar.setBackground(INPUT_BG);
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressBar.setBorderPainted(false);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(lblProgress, gbc);
        gbc.gridy = 1;
        card.add(progressBar, gbc);
        gbc.gridy = 2;
        card.add(new JSeparator(), gbc);

        // Fields
        txtName    = styledTextField("Full Name");
        txtCNIC    = styledTextField("e.g. 35202-1234567-8");
        txtAddress = styledTextField("City, Province");
        txtEmail   = styledTextField("user@example.com");

        List<String> names = service.getCandidates().stream()
                .map(c -> c.getName()).toList();
        String[] items = new String[names.size() + 1];
        items[0] = "Select Candidate";
        for (int i = 0; i < names.size(); i++) items[i + 1] = names.get(i);
        cmbCandidate = styledComboBox(items);

        addFormRow(card, gbc, 3, "Full Name",            txtName);
        addFormRow(card, gbc, 5, "CNIC (XXXXX-XXXXXXX-X)", txtCNIC);
        addFormRow(card, gbc, 7, "Address",              txtAddress);
        addFormRow(card, gbc, 9, "Email Address",        txtEmail);
        addFormRow(card, gbc,11, "Candidate",            cmbCandidate);

        // Status label
        lblStatus = styledLabel(" ", Font.PLAIN, 12, ERROR_COLOR);
        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 2;
        card.add(lblStatus, gbc);

        // Submit button
        JButton btnSubmit = buildSubmitButton();
        gbc.gridy = 14; gbc.insets = new Insets(16, 8, 4, 8);
        card.add(btnSubmit, gbc);

        bodyWrapper.add(card, new GridBagConstraints());
        add(bodyWrapper, BorderLayout.CENTER);

        // ---- Footer ----
        add(buildFooter(), BorderLayout.SOUTH);

        // ---- CNIC auto-format on focus lost ----
        txtCNIC.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { autoFormatCnic(); }
        });

        // ---- Enter key submits ----
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) submitVote();
            }
        };
        txtName.addKeyListener(enterSubmit);
        txtCNIC.addKeyListener(enterSubmit);
        txtAddress.addKeyListener(enterSubmit);
        txtEmail.addKeyListener(enterSubmit);

        AppLogger.info("VotingFormPanel initialised.");
    }

    // ---------------------------------------------------------------
    // Event: submit vote
    // ---------------------------------------------------------------
    private void submitVote() {
        lblStatus.setText(" ");

        String name      = txtName.getText().trim();
        String cnic      = txtCNIC.getText().trim();
        String address   = txtAddress.getText().trim();
        String email     = txtEmail.getText().trim();
        String candidate = (String) cmbCandidate.getSelectedItem();

        // 1. Input validation
        ValidationResult vr = validator.validateAll(name, cnic, address, email, candidate);
        if (!vr.isValid()) {
            lblStatus.setForeground(ERROR_COLOR);
            lblStatus.setText("⚠  " + vr.getMessage());
            AppLogger.warning("Validation failed: " + vr.getMessage());
            return;
        }

        // 2. Build voter and cast vote (business logic)
        try {
            Voter voter = new Voter(name, cnic, address, email, candidate);
            service.castVote(voter);

        } catch (DuplicateCnicException ex) {
            lblStatus.setForeground(ERROR_COLOR);
            lblStatus.setText("⚠  " + ex.getMessage());
            AppLogger.warning(ex.getMessage());
            return;

        } catch (UnknownCandidateException ex) {
            lblStatus.setForeground(ERROR_COLOR);
            lblStatus.setText("⚠  " + ex.getMessage());
            AppLogger.severe(ex.getMessage());
            return;
        }

        // 3. Voting complete?
        if (service.isVotingComplete()) {
            try {
                service.saveResults();
                showResultsDialog();
                parentFrame.dispose();
            } catch (IOException ex) {
                AppLogger.severe("Failed to save results", ex);
                JOptionPane.showMessageDialog(parentFrame,
                        "Error saving results:\n" + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // 4. Advance to next voter
        updateProgress();
        clearForm();
        lblStatus.setForeground(SUCCESS);
        lblStatus.setText("✔  Vote recorded successfully.");
    }

    // ---------------------------------------------------------------
    // UI helpers
    // ---------------------------------------------------------------

    private void updateProgress() {
        int done  = service.getVotersProcessed();
        int total = service.getTotalVoters();
        int next  = done + 1;
        if (next <= total) lblProgress.setText("Voter " + next + " of " + total);
        progressBar.setValue(done);
    }

    private void clearForm() {
        txtName.setText("");
        txtCNIC.setText("");
        txtAddress.setText("");
        txtEmail.setText("");
        cmbCandidate.setSelectedIndex(0);
        txtName.requestFocusInWindow();
    }

    /** Auto-strips non-digits and re-inserts dashes to match XXXXX-XXXXXXX-X. */
    private void autoFormatCnic() {
        String raw = txtCNIC.getText().replaceAll("[^0-9]", "");
        if (raw.length() == 13) {
            txtCNIC.setText(raw.substring(0,5) + "-" + raw.substring(5,12) + "-" + raw.charAt(12));
        }
    }

    private void showResultsDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI;padding:10px;width:320px'>");
        sb.append("<h2 style='color:#40C4FF'>Voting Complete!</h2>");
        sb.append("<p style='color:#aaa'>Results have been saved to <b>result.txt</b></p><hr/>");
        sb.append("<h3 style='color:#eee'>Final Vote Count</h3><ul>");
        service.getCandidates().forEach(c ->
                sb.append("<li style='color:#ccc'>").append(c.getName())
                  .append(": <b>").append(c.getVoteCount()).append("</b></li>"));
        sb.append("</ul><hr/>");
        evoting.model.Candidate winner = service.getWinner();
        if (winner != null) {
            sb.append("<p style='color:#3CD282;font-size:14px'><b>🏆 Winner: ")
              .append(winner.getName()).append("</b></p>");
        }
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(parentFrame, sb.toString(),
                "Election Results", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------------------------------------------------------
    // Builder helpers for styled components
    // ---------------------------------------------------------------

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_CARD);
        header.setBorder(new EmptyBorder(20, 40, 16, 40));

        JLabel title = new JLabel("🗳  E-Voting System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Secure Electronic Ballot");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(title);
        text.add(sub);
        header.add(text, BorderLayout.WEST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(BG_CARD);
        footer.setBorder(new EmptyBorder(10, 0, 10, 0));
        JLabel lbl = new JLabel("SCD Lab  •  E-Voting System  •  2025");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        footer.add(lbl);
        return footer;
    }

    private JButton buildSubmitButton() {
        JButton btn = new JButton("Submit Vote  →");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(BG_DARK);
        btn.setBackground(ACCENT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 44));

        // Event handling – hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(ACCENT); }
        });

        // Event handling – click
        btn.addActionListener(e -> submitVote());
        return btn;
    }

    private void addFormRow(JPanel card, GridBagConstraints gbc,
                             int row, String label, JComponent field) {
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = row;
        gbc.insets = new Insets(6, 8, 0, 8);
        JLabel lbl = styledLabel(label, Font.PLAIN, 12, TEXT_MUTED);
        card.add(lbl, gbc);

        gbc.gridx = 0; gbc.gridy = row + 1; gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 8, 8, 8);
        card.add(field, gbc);
    }

    private JTextField styledTextField(String placeholder) {
        JTextField tf = new JTextField(24);
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        // Placeholder via FocusListener
        tf.setText(placeholder);
        tf.setForeground(TEXT_MUTED);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(TEXT_PRIMARY); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().isBlank()) { tf.setText(placeholder); tf.setForeground(TEXT_MUTED); }
            }
        });
        return tf;
    }

    /** Gets actual text (strips placeholder value). */
    private String getFieldText(JTextField tf) {
        // placeholder text is shown as muted – if it matches placeholder, return ""
        return tf.getForeground().equals(TEXT_MUTED) ? "" : tf.getText().trim();
    }

    private JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return cb;
    }

    private JLabel styledLabel(String text, int style, int size, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", style, size));
        lbl.setForeground(color);
        return lbl;
    }
}
