package evoting.model;

/**
 * Represents a voter in the e-voting system.
 * Encapsulates all voter data with proper encapsulation (OOP principle).
 */
public class Voter {

    private final String name;
    private final String cnic;
    private final String address;
    private final String email;
    private final String candidateName;

    public Voter(String name, String cnic, String address, String email, String candidateName) {
        if (name == null || name.isBlank())       throw new IllegalArgumentException("Name cannot be blank.");
        if (cnic == null || cnic.isBlank())       throw new IllegalArgumentException("CNIC cannot be blank.");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Address cannot be blank.");
        if (email == null || email.isBlank())     throw new IllegalArgumentException("Email cannot be blank.");
        if (candidateName == null || candidateName.isBlank()) throw new IllegalArgumentException("Candidate cannot be blank.");

        this.name          = name;
        this.cnic          = cnic;
        this.address       = address;
        this.email         = email;
        this.candidateName = candidateName;
    }

    public String getName()          { return name; }
    public String getCnic()          { return cnic; }
    public String getAddress()       { return address; }
    public String getEmail()         { return email; }
    public String getCandidateName() { return candidateName; }

    @Override
    public String toString() {
        return String.format(
            "Name: %s%nCNIC: %s%nAddress: %s%nEmail: %s%nVoted For: %s%n",
            name, cnic, address, email, candidateName
        );
    }
}
