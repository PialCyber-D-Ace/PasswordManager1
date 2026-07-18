package passwordmanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Password model — represents a saved credential.
 */
public class Password {
    private int id;
    private String userEmail;
    private String websiteName;
    private String websiteUrl;
    private String username;
    private String password;
    private String notes;
    private String category;
    private String createdAt;

    public Password() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Password(int id, String userEmail, String websiteName, String websiteUrl,
                    String username, String password, String notes, String category) {
        this.id = id;
        this.userEmail = userEmail;
        this.websiteName = websiteName;
        this.websiteUrl = websiteUrl;
        this.username = username;
        this.password = password;
        this.notes = notes;
        this.category = category;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getWebsiteName() { return websiteName; }
    public void setWebsiteName(String websiteName) { this.websiteName = websiteName; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCreatedAt() { return createdAt; }

    // Serialize: id|email|name|url|user|pass|notes|cat|date
    public String serialize() {
        return id + "|" + userEmail + "|" + websiteName + "|" + websiteUrl + "|"
             + username + "|" + password + "|" + notes + "|" + category + "|" + createdAt;
    }

    public static Password deserialize(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 9) return null;
        Password pw = new Password();
        pw.id = Integer.parseInt(p[0]);
        pw.userEmail = p[1];
        pw.websiteName = p[2];
        pw.websiteUrl = p[3];
        pw.username = p[4];
        pw.password = p[5];
        pw.notes = p[6];
        pw.category = p[7];
        pw.createdAt = p[8];
        return pw;
    }
}