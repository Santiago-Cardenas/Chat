package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static List<PrintWriter> clients = Collections.synchronizedList(new ArrayList<>());
    private static List<String> usernames = Collections.synchronizedList(new ArrayList<>());
    private static Map<String, ChatGroup> groups = Collections.synchronizedMap(new HashMap<>());
    private static MessageHistory messageHistory = new MessageHistory();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor de chat iniciado en el puerto 12345");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress());

                // Output stream para enviar datos al cliente
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Ejecutar el manejador del cliente
                threadPool.execute(new ClientHandler(clientSocket, out, clients, usernames, groups, messageHistory));
            }
        }
    }

    // Método para enviar mensajes a todos los clientes
    public static void broadcastMessage(String message) {
        synchronized (clients) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }
    }

    public static void saveMessage(String message) {
        messageHistory.addMessage(message);
    }
}
