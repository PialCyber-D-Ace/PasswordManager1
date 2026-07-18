package passwordmanager;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * File-based storage — handles users and passwords persistence.
 */
public class Database {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.txt";
    private static final String PASSWORDS_FILE = DATA_DIR + "/passwords.txt";

    public Database() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            if (!Files.exists(Paths.get(USERS_FILE))) Files.createFile(Paths.get(USERS_FILE));
            if (!Files.exists(Paths.get(PASSWORDS_FILE))) Files.createFile(Paths.get(PASSWORDS_FILE));
        } catch (IOException e) {
            System.err.println("Error initializing data directory: " + e.getMessage());
        }
    }

    // ============ USER OPERATIONS ============
    public boolean userExists(String email) {
        try {
            for (String line : Files.readAllLines(Paths.get(USERS_FILE))) {
                User u = User.deserialize(line);
                if (u != null && u.getEmail().equalsIgnoreCase(email)) return true;
            }
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    public boolean registerUser(User user) {
        if (userExists(user.getEmail())) return false;
        try (FileWriter fw = new FileWriter(USERS_FILE, true)) {
            fw.write(user.serialize() + "\n");
            return true;
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    public User getUser(String email) {
        try {
            for (String line : Files.readAllLines(Paths.get(USERS_FILE))) {
                User u = User.deserialize(line);
                if (u != null && u.getEmail().equalsIgnoreCase(email)) return u;
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateUser(User updated) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
            List<String> newLines = new ArrayList<>();
            boolean found = false;
            for (String line : lines) {
                User u = User.deserialize(line);
                if (u != null && u.getEmail().equalsIgnoreCase(updated.getEmail())) {
                    newLines.add(updated.serialize());
                    found = true;
                } else {
                    newLines.add(line);
                }
            }
            if (found) Files.write(Paths.get(USERS_FILE), newLines);
            return found;
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    // ============ PASSWORD OPERATIONS ============
    public List<Password> getPasswordsForUser(String email) {
        List<Password> list = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(Paths.get(PASSWORDS_FILE))) {
                Password p = Password.deserialize(line);
                if (p != null && p.getUserEmail().equalsIgnoreCase(email)) list.add(p);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public synchronized int addPassword(Password p) {
        int newId = getNextId();
        p.setId(newId);
        try (FileWriter fw = new FileWriter(PASSWORDS_FILE, true)) {
            fw.write(p.serialize() + "\n");
            return newId;
        } catch (IOException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean updatePassword(Password updated) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PASSWORDS_FILE));
            List<String> newLines = new ArrayList<>();
            boolean found = false;
            for (String line : lines) {
                Password p = Password.deserialize(line);
                if (p != null && p.getId() == updated.getId()
                        && p.getUserEmail().equalsIgnoreCase(updated.getUserEmail())) {
                    newLines.add(updated.serialize());
                    found = true;
                } else {
                    newLines.add(line);
                }
            }
            if (found) Files.write(Paths.get(PASSWORDS_FILE), newLines);
            return found;
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deletePassword(int id, String email) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PASSWORDS_FILE));
            List<String> newLines = new ArrayList<>();
            boolean deleted = false;
            for (String line : lines) {
                Password p = Password.deserialize(line);
                if (p != null && p.getId() == id && p.getUserEmail().equalsIgnoreCase(email)) {
                    deleted = true;
                    continue;
                }
                newLines.add(line);
            }
            if (deleted) Files.write(Paths.get(PASSWORDS_FILE), newLines);
            return deleted;
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    public Password getPasswordById(int id, String email) {
        try {
            for (String line : Files.readAllLines(Paths.get(PASSWORDS_FILE))) {
                Password p = Password.deserialize(line);
                if (p != null && p.getId() == id && p.getUserEmail().equalsIgnoreCase(email))
                    return p;
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    private int getNextId() {
        int max = 0;
        try {
            for (String line : Files.readAllLines(Paths.get(PASSWORDS_FILE))) {
                Password p = Password.deserialize(line);
                if (p != null && p.getId() > max) max = p.getId();
            }
        } catch (IOException e) { e.printStackTrace(); }
        return max + 1;
    }
}