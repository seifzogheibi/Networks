package client;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import java.text.SimpleDateFormat;
import java.util.Date;



public class ClientHandler implements Runnable {
    private Socket socket;
    private static ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    static {
        votes.put("rabbit", 0);
        votes.put("squirrel", 0);
        votes.put("duck", 0);
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        ) {
            InetAddress inet = socket.getInetAddress();
            String request = reader.readLine();
            String response = processRequest(request);
            writer.println(response); // Send response to client
            System.out.println("Logging request: " + request + " from " + inet.getHostAddress()); // Debugging log
            logRequest(inet.getHostAddress(), request); // Log request
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String processRequest(String request) {
        if (request.equals("list")) {
            // Return all vote counts
            StringBuilder sb = new StringBuilder();
            votes.forEach((option, count) -> sb.append(option).append(" has ").append(count).append(" votes.\n"));
            return sb.length() > 0 ? sb.toString() : "No votes yet.";
        } else if (request.startsWith("vote ")) {
            String option = request.substring(5).trim();
            
            // Ensure the option exists before voting
            if (!votes.containsKey(option)) {
                return "Error: '" + option + "' is not a valid voting option.";
            }

            // 🔹 Thread-Safe Vote Update
            synchronized (votes) {
                votes.merge(option, 1, Integer::sum);
            }

            return "Vote for '" + option + "' counted.";
        } else {
            return "Invalid request.";
        }
    }

    /**
     * Logs client requests to log.txt in the format: date|time|client IP|request
     */
    private void logRequest(String clientIP, String request) {
        try {
            // 🔹 Ensure log.txt is created if missing
            File logFile = new File("log.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
    
            // 🔹 Write log entry
            BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true));
            String timestamp = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss").format(new Date());
            logWriter.write(timestamp + "|" + clientIP + "|" + request + "\n");
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}