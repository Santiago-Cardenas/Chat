package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor de chat en el puerto 12345");

            // Solicitar y enviar nombre de usuario
            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                System.out.println(fromServer);
                if (fromServer.equals("Ingrese su nombre de usuario:")) {
                    String username = stdIn.readLine();
                    out.println(username);
                    break;
                }
            }

            // Hilo para recibir mensajes del servidor
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Enviar mensajes al servidor
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);  // Enviar al servidor
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
