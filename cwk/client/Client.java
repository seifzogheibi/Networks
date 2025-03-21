package client;

import java.io.*;
import java.net.*;

public class Client {

    private Socket socket = null;
    private PrintWriter socketOutput = null;
    private BufferedReader socketInput = null;

    public void runClient(String[] args) {
        try {
            // connect to server
            socket = new Socket("localhost", 7777);

            // initialize input/output streams to read responses and send requests (to/from
            // server)
            socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOutput = new PrintWriter(socket.getOutputStream(), true);

            // error statements
        } catch (UnknownHostException e) {
            System.err.println("Unknown host.\n");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't connect to host or establish I/O connection.\n");
            System.exit(1);
        }

        // validate first command ('vote' or 'list')
        String first_arg = args[0];
        // store request
        String request;
        if (first_arg.equals("list")) {
            request = "list";
        } else if (first_arg.equals("vote") && args.length == 2) {
            request = "vote " + args[1];
        } else {
            System.out.println("Bad command. How to use: 'java Client list' OR 'java Client vote <option>'");
            return;
        }

        // send request to server and show response
        try {
            socketOutput.println(request);
            String fromServer;
            while ((fromServer = socketInput.readLine()) != null) {
                System.out.println("Server: " + fromServer);
            }

            // close streams and exit program after every response
            socketOutput.close();
            socketInput.close();
            socket.close();
            System.exit(0);

        } catch (IOException e) {
            System.err.println("Cannot connect from server.");
            System.exit(1);
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.runClient(args);
    }
}
