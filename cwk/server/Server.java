// package server;

// import java.net.*;
// import java.io.*;
// import java.util.concurrent.*;

// import client.ClientHandler; // ✅ Import ClientHandler from the client package

// public class Server {
//     private static ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();
//      public static void main ( String [] args ) throws IOException {
//      ServerSocket serverSocket = null ;
//      ExecutorService service = null ;
    
//      if (args.length < 2) {
//         System.err.println("At least two voting options must be provided.");
//         System.exit(1);
//     }

//     votes.clear();

//     // lect 10
//     // synchronized( System.out ) {
//     //     System.out.print("Socket host: " + so.getInetAddress());
//     //     System.out.print(" on port: " + so.getPort());
//     //     System.out.println();
//     //     }

//      try {
//      serverSocket = new ServerSocket (7777) ;
//      System.out.println("The server is running on port 7777:");
//      service = Executors . newCachedThreadPool () ;
//      } catch ( IOException e ) {
//         System . err . println ("Cannot listen on port 7777.") ;
//         System . exit (1) ;
//         }
     
    
//      while ( true )
//      {
//      Socket client = serverSocket . accept () ;
//      service . submit ( new ClientHandler ( client ) ) ;
//      }
//     }
//     // lect 10 kkc client handler
//     private static class HandleClient extends Thread {
//         private Socket socket;
    
//         public HandleClient(Socket socket) {
//             this.socket = socket;
//         }
    
//         public void run() {
//             try {
//                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                 String inputLine;
    
//                 while ((inputLine = in.readLine()) != null) {
//                     String outputLine = processInput(inputLine);
//                     out.println(outputLine);
//                 }
    
//                 socket.close(); // Automatically closes input/output streams
    
//             } catch (IOException e) {
//                 System.err.println("Error in handling client request.");
//                 e.printStackTrace();
//             }
//         }
//     }
// // lect 8
// private static final int WAITING = 0;
// private static final int VOTING = 1;
// private static final int LISTING = 2;
// private static int state = WAITING;

// private static String processInput(String theInput) {
//     String theOutput = null;

//     if (state == WAITING) {
//         theOutput = "Welcome! You can vote using 'vote <option>' or check results with 'list'.";
//         state = VOTING;
//     } 
//     else if (state == VOTING) {
//         if (theInput.startsWith("vote ")) {
//             String option = theInput.substring(5).trim().toLowerCase();

//             if (!votes.containsKey(option)) {
//                 theOutput = "❌ Error: '" + option + "' is not a valid voting option.";
//             } else {
//                 // Thread-safe vote update
//                 synchronized (votes) {
//                     votes.put(option, votes.get(option) + 1);
//                 }
//                 theOutput = "✅ Vote for '" + option + "' counted!";
//                 state = WAITING; // Reset state
//             }
//         } 
//         else if (theInput.equals("list")) {
//             theOutput = "📋 Fetching vote results...";
//             state = LISTING;
//         } 
//         else {
//             theOutput = "❌ Invalid request. Use 'vote <option>' or 'list'.";
//         }
//     } 
//     else if (state == LISTING) {
//         StringBuilder result = new StringBuilder();
//         for (String key : votes.keySet()) {
//             result.append(key).append(": ").append(votes.get(key)).append(" votes\n");
//         }
//         theOutput = result.toString();
//         state = WAITING;
//     }

//     return theOutput;



//     }
// }


package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private static ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();
    private static final int PORT = 7777;
    private static final int MAX_CLIENTS = 30;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("At least two voting options must be given.");
            System.exit(1);
        }

        // clear votes
        votes.clear();

        System.out.println("Server running with voting options:" + votes.keySet());

        ExecutorService service = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port" + PORT);

            while (true) {
                Socket client = serverSocket.accept();
                service.submit(new HandleClient(client)); // Use HandleClient instead of ClientHandler
            }
        } catch (IOException e) {
            System.err.println("Cannot listen on port" + PORT);
            System.exit(1);
        }
    }

    private static class HandleClient extends Thread {
        private Socket socket;

        public HandleClient(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    String outputLine = processInput(socket, inputLine);
                    out.println(outputLine);
                }

                socket.close();
            } catch (IOException e) {
                System.err.println("Error handling client request.");
                e.printStackTrace();
            }
        }
    }

    private static String processInput(Socket clientSocket, String input) {
        if (input.startsWith("vote ")) {
            String option = input.substring(5).trim().toLowerCase();
            if (!votes.containsKey(option)) {
                return "Error: '" + option + "' is not a valid voting option.";
            }
            synchronized (votes) {
                votes.put(option, votes.get(option) + 1);
            }
            return "Vote for '" + option + "' counted!";
        } 
        else if (input.equals("list")) {
            StringBuilder result = new StringBuilder();
            for (String key : votes.keySet()) {
                result.append(key).append(": ").append(votes.get(key)).append(" votes\n");
            }
            return result.toString();
        } 
        else {
            return "Invalid request. Use 'vote <option>' or 'list'.";
        }
    }
}
