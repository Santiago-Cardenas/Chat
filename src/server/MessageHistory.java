package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageHistory {
    private List<String> messages = Collections.synchronizedList(new ArrayList<>());

    public void addMessage(String message) {
        messages.add(message);
        saveToFile(message);
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    private void saveToFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Chat\\docs\\message_history.txt", true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
