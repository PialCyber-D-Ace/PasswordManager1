package passwordmanager;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Entry point - starts the HTTP server.
 */
public class Main {
    public static void main(String[] args) {
        int port = 8080;
        
        // Get the current working directory and append "frontend"
        String frontendPath = Paths.get("frontend").toAbsolutePath().toString();
        
        System.out.println("Frontend path: " + frontendPath);

        try {
            Server server = new Server(port, frontendPath);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}