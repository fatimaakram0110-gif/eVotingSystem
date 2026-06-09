package evoting;

import evoting.model.Candidate;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Candidate model.
 *
 * Verifies:
 *  - Initial vote count is zero.
 *  - incrementVote() increases count by exactly 1 each time.
 *  - Constructor rejects blank/null names.
 *  - toString() contains name and vote count.
 */
@DisplayName("Candidate Model Tests")
class CandidateTest {

    @Test
    @DisplayName("Initial vote count is zero")
    void testInitialVoteCount() {
        Candidate c = new Candidate("Test Candidate");
        assertEquals(0, c.getVoteCount(), "Initial vote count must be 0");
    }

    @Test
    @DisplayName("incrementVote increases count by 1")
    void testIncrementVote() {
        Candidate c = new Candidate("Test Candidate");
        c.incrementVote();
        assertEquals(1, c.getVoteCount());
        c.incrementVote();
        assertEquals(2, c.getVoteCount());
    }

    @Test
    @DisplayName("getName returns the name supplied at construction")
    void testGetName() {
        Candidate c = new Candidate("Nawaz Sharif");
        assertEquals("Nawaz Sharif", c.getName());
    }

    @Test
    @DisplayName("toString contains name and vote count")
    void testToString() {
        Candidate c = new Candidate("Ali");
        c.incrementVote();
        String s = c.toString();
        assertTrue(s.contains("Ali"),   "toString must contain candidate name");
        assertTrue(s.contains("1"),     "toString must contain vote count");
    }

    @Test
    @DisplayName("Blank name throws IllegalArgumentException")
    void testBlankNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Candidate("  "));
    }

    @Test
    @DisplayName("Null name throws IllegalArgumentException")
    void testNullNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Candidate(null));
    }
}
