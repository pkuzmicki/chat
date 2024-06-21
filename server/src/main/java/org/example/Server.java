package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private ServerSocket socket;
    List<ClientThread> clients = new ArrayList<>();
    Map<String, ClientThread> clientMap = new HashMap<>();

    public Server(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    public List<String> usernames(){
        return clientMap.keySet().stream().toList();
    }

    public void listen() throws IOException {
        while(true) {
            Socket client = socket.accept();
            System.out.println("new client connected:" + client);

            ClientThread thread = new ClientThread(client, this);
            clients.add(thread);
            thread.start();
        }
    }

    public void login(ClientThread loggedClient) throws JsonProcessingException {
        clientMap.put(loggedClient.getUsername(), loggedClient);
        for(ClientThread client : clients) {
            if (client != loggedClient) {
                Message message = new Message(MessageType.Broadcast, "User " + loggedClient.getUsername() + " joined the chat!");
                client.send(message);
            }
        }
        System.out.println("new client: " + loggedClient.getUsername());
    }

    public void logout(ClientThread thisClient) throws IOException {
        clients.remove(thisClient);
        clientMap.remove(thisClient.getName());
        Message message = new Message(MessageType.Broadcast, "User " + thisClient.getUsername() + " has left the chat!");
        broadcast("server: ", message);
    }

    public void broadcast(String sender, Message message) throws JsonProcessingException {
        message.content = sender + message.content;
        for(ClientThread client : clients) {
            client.send(message);
        }
    }

    public void whisper(String sender, Message message) throws JsonProcessingException {
        String[] messageParts = message.content.split(" ", 3);
        String login = messageParts[1];
        String content = messageParts[2];


        for (ClientThread client : clients){
            if (client.getUsername().equals(login)){
                message.content = sender + content;
                client.send(message);
            }
        }
    }
}
