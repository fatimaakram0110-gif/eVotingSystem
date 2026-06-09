package evoting;

import evoting.model.Candidate;
import evoting.model.Voter;
import evoting.service.VotingService;
import evoting.service.VotingService.DuplicateCnicException;
import evoting.service.VotingService.UnknownCandidateException;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VotingService core business logic.
 *
 * Requirement satisfied:
 *  - Unit Testing: automated tests for vote casting, duplicate detection,
 *                  unknown candidate handling, winner determination, and
 *                  completion tracking.
 */
@DisplayName("VotingService Tests")
class VotingServiceTest {

    private static final List<String> CANDIDATES =
            List.of("Nawaz Sharif", "Imran Khan", "Bilawal Bhutto Zardari");

    private VotingService service;

    @BeforeEach
    void setUp() {
        service = new VotingService(3, CANDIDATES);
    }

    // ---------------------------------------------------------------
    // Happy path
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Casting a valid vote increments vote count")
    void testCastVoteIncrementsCount()
            throws DuplicateCnicException, UnknownCandidateException {
        Voter v = new Voter("Ali", "35202-1234567-8", "Lahore", "ali@t.com", "Imran Khan");
        service.castVote(v);

        Candidate c = service.getCandidates().stream()
                .filter(x -> x.getName().equals("Imran Khan"))
                .findFirst().orElse(null);

        assertNotNull(c);
        assertEquals(1, c.getVoteCount(), "Vote count should be 1 after one vote");
    }

    @Test
    @DisplayName("Voters processed counter advances after each vote")
    void testVotersProcessedAdvances()
            throws DuplicateCnicException, UnknownCandidateException {
        assertEquals(0, service.getVotersProcessed(), "Should start at 0");

        service.castVote(new Voter("A", "11111-1111111-1", "Addr", "a@b.com", "Nawaz Sharif"));
        assertEquals(1, service.getVotersProcessed());

        service.castVote(new Voter("B", "22222-2222222-2", "Addr", "b@b.com", "Imran Khan"));
        assertEquals(2, service.getVotersProcessed());
    }

    @Test
    @DisplayName("isVotingComplete returns true when all voters have voted")
    void testIsVotingComplete()
            throws DuplicateCnicException, UnknownCandidateException {
        assertFalse(service.isVotingComplete());
        service.castVote(new Voter("A", "11111-1111111-1", "A", "a@b.com", "Nawaz Sharif"));
        service.castVote(new Voter("B", "22222-2222222-2", "B", "b@b.com", "Imran Khan"));
        assertFalse(service.isVotingComplete(), "Not complete yet – 3 voters needed");
        service.castVote(new Voter("C", "33333-3333333-3", "C", "c@b.com", "Nawaz Sharif"));
        assertTrue(service.isVotingComplete(), "Voting should be complete after 3 votes");
    }

    // ---------------------------------------------------------------
    // Duplicate CNIC
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Duplicate CNIC throws DuplicateCnicException")
    void testDuplicateCnicThrows()
            throws DuplicateCnicException, UnknownCandidateException {
        Voter v1 = new Voter("Ali",  "35202-1234567-8", "Lahore", "ali@t.com", "Imran Khan");
        Voter v2 = new Voter("Sara", "35202-1234567-8", "Karachi","sara@t.com","Nawaz Sharif");

        service.castVote(v1);
        assertThrows(DuplicateCnicException.class, () -> service.castVote(v2),
                "Second use of same CNIC must throw DuplicateCnicException");
    }

    @Test
    @DisplayName("Duplicate CNIC does NOT increment vote count")
    void testDuplicateCnicDoesNotIncrementVote()
            throws DuplicateCnicException, UnknownCandidateException {
        Voter v1 = new Voter("Ali",  "35202-1234567-8", "Lahore", "ali@t.com", "Imran Khan");
        Voter v2 = new Voter("Sara", "35202-1234567-8", "Karachi","sara@t.com","Nawaz Sharif");

        service.castVote(v1);
        try { service.castVote(v2); } catch (DuplicateCnicException ignored) {}

        // Imran Khan should still have exactly 1 vote
        Candidate ik = service.getCandidates().stream()
                .filter(x -> x.getName().equals("Imran Khan")).findFirst().orElseThrow();
        assertEquals(1, ik.getVoteCount(), "Duplicate vote must not be counted");
        assertEquals(1, service.getVotersProcessed(), "Processed count must not increase");
    }

    // ---------------------------------------------------------------
    // Unknown candidate
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Voting for unknown candidate throws UnknownCandidateException")
    void testUnknownCandidateThrows() {
        Voter v = new Voter("Ali", "35202-1234567-8", "Lahore", "ali@t.com", "Ghost Party");
        assertThrows(UnknownCandidateException.class, () -> service.castVote(v),
                "Unknown candidate must throw UnknownCandidateException");
    }

    // ---------------------------------------------------------------
    // Winner determination
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getWinner returns the candidate with the highest vote count")
    void testGetWinner()
            throws DuplicateCnicException, UnknownCandidateException {
        service.castVote(new Voter("A","11111-1111111-1","A","a@b.com","Nawaz Sharif"));
        service.castVote(new Voter("B","22222-2222222-2","B","b@b.com","Nawaz Sharif"));
        service.castVote(new Voter("C","33333-3333333-3","C","c@b.com","Imran Khan"));

        Candidate winner = service.getWinner();
        assertNotNull(winner);
        assertEquals("Nawaz Sharif", winner.getName(), "Nawaz Sharif should win with 2 votes");
    }

    @Test
    @DisplayName("getWinner returns a candidate even with zero votes (first in list)")
    void testGetWinnerNoVotes() {
        Candidate winner = service.getWinner();
        assertNotNull(winner, "getWinner should never return null");
    }

    // ---------------------------------------------------------------
    // Constructor guard clauses
    // ---------------------------------------------------------------

    @Test
    @DisplayName("VotingService rejects zero voters")
    void testZeroVotersThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new VotingService(0, CANDIDATES));
    }

    @Test
    @DisplayName("VotingService rejects empty candidate list")
    void testEmptyCandidatesThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new VotingService(1, List.of()));
    }

    // ---------------------------------------------------------------
    // Voter model guard clauses
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Voter rejects blank name")
    void testVoterBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Voter("", "35202-1234567-8", "Addr", "a@b.com", "Imran Khan"));
    }

    @Test
    @DisplayName("Voter rejects null CNIC")
    void testVoterNullCnic() {
        assertThrows(IllegalArgumentException.class,
                () -> new Voter("Ali", null, "Addr", "a@b.com", "Imran Khan"));
    }
}
