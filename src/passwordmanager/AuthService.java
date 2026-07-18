package passwordmanager;

/**
 * Handles user authentication logic.
 */
public class AuthService {
    private final Database db;

    public AuthService(Database db) {
        this.db = db;
    }

    /**
     * Register a new user.
     * @return true if registration succeeded, false if email already exists.
     */
    public boolean register(String fullName, String email, String masterPassword) {
        if (fullName == null || email == null || masterPassword == null) return false;
        if (fullName.trim().isEmpty() || email.trim().isEmpty() || masterPassword.length() < 6)
            return false;
        User user = new User(fullName.trim(), email.trim().toLowerCase(), masterPassword);
        return db.registerUser(user);
    }

    /**
     * Authenticate a user.
     * @return the User object if credentials are valid, else null.
     */
    public User login(String email, String masterPassword) {
        if (email == null || masterPassword == null) return null;
        User user = db.getUser(email.trim().toLowerCase());
        if (user == null) return null;
        if (user.getMasterPassword().equals(masterPassword)) return user;
        return null;
    }

    /**
     * Change master password.
     */
    public boolean changePassword(String email, String oldPass, String newPass) {
        User user = db.getUser(email);
        if (user == null) return false;
        if (!user.getMasterPassword().equals(oldPass)) return false;
        if (newPass == null || newPass.length() < 6) return false;
        user.setMasterPassword(newPass);
        return db.updateUser(user);
    }
}