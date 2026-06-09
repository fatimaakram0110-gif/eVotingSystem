package evoting.validation;

import java.util.regex.Pattern;

/**
 * Centralised validation logic for voter input fields.
 *
 * Requirement satisfied:
 *  - Code Refactoring  : validation extracted into its own class (SRP).
 *  - Exception Handling: returns structured ValidationResult instead of
 *                        throwing, so callers can compose multiple checks.
 */
public class InputValidator {

    // ---------------------------------------------------------------
    // Compiled patterns (cheaper than re-compiling on every call)
    // ---------------------------------------------------------------
    private static final Pattern NAME_PATTERN  = Pattern.compile("^[A-Za-z ]+$");
    private static final Pattern CNIC_PATTERN  = Pattern.compile("^\\d{5}-\\d{7}-\\d{1}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // ---------------------------------------------------------------
    // Public validation methods – each returns a ValidationResult
    // ---------------------------------------------------------------

    public ValidationResult validateName(String name) {
        if (name == null || name.isBlank())
            return ValidationResult.fail("Name is required.");
        if (!NAME_PATTERN.matcher(name).matches())
            return ValidationResult.fail("Name must contain letters and spaces only.");
        return ValidationResult.ok();
    }

    public ValidationResult validateCnic(String cnic) {
        if (cnic == null || cnic.isBlank())
            return ValidationResult.fail("CNIC is required.");
        if (!CNIC_PATTERN.matcher(cnic).matches())
            return ValidationResult.fail("Invalid CNIC format. Use XXXXX-XXXXXXX-X.");
        return ValidationResult.ok();
    }

    public ValidationResult validateAddress(String address) {
        if (address == null || address.isBlank())
            return ValidationResult.fail("Address is required.");
        return ValidationResult.ok();
    }

    public ValidationResult validateEmail(String email) {
        if (email == null || email.isBlank())
            return ValidationResult.fail("Email is required.");
        if (!EMAIL_PATTERN.matcher(email).matches())
            return ValidationResult.fail("Invalid email format. Example: user@example.com");
        return ValidationResult.ok();
    }

    public ValidationResult validateCandidate(String candidate) {
        if (candidate == null || candidate.equals("Select Candidate") || candidate.isBlank())
            return ValidationResult.fail("Please select a candidate.");
        return ValidationResult.ok();
    }

    // ---------------------------------------------------------------
    // Convenience: validate all fields at once
    // ---------------------------------------------------------------

    public ValidationResult validateAll(String name, String cnic, String address,
                                         String email, String candidate) {
        ValidationResult r;
        r = validateName(name);    if (!r.isValid()) return r;
        r = validateCnic(cnic);    if (!r.isValid()) return r;
        r = validateAddress(address); if (!r.isValid()) return r;
        r = validateEmail(email);  if (!r.isValid()) return r;
        r = validateCandidate(candidate); if (!r.isValid()) return r;
        return ValidationResult.ok();
    }

    // ---------------------------------------------------------------
    // Inner value-object for validation outcomes
    // ---------------------------------------------------------------

    public static final class ValidationResult {
        private final boolean valid;
        private final String  message;

        private ValidationResult(boolean valid, String message) {
            this.valid   = valid;
            this.message = message;
        }

        public static ValidationResult ok()           { return new ValidationResult(true,  ""); }
        public static ValidationResult fail(String m) { return new ValidationResult(false, m); }

        public boolean isValid()    { return valid; }
        public String  getMessage() { return message; }
    }
}
