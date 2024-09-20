package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter clientOut;
    private List<PrintWriter> clients;
    private List<String> usernames;
    private String username;

    public ClientHandler(Socket socket, PrintWriter clientOut, List<PrintWriter> clients, List<String> usernames) {
        this.clientSocket = socket;
        this.clientOut = clientOut;
        this.clients = clients;
        this.usernames = usernames;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            while (true) {
                clientOut.println("Ingrese su nombre de usuario:");
                username = in.readLine();

                if (username == null) {
                    return;
                }

                synchronized (usernames) {
                    if (!usernames.contains(username)) {
                        usernames.add(username);
                        break;
                    } else {
                        clientOut.println("Nombre de usuario ya está en uso. Intente con otro nombre.");
                    }
                }
            }

            clients.add(clientOut);
            Server.broadcastMessage("Usuario " + username + " se ha unido al chat.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Mensaje recibido: " + message);
                Server.broadcastMessage(username + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.remove(clientOut);
            synchronized (usernames) {
                usernames.remove(username);
            }
            Server.broadcastMessage("Usuario " + username + " ha salido del chat.");
        }
    }
}
