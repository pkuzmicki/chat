package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {
    public void start(String address, int port) {
        try {
            ConnectionThread thread = new ConnectionThread(address, port);
            thread.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Insert your login: ");
            String login = reader.readLine();
            thread.login(login);
            System.out.println("Type /online to check online users or /exit to disconnect");

            while(true) {
                String rawMessage = reader.readLine();

                if (rawMessage.equals("/online")) {
                    thread.onlineUsers();
                } else if (rawMessage.startsWith("/w")) {
                    thread.whisper(rawMessage);
                } else if (rawMessage.equals("/exit")) {
                    thread.logout(login);
                } else {
                    Message message = new Message(
                            MessageType.Broadcast, rawMessage);
                    thread.send(message);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
