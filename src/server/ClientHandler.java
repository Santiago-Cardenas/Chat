package server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter clientOut;
    private List<PrintWriter> clients;
    private List<String> usernames;
    private Map<String, ChatGroup> groups;
    private MessageHistory messageHistory;
    private String username;

    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;

    public ClientHandler(Socket socket, PrintWriter clientOut, List<PrintWriter> clients, List<String> usernames, Map<String, ChatGroup> groups, MessageHistory messageHistory) {
        this.clientSocket = socket;
        this.clientOut = clientOut;
        this.clients = clients;
        this.usernames = usernames;
        this.groups = groups;
        this.messageHistory = messageHistory;
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
            ChatServer.broadcastMessage("Usuario " + username + " se ha unido al chat.");
            ChatServer.saveMessage("Usuario " + username + " se ha unido al chat.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Mensaje recibido: " + message);

                if (message.startsWith("/")) {
                    handleCommand(message);
                } else {
                    ChatServer.broadcastMessage(username + ": " + message);
                    ChatServer.saveMessage(username + ": " + message);
                }
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
            ChatServer.broadcastMessage("Usuario " + username + " ha salido del chat.");
            ChatServer.saveMessage("Usuario " + username + " ha salido del chat.");
        }
    }

    private void handleCommand(String message) {
        String[] parts = message.split(" ", 3);
        String command = parts[0];

        switch (command) {
            case "/private":
                handlePrivateMessage(parts);
                break;
            case "/group":
                handleGroupCommand(message);
                break;
            case "/voice":
                handleVoiceMessage(parts);
                break;
            case "/call":
                handleCallCommand(parts);
                break;
            default:
                clientOut.println("Comando no reconocido.");
                break;
        }
    }

    private void handlePrivateMessage(String[] parts) {
        if (parts.length < 3) {
            clientOut.println("Uso: /private <usuario> <mensaje>");
            return;
        }
        String targetUser = parts[1];
        String message = parts[2];
        synchronized (clients) {
            for (int i = 0; i < usernames.size(); i++) {
                if (usernames.get(i).equals(targetUser)) {
                    clients.get(i).println("[Privado de " + username + "]: " + message);
                    ChatServer.saveMessage("[Privado de " + username + " a " + targetUser + "]: " + message);
                    return;
                }
            }
        }
        clientOut.println("Usuario no encontrado.");
    }

    private void handleGroupCommand(String message) {
        String[] parts = message.split(" ");
        if (parts.length < 3) {
            clientOut.println("Uso: /group <crear|unirse|mensaje> <nombreGrupo> [mensaje]");
            return;
        }

        String action = parts[1];
        String groupName = parts[2];
        String groupMessage = null;
        if(parts.length > 3) {
            groupMessage = parts[3];
        }
        for (int i = 0; i < parts.length; i++) {
            System.out.println("Parte " + i + ": " + parts[i]);
            System.out.println("Longitud: " + parts[i].length());
        }
        synchronized (groups) {
            if (action.equals("crear")) {
                if (!groups.containsKey(groupName)) {
                    groups.put(groupName, new ChatGroup(groupName));
                    clientOut.println("Grupo " + groupName + " creado.");
                    ChatGroup group = groups.get(groupName);
                    group.addMember(username, clientOut);
                } else {
                    clientOut.println("El grupo ya existe.");
                }
            } else if (action.equals("unirse")) {
                ChatGroup group = groups.get(groupName);
                if (group != null) {
                    group.addMember(username, clientOut);
                    clientOut.println("Te has unido al grupo " + groupName);
                    group.broadcast(username + " se ha unido al grupo.");
                } else {
                    clientOut.println("El grupo no existe.");
                }
            } else if (action.equals("mensaje")) {
                ChatGroup group = groups.get(groupName);
                if (group != null) {
                    group.broadcast(username + ": " + groupMessage);
                    ChatServer.saveMessage("[Grupo " + groupName + "] " + username + ": " + groupMessage);
                } else {
                    clientOut.println("El grupo no existe.");
                }
            } else {
                clientOut.println("Acción no reconocida.");
            }
        }
    }

    private void handleVoiceMessage(String[] parts) {
        if (parts.length < 2) {
            clientOut.println("Uso: /voice <usuario|grupo>");
            return;
        }

        String target = parts[1];
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RecordAudio recorder = new RecordAudio(format, 5, out);  // 5 segundos de duración
        Thread t = new Thread(recorder);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] audio = out.toByteArray();
        String audioMessage = "Mensaje de voz de " + username;

        synchronized (clients) {
            if (usernames.contains(target)) {
                int index = usernames.indexOf(target);
                clients.get(index).println(audioMessage);
                saveVoiceMessage(audio, username + "_to_" + target);
                new Thread(() -> {
                    PlayerAudio player = new PlayerAudio(format);
                    player.initAudio(audio);
                }).start();
                ChatServer.saveMessage("[Mensaje de voz de " + username + " a " + target + "]");
            } else if (groups.containsKey(target)) {
                ChatGroup group = groups.get(target);
                group.broadcast(audioMessage);
                saveVoiceMessage(audio, username + "_to_group_" + target);
                for (PrintWriter member : group.getMembers().values()) {
                    new Thread(() -> {
                        PlayerAudio player = new PlayerAudio(format);
                        player.initAudio(audio);
                    }).start();
                }
                ChatServer.saveMessage("[Mensaje de voz de " + username + " al grupo " + target + "]");
            } else {
                clientOut.println("Usuario o grupo no encontrado.");
            }
        }
    }

    private void handleCallCommand(String[] parts) {
        if (parts.length < 2) {
            clientOut.println("Uso: /call <usuario|grupo>");
            return;
        }

        String target = parts[1];
        String callMessage = "Llamada de " + username;

        synchronized (clients) {
            if (usernames.contains(target)) {
                int index = usernames.indexOf(target);
                clients.get(index).println(callMessage);
                // Aquí podríamos iniciar un protocolo para manejar la llamada
                ChatServer.saveMessage("[Llamada de " + username + " a " + target + "]");
            } else if (groups.containsKey(target)) {
                ChatGroup group = groups.get(target);
                group.broadcast(callMessage);
                // Aquí podríamos iniciar un protocolo para manejar la llamada de grupo
                ChatServer.saveMessage("[Llamada de " + username + " al grupo " + target + "]");
            } else {
                clientOut.println("Usuario o grupo no encontrado.");
            }
        }
    }

    private void saveVoiceMessage(byte[] audio, String fileName) {
        try {
            File directory = new File("Chat\\docs\\voice_messages");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, fileName + ".wav");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(audio);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
