package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import java.util.Date;

// lect 11 executor server model
public class Server {
    // hashmapping to store votes thread-safely
    private static ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        ExecutorService service = null;

        // make sure minimum 2 arguments (options) are provided in the command line
        // argument
        if (args.length < 2) {
            System.err.println("Bad command. How to use: java Server <option 1> <option 2> ...");
            System.exit(1);
        }

        // clear previously stored votes
        votes.clear();
        for (String option : args) {
            votes.put(option.toLowerCase(), 0);
        }

        // debugging
        System.out.println("server running with voting options:" + votes.keySet());

        // initialize server port for client connections
        try {
            serverSocket = new ServerSocket(7777);
            System.out.println("The server is running on port " + serverSocket.getLocalPort());

        } catch (IOException e) {
            System.err.println("Cannot listen on port " + serverSocket);
            System.exit(1);
        }

        // thread pool to handle many clients at once
        service = Executors.newFixedThreadPool(30);

        // looping through to accept new clients connections
        while (true) {
            Socket client = serverSocket.accept();
            service.submit(new HandleClient(client));
        }
    }

    // lect 10 kkc handler
    private static class HandleClient extends Thread {
        private Socket socket;

        public HandleClient(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // output/input stream to send/receive client data
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // read, log, process, then respond to client request and exit loop
                String inputLine, outputLine;
                while ((inputLine = in.readLine()) != null) {
                    logRequest(socket.getInetAddress().getHostAddress(), inputLine);
                    outputLine = processInput(socket, inputLine);
                    out.println(outputLine);
                    break;
                }

                // debbuging logging.
                InetAddress inet = socket.getInetAddress();
                Date date = new Date();
                System.out.println("\nDate " + date.toString());
                System.out.println("Connection made from " + inet.getHostName());

                out.close();
                in.close();
                socket.close();
            }

            catch (IOException e) {
                System.err.println("An error occured when handling the client request.");
                e.printStackTrace();
            }
        }
    }

    // some methods in this class is inspired by
    // https://stackoverflow.com/questions/51650024/java-client-server-client-with-synchronised-thread
    private static String processInput(Socket clientSocket, String input) {
        // take the arguments after 'vote '
        if (input.startsWith("vote ")) {
            String option = input.substring(5).trim().toLowerCase();
            // validate voting option
            if (!votes.containsKey(option)) {
                return "Cannot find option '" + option + "'.";
            }
            // thread-safe incrementation of votes
            synchronized (votes) {
                votes.put(option, votes.get(option) + 1);
            }
            return "Incremented the number of votes for '" + option + "'.";

            // if 'list' argument is requested, loop through all options and return them
        } else if (input.equals("list")) {
            StringBuilder result = new StringBuilder();
            for (String key : votes.keySet()) {
                result.append(key).append(": ").append(votes.get(key)).append(" votes\n");
            }
            return result.toString().trim();
        } else {
            return "Bad request. Only input 'vote <option>' or 'list'.";
        }
    }

    private static void logRequest(String clientIP, String request) {
        try {
            // create log.txt if it doesnt exist in the server directory
            File logFile = new File("log.txt");
            if (!logFile.exists())
                logFile.createNewFile();

            // write request details to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.write(new java.text.SimpleDateFormat("dd-MM-yyyy | HH:mm:ss").format(new Date()) + " | " + clientIP
                    + " | " + request + "\n");
            writer.close();
        } catch (IOException e) {
            System.err.println("An error occured when writing to log file.");
        }
    }

}
