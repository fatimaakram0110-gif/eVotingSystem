package evoting.model;

/**
 * Represents a candidate in the election.
 * Encapsulates candidate identity and vote accumulation.
 */
public class Candidate {

    private final String name;
    private int voteCount;

    public Candidate(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Candidate name cannot be blank.");
        this.name      = name;
        this.voteCount = 0;
    }

    public String getName()     { return name; }
    public int    getVoteCount(){ return voteCount; }

    /** Atomically increments this candidate's vote tally. */
    public void incrementVote() { voteCount++; }

    @Override
    public String toString() {
        return name + ": " + voteCount + " vote(s)";
    }
}
