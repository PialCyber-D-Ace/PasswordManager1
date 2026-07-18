package passwordmanager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Core business logic for managing passwords.
 */
public class PasswordManager {
    private final Database db;

    public PasswordManager(Database db) {
        this.db = db;
    }

    public int add(String email, String websiteName, String websiteUrl,
                   String username, String password, String notes, String category) {
        Password p = new Password(0, email, websiteName, websiteUrl,
                                  username, password, notes, category);
        return db.addPassword(p);
    }

    public List<Password> getAll(String email) {
        return db.getPasswordsForUser(email);
    }

    public List<Password> search(String email, String query) {
        if (query == null || query.trim().isEmpty()) return getAll(email);
        String q = query.trim().toLowerCase();
        return getAll(email).stream()
                .filter(p -> p.getWebsiteName().toLowerCase().contains(q)
                          || p.getUsername().toLowerCase().contains(q)
                          || p.getWebsiteUrl().toLowerCase().contains(q)
                          || p.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public Password getById(String email, int id) {
        return db.getPasswordById(id, email);
    }

    public boolean update(int id, String email, String websiteName, String websiteUrl,
                          String username, String password, String notes, String category) {
        Password existing = db.getPasswordById(id, email);
        if (existing == null) return false;
        existing.setWebsiteName(websiteName);
        existing.setWebsiteUrl(websiteUrl);
        existing.setUsername(username);
        existing.setPassword(password);
        existing.setNotes(notes);
        existing.setCategory(category);
        return db.updatePassword(existing);
    }

    public boolean delete(String email, int id) {
        return db.deletePassword(id, email);
    }
}