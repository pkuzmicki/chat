package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {
    Socket socket;
    Server server;
    PrintWriter writer;
    BufferedReader reader;
    List<String> clientUsernames = new ArrayList<>();
    String login;

    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            reader = new BufferedReader(new InputStreamReader(input));
            writer = new PrintWriter(output,true);

            String rawMessage;

            while((rawMessage = reader.readLine()) != null) {
                Message message = new ObjectMapper().readValue(rawMessage, Message.class);

                switch (message.type) {
                    case Login -> {
                        login = message.content;
                        server.login(this);
                    }
                    case Broadcast -> server.broadcast(login + ": ", message);
                    case Whisper -> server.whisper(login + ": ", message);
                    case Online -> onlineUsers();
                    case Logout -> leave();
                }
            }
            leave();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Message message) throws JsonProcessingException {
        String rawMessage = new ObjectMapper().writeValueAsString(message);
        writer.println(rawMessage);
    }

    public void leave() throws IOException {
        socket.close();
        server.logout(this);
    }

    public String getUsername() {
        return login;
    }

    public void onlineUsers() throws JsonProcessingException {
        Message message = new Message(MessageType.Online, server.usernames().toString());
        send(message);
    }
}
