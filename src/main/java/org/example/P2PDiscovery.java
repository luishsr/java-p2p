package org.example;

import java.io.IOException;
import java.net.*;

public class P2PDiscovery {
    private static final int PORT = 8888;
    private static final String DISCOVERY_MESSAGE = "P2P_DISCOVERY";
    private static final String RESPONSE_MESSAGE = "P2P_RESPONSE";

    public static void main(String[] args) {
        try {
            // Create a DatagramSocket to send and receive UDP packets
            DatagramSocket socket = new DatagramSocket(PORT);

            // Create a thread to listen for incoming discovery requests
            Thread listenerThread = new Thread(() -> {
                try {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    while (true) {
                        socket.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        // Check if the received message is a discovery request
                        if (message.equals(DISCOVERY_MESSAGE)) {
                            InetAddress clientAddress = receivePacket.getAddress();
                            int clientPort = receivePacket.getPort();
                            System.out.println("Received discovery request from " + clientAddress.getHostAddress());

                            // Respond to the discovery request
                            String responseMessage = "P2P_RESPONSE";
                            byte[] responseData = responseMessage.getBytes();
                            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                                    clientAddress, clientPort);
                            socket.send(responsePacket);
                        } else if (message.equals(RESPONSE_MESSAGE)) {
                            InetAddress clientAddress = receivePacket.getAddress();
                            int clientPort = receivePacket.getPort();
                            System.out.println("Received response back from " + clientAddress.getHostAddress());

                            // Respond to the discovery request
                            String responseMessage = "P2P_RESPONSE";
                            byte[] responseData = responseMessage.getBytes();
                            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                                    clientAddress, clientPort);
                            socket.send(responsePacket);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Start the listener thread
            listenerThread.start();

            // Broadcast your presence
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            String broadcastMessage = DISCOVERY_MESSAGE;
            byte[] sendData = broadcastMessage.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, PORT);

            while (true) {
                socket.send(sendPacket);
                System.out.println("Broadcasted presence...");
                Thread.sleep(5000); // Broadcast every 5 seconds
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
