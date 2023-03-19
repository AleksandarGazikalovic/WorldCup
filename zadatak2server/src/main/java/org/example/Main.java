package org.example;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8081);
        serverSocket.setReuseAddress(true);
        while (true) {
            try {
                System.out.println("Cekam na konekciju");
                Socket communicationSocket = serverSocket.accept();
                System.out.println("Doslo je do konekcije");
                ObjectOutputStream clientOutputStream = new ObjectOutputStream(communicationSocket.getOutputStream());
                clientOutputStream.writeUTF("Uspesno ste se povezali");
                clientOutputStream.flush();
                ObjectInputStream clientInputStream = new ObjectInputStream(communicationSocket.getInputStream());

                ClientHandler clientHandler = new ClientHandler(communicationSocket, clientInputStream, clientOutputStream);
                new Thread(clientHandler).start();
                continue;


            } catch (IOException e) {
                System.out.println("Konekcija je prekinuta!");            }
        }
    }
}