package server;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ChatGroup {
    private String name;
    private Map<String, PrintWriter> members;

    public ChatGroup(String name) {
        this.name = name;
        this.members = new HashMap<>();
    }

    public void addMember(String username, PrintWriter out) {
        members.put(username, out);
    }

    public void removeMember(String username) {
        members.remove(username);
    }

    public void broadcast(String message) {
        for (PrintWriter out : members.values()) {
            out.println("[Grupo " + name + "] " + message);
        }
    }

    public Map<String, PrintWriter> getMembers() {
        return members;
    }
}
