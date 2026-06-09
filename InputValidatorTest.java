package evoting;

import evoting.validation.InputValidator;
import evoting.validation.InputValidator.ValidationResult;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InputValidator.
 *
 * Requirement satisfied:
 *  - Unit Testing: automated test cases for the validation business logic.
 *
 * Each test is annotated with what it verifies.
 */
@DisplayName("InputValidator Tests")
class InputValidatorTest {

    private InputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InputValidator();
    }

    // ---------------------------------------------------------------
    // Name validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Valid name with letters and spaces passes")
    void testValidName() {
        ValidationResult r = validator.validateName("Nawaz Sharif");
        assertTrue(r.isValid(), "Expected valid name to pass");
    }

    @Test
    @DisplayName("Name with digits is rejected")
    void testNameWithDigits() {
        ValidationResult r = validator.validateName("Alice123");
        assertFalse(r.isValid(), "Name with digits should fail");
        assertFalse(r.getMessage().isBlank(), "Error message should not be blank");
    }

    @Test
    @DisplayName("Empty name is rejected")
    void testEmptyName() {
        ValidationResult r = validator.validateName("  ");
        assertFalse(r.isValid(), "Blank name should fail");
    }

    @Test
    @DisplayName("Null name is rejected")
    void testNullName() {
        ValidationResult r = validator.validateName(null);
        assertFalse(r.isValid(), "Null name should fail");
    }

    // ---------------------------------------------------------------
    // CNIC validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Well-formed CNIC passes")
    void testValidCnic() {
        ValidationResult r = validator.validateCnic("35202-1234567-8");
        assertTrue(r.isValid(), "Valid CNIC should pass");
    }

    @Test
    @DisplayName("CNIC without dashes is rejected")
    void testCnicNoDashes() {
        ValidationResult r = validator.validateCnic("3520212345678");
        assertFalse(r.isValid(), "CNIC without dashes should fail");
    }

    @Test
    @DisplayName("CNIC with wrong segment lengths is rejected")
    void testCnicWrongSegments() {
        ValidationResult r = validator.validateCnic("352-1234567-8");
        assertFalse(r.isValid(), "Malformed CNIC should fail");
    }

    @Test
    @DisplayName("Empty CNIC is rejected")
    void testEmptyCnic() {
        ValidationResult r = validator.validateCnic("");
        assertFalse(r.isValid(), "Empty CNIC should fail");
    }

    // ---------------------------------------------------------------
    // Email validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Standard email passes")
    void testValidEmail() {
        ValidationResult r = validator.validateEmail("user@example.com");
        assertTrue(r.isValid(), "Standard email should pass");
    }

    @Test
    @DisplayName("Email with plus-addressing passes")
    void testEmailWithPlus() {
        ValidationResult r = validator.validateEmail("user+tag@domain.org");
        assertTrue(r.isValid(), "Plus-address email should pass");
    }

    @Test
    @DisplayName("Email without @ is rejected")
    void testEmailNoAt() {
        ValidationResult r = validator.validateEmail("userexample.com");
        assertFalse(r.isValid(), "Email without @ should fail");
    }

    @Test
    @DisplayName("Email without domain extension is rejected")
    void testEmailNoDomainExtension() {
        ValidationResult r = validator.validateEmail("user@domain");
        assertFalse(r.isValid(), "Email without domain extension should fail");
    }

    @Test
    @DisplayName("Empty email is rejected")
    void testEmptyEmail() {
        ValidationResult r = validator.validateEmail("   ");
        assertFalse(r.isValid(), "Blank email should fail");
    }

    // ---------------------------------------------------------------
    // Candidate validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Non-placeholder candidate passes")
    void testValidCandidate() {
        ValidationResult r = validator.validateCandidate("Imran Khan");
        assertTrue(r.isValid(), "Real candidate name should pass");
    }

    @Test
    @DisplayName("Placeholder 'Select Candidate' is rejected")
    void testPlaceholderCandidate() {
        ValidationResult r = validator.validateCandidate("Select Candidate");
        assertFalse(r.isValid(), "Placeholder should be rejected");
    }

    @Test
    @DisplayName("Null candidate is rejected")
    void testNullCandidate() {
        ValidationResult r = validator.validateCandidate(null);
        assertFalse(r.isValid(), "Null candidate should fail");
    }

    // ---------------------------------------------------------------
    // Address validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Non-empty address passes")
    void testValidAddress() {
        ValidationResult r = validator.validateAddress("Lahore, Punjab");
        assertTrue(r.isValid(), "Non-empty address should pass");
    }

    @Test
    @DisplayName("Empty address is rejected")
    void testEmptyAddress() {
        ValidationResult r = validator.validateAddress("");
        assertFalse(r.isValid(), "Empty address should fail");
    }

    // ---------------------------------------------------------------
    // validateAll convenience method
    // ---------------------------------------------------------------

    @Test
    @DisplayName("validateAll passes when all fields are valid")
    void testValidateAllPasses() {
        ValidationResult r = validator.validateAll(
                "Ali Hassan", "35202-1234567-8",
                "Lahore", "ali@test.com", "Imran Khan");
        assertTrue(r.isValid(), "All valid fields should pass validateAll");
    }

    @Test
    @DisplayName("validateAll fails on first invalid field (CNIC)")
    void testValidateAllFailsOnCnic() {
        ValidationResult r = validator.validateAll(
                "Ali Hassan", "BADCNIC",
                "Lahore", "ali@test.com", "Imran Khan");
        assertFalse(r.isValid(), "Invalid CNIC should cause validateAll to fail");
        assertNotNull(r.getMessage(), "Error message should not be null");
    }
}
