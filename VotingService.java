package evoting.service;

import evoting.model.Candidate;
import evoting.model.Voter;
import evoting.util.AppLogger;
import evoting.util.FileResultWriter;

import java.io.IOException;
import java.util.*;

/**
 * Central business-logic service for the e-voting system.
 *
 * Responsibilities:
 *  - Manage the list of candidates and their vote tallies.
 *  - Accept voter registrations and prevent duplicate voting.
 *  - Delegate file-writing to FileResultWriter.
 *  - Provide query methods for the GUI.
 *
 * Requirement satisfied:
 *  - Code Refactoring   : business logic fully separated from GUI (MVC-ish).
 *  - Exception Handling : duplicate CNIC and unknown candidate throw
 *                         descriptive checked exceptions.
 */
public class VotingService {

    // ---------------------------------------------------------------
    // State
    // ---------------------------------------------------------------
    private final List<Candidate> candidates;
    private final List<Voter>     voters      = new ArrayList<>();
    private final Set<String>     usedCNICs   = new HashSet<>();

    private final int totalVoters;
    private int       votersProcessed = 0;

    // ---------------------------------------------------------------
    // Construction
    // ---------------------------------------------------------------

    public VotingService(int totalVoters, List<String> candidateNames) {
        if (totalVoters < 1) throw new IllegalArgumentException("Total voters must be at least 1.");
        if (candidateNames == null || candidateNames.isEmpty())
            throw new IllegalArgumentException("At least one candidate is required.");

        this.totalVoters = totalVoters;
        this.candidates  = new ArrayList<>();
        for (String name : candidateNames) {
            candidates.add(new Candidate(name));
        }

        AppLogger.info("VotingService initialised: totalVoters=" + totalVoters
                       + ", candidates=" + candidateNames);
    }

    // ---------------------------------------------------------------
    // Core operations
    // ---------------------------------------------------------------

    /**
     * Records a vote.
     *
     * @throws DuplicateCnicException  if the CNIC has already been used.
     * @throws UnknownCandidateException if the selected candidate is not in the list.
     */
    public void castVote(Voter voter) throws DuplicateCnicException, UnknownCandidateException {

        if (usedCNICs.contains(voter.getCnic())) {
            AppLogger.warning("Duplicate CNIC attempt: " + voter.getCnic());
            throw new DuplicateCnicException("CNIC " + voter.getCnic() + " has already voted.");
        }

        Candidate target = findCandidate(voter.getCandidateName());
        if (target == null) {
            AppLogger.warning("Unknown candidate: " + voter.getCandidateName());
            throw new UnknownCandidateException("Candidate '" + voter.getCandidateName() + "' not found.");
        }

        usedCNICs.add(voter.getCnic());
        voters.add(voter);
        target.incrementVote();
        votersProcessed++;

        AppLogger.info("Vote cast: voter=" + voter.getCnic()
                       + ", candidate=" + voter.getCandidateName()
                       + ", progress=" + votersProcessed + "/" + totalVoters);
    }

    /**
     * Persists results to result.txt.
     *
     * @throws IOException if file writing fails.
     */
    public void saveResults() throws IOException {
        FileResultWriter.write(voters, candidates);
    }

    // ---------------------------------------------------------------
    // Query helpers (used by GUI and tests)
    // ---------------------------------------------------------------

    public boolean isVotingComplete()     { return votersProcessed >= totalVoters; }
    public int     getVotersProcessed()   { return votersProcessed; }
    public int     getTotalVoters()       { return totalVoters; }
    public List<Candidate> getCandidates(){ return Collections.unmodifiableList(candidates); }
    public List<Voter>     getVoters()    { return Collections.unmodifiableList(voters); }

    /** Returns the candidate with the highest vote count, or null if no votes cast. */
    public Candidate getWinner() {
        return candidates.stream()
                .max(Comparator.comparingInt(Candidate::getVoteCount))
                .orElse(null);
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private Candidate findCandidate(String name) {
        return candidates.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    // ---------------------------------------------------------------
    // Custom checked exceptions
    // ---------------------------------------------------------------

    public static class DuplicateCnicException extends Exception {
        public DuplicateCnicException(String msg) { super(msg); }
    }

    public static class UnknownCandidateException extends Exception {
        public UnknownCandidateException(String msg) { super(msg); }
    }
}
