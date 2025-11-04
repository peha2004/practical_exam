package org.example.practical_exam;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {

    private static final Set<DataOutputStream> clientOutPuts = new HashSet<>();

    public static void main(String[] args) {
      
        try (ServerSocket serverSocket = new ServerSocket(5000)) {

            System.out.println("Server running on port 5000...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                synchronized (clientOutPuts) {
                    clientOutPuts.add(dos);
                }

                new Thread(new ClientHandler(socket, dos)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, byte[] imageBytes) {
        synchronized (clientOutPuts) {
            for (DataOutputStream dos : clientOutPuts) {
                try {
                    if (message != null) {
                        dos.writeUTF(message);
                    } else if (imageBytes != null) {
                        dos.writeUTF("IMAGE");
                        dos.writeInt(imageBytes.length);
                        dos.write(imageBytes);
                    }
                    dos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final DataOutputStream dos;
        private final DataInputStream dis;
        private String username;

        public ClientHandler(Socket socket, DataOutputStream dos) throws IOException {
            this.socket = socket;
            this.dos = dos;
            this.dis = new DataInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                // read username first
                String firstMessage = dis.readUTF();
                if (firstMessage.startsWith("USERNAME:")) {
                    username = firstMessage.substring(9);
                    System.out.println(username + " joined");
                    broadcast(username + " joined the chat", null);
                }

                // message loop
                while (true) {
                    String msg = dis.readUTF();
                    if (msg.equals("IMAGE")) {
                        int size = dis.readInt();
                        byte[] imageBytes = new byte[size];
                        dis.readFully(imageBytes);
                        System.out.println(username + " sent an image");

                        broadcast(null, imageBytes);
                    } else {
                        System.out.println(username + ": " + msg);
                        broadcast(username + ": " + msg, null);
                    }
                }

            } catch (Exception e) {
                System.out.println("Client disconnected: " + username);
            } finally {
                try {
                    dis.close();
                    dos.close();
                    socket.close();
                } catch (Exception ignored) {}

                synchronized (clientOutPuts) {
                    clientOutPuts.remove(dos);
                }

                broadcast(username + " left the chat", null);
            }
        }
    }
}
