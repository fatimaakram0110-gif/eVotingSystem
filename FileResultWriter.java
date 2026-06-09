package evoting.util;

import evoting.model.Candidate;
import evoting.model.Voter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles all file I/O for the voting results.
 *
 * Requirement satisfied:
 *  - Exception Handling : IOExceptions are caught and re-thrown as
 *                         a checked VotingException so the caller decides
 *                         how to present the error.
 *  - Code Refactoring   : file-writing responsibility extracted from GUI.
 */
public final class FileResultWriter {

    private static final String RESULT_FILE   = "result.txt";
    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FileResultWriter() {}

    /**
     * Writes every voter record and the final vote tally to result.txt.
     *
     * @param voters     ordered list of all voters who cast ballots
     * @param candidates list of candidates with their final vote counts
     * @throws IOException if the file cannot be written
     */
    public static void write(List<Voter> voters, List<Candidate> candidates)
            throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RESULT_FILE))) {

            bw.write("========================================");
            bw.newLine();
            bw.write("   E-VOTING SYSTEM - RESULTS REPORT");
            bw.newLine();
            bw.write("   Generated: " + LocalDateTime.now().format(DTF));
            bw.newLine();
            bw.write("========================================");
            bw.newLine();
            bw.newLine();

            // ---- Individual voter records ----
            bw.write("VOTER RECORDS");
            bw.newLine();
            bw.write("----------------------------------------");
            bw.newLine();

            int idx = 1;
            for (Voter v : voters) {
                bw.write("Voter #" + idx++);
                bw.newLine();
                bw.write(v.toString());
                bw.write("----------------------------------------");
                bw.newLine();
            }

            // ---- Aggregate tally ----
            bw.newLine();
            bw.write("FINAL VOTE COUNT");
            bw.newLine();
            bw.write("----------------------------------------");
            bw.newLine();

            int total = 0;
            for (Candidate c : candidates) {
                bw.write(c.toString());
                bw.newLine();
                total += c.getVoteCount();
            }

            bw.newLine();
            bw.write("Total votes cast: " + total);
            bw.newLine();

            // ---- Winner announcement ----
            Candidate winner = candidates.stream()
                    .max((a, b) -> Integer.compare(a.getVoteCount(), b.getVoteCount()))
                    .orElse(null);

            if (winner != null) {
                bw.newLine();
                bw.write("WINNER: " + winner.getName()
                        + " with " + winner.getVoteCount() + " vote(s)");
                bw.newLine();
            }

            AppLogger.info("Results written to " + RESULT_FILE);
        }
    }

    public static String getResultFile() { return RESULT_FILE; }
}
