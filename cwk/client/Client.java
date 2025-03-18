package client;

import java.io.*;
import java.net.*;
import java.util.*;

// public class Client
// {
// 	public static void main( String[] args )
// 	{
// 	}
// }

public class Client {
    public static void main(String[] args) {
        // Ensure the user provided a command argument
        if (args.length < 1) {
            System.out.println("Usage: java Client <command> [option]");
            return;
        }

        String serverAddress = "localhost";
        int serverPort = 7777;
        String command = args[0];

        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Build request string
            String request;
            if (command.equals("list")) {
                request = "list";
            } else if (command.equals("vote") && args.length == 2) {
                request = "vote " + args[1];
            } else {
                System.out.println("Invalid command. Use 'list' or 'vote <option>'.");
                return;
            }

            // Send request to server
            out.println(request);

            // Read response from server
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
            }

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}