package passwordmanager;

/**
 * User model — represents a registered user.
 */
public class User {
    private String fullName;
    private String email;
    private String masterPassword;

    public User() {}

    public User(String fullName, String email, String masterPassword) {
        this.fullName = fullName;
        this.email = email;
        this.masterPassword = masterPassword;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMasterPassword() { return masterPassword; }
    public void setMasterPassword(String masterPassword) { this.masterPassword = masterPassword; }

    // Serialize to pipe-delimited string for storage
    public String serialize() {
        return fullName + "|" + email + "|" + masterPassword;
    }

    // Deserialize from pipe-delimited string
    public static User deserialize(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 3) return null;
        return new User(parts[0], parts[1], parts[2]);
    }
}