package passwordmanager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.*;
import java.util.*;

public class Server {
    private final int port;
    private final String frontendPath;
    private final Database db;
    private final AuthService auth;
    private final PasswordManager pm;

    public Server(int port, String frontendPath) {
        this.port = port;
        this.frontendPath = frontendPath;
        this.db = new Database();
        this.auth = new AuthService(db);
        this.pm = new PasswordManager(db);
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Routes
        server.createContext("/api/register", this::handleRegister);
        server.createContext("/api/login", this::handleLogin);
        server.createContext("/api/passwords", this::handlePasswords);
        server.createContext("/api/profile", this::handleProfile);
        
        // Static Files (HTML, CSS, JS)
        server.createContext("/", this::handleStatic);

        server.setExecutor(null);
        server.start();
        System.out.println("==========================================");
        System.out.println("  ✅ Password Manager Server Running");
        System.out.println("  🌐 Open: http://localhost:" + port);
        System.out.println("==========================================");
    }

    // --- STATIC FILES HANDLER ---
    private void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        // Remove leading slash to match folder structure
        String filePath = path.substring(1);
        Path file = Paths.get(frontendPath, filePath).normalize();

        // Security: Prevent accessing files outside the frontend folder
        if (!file.startsWith(Paths.get(frontendPath).toAbsolutePath())) {
            send(ex, 403, "Forbidden");
            return;
        }

        // If file doesn't exist, return 404
        if (!Files.exists(file)) {
            send(ex, 404, "Not Found: " + filePath);
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(file);
            String mime = guessMime(path);
            ex.getResponseHeaders().set("Content-Type", mime);
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            send(ex, 500, "Internal Server Error");
        }
    }

    private String guessMime(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css"))  return "text/css; charset=utf-8";
        if (path.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        if (path.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }

    // --- API HANDLERS ---
    private void enableCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes("UTF-8");
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private Map<String, String> parseBody(HttpExchange ex) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ex.getRequestBody(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return parseQuery(sb.toString());
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            try {
                String k = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String v = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                map.put(k, v);
            } catch (UnsupportedEncodingException | IllegalArgumentException ignored) {}
        }
        return map;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private void handleRegister(HttpExchange ex) throws IOException {
        enableCors(ex);
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 204, ""); return; }
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 405, "{\"error\":\"Method not allowed\"}"); return; }

        Map<String, String> body = parseBody(ex);
        if (auth.register(body.get("fullName"), body.get("email"), body.get("password"))) {
            send(ex, 200, "{\"success\":true,\"message\":\"Registered successfully\"}");
        } else {
            send(ex, 400, "{\"success\":false,\"message\":\"Email already registered or invalid data\"}");
        }
    }

    private void handleLogin(HttpExchange ex) throws IOException {
        enableCors(ex);
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 204, ""); return; }
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 405, "{\"error\":\"Method not allowed\"}"); return; }

        Map<String, String> body = parseBody(ex);
        User u = auth.login(body.get("email"), body.get("password"));
        if (u != null) {
            send(ex, 200, "{\"success\":true,\"fullName\":\"" + escape(u.getFullName()) + "\",\"email\":\"" + escape(u.getEmail()) + "\"}");
        } else {
            send(ex, 401, "{\"success\":false,\"message\":\"Invalid email or password\"}");
        }
    }

    private void handlePasswords(HttpExchange ex) throws IOException {
    enableCors(ex);
    if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 204, ""); return; }

    String method = ex.getRequestMethod();
    
    // Parse query parameters (for GET and DELETE)
    String queryString = ex.getRequestURI().getQuery();
    Map<String, String> queryParams = parseQuery(queryString);
    
    // For POST and PUT, we need to parse the body
    Map<String, String> body = new HashMap<>();
    if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
        body = parseBody(ex);
    }
    
    // Get email from query params OR body (whichever has it)
    String email = queryParams.get("email");
    if (email == null || email.isEmpty()) {
        email = body.get("email");
    }
    
    if (email == null || email.isEmpty()) {
        send(ex, 400, "{\"success\":false,\"message\":\"Email required\"}");
        return;
    }

    switch (method) {
        case "GET" -> {
            String q = queryParams.get("q");
            List<Password> list = (q != null) ? pm.search(email, q) : pm.getAll(email);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Password p = list.get(i);
                sb.append("{\"id\":").append(p.getId())
                  .append(",\"websiteName\":\"").append(escape(p.getWebsiteName())).append("\"")
                  .append(",\"websiteUrl\":\"").append(escape(p.getWebsiteUrl())).append("\"")
                  .append(",\"username\":\"").append(escape(p.getUsername())).append("\"")
                  .append(",\"password\":\"").append(escape(p.getPassword())).append("\"")
                  .append(",\"notes\":\"").append(escape(p.getNotes())).append("\"")
                  .append(",\"category\":\"").append(escape(p.getCategory())).append("\"")
                  .append(",\"createdAt\":\"").append(escape(p.getCreatedAt())).append("\"}");
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            send(ex, 200, sb.toString());
        }
        case "POST" -> {
            int id = pm.add(email, 
                    body.getOrDefault("websiteName",""), 
                    body.getOrDefault("websiteUrl",""),
                    body.getOrDefault("username",""), 
                    body.getOrDefault("password",""),
                    body.getOrDefault("notes",""), 
                    body.getOrDefault("category","General"));
            send(ex, 200, "{\"success\":true,\"id\":" + id + "}");
        }
        case "PUT" -> {
            int id = Integer.parseInt(body.getOrDefault("id", "0"));
            boolean ok = pm.update(id, email, 
                    body.getOrDefault("websiteName",""), 
                    body.getOrDefault("websiteUrl",""),
                    body.getOrDefault("username",""), 
                    body.getOrDefault("password",""),
                    body.getOrDefault("notes",""), 
                    body.getOrDefault("category","General"));
            send(ex, ok ? 200 : 404, "{\"success\":" + ok + "}");
        }
        case "DELETE" -> {
            int id = Integer.parseInt(queryParams.getOrDefault("id", "0"));
            boolean ok = pm.delete(email, id);
            send(ex, ok ? 200 : 404, "{\"success\":" + ok + "}");
        }
        default -> send(ex, 405, "{\"error\":\"Method not allowed\"}");
    }
}

    private void handleProfile(HttpExchange ex) throws IOException {
        enableCors(ex);
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 204, ""); return; }
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 405, "{\"error\":\"Method not allowed\"}"); return; }

        Map<String, String> body = parseBody(ex);
        boolean ok = auth.changePassword(body.get("email"), body.get("oldPassword"), body.get("newPassword"));
        send(ex, ok ? 200 : 400, "{\"success\":" + ok + ",\"message\":\"" + (ok ? "Password changed" : "Old password incorrect") + "\"}");
    }
}