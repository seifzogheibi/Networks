package server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

import client.ClientHandler; // ✅ Import ClientHandler from the client package


// public class Server
// {
// 	public static void main( String[] args )
// 	{
// 	}
// }

public class Server {
    private static final int PORT = 7777;
    private static final int MAX_CLIENTS = 30;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSock = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket sock = serverSock.accept();
                pool.execute(new ClientHandler(sock)); // Hand off client to worker thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
