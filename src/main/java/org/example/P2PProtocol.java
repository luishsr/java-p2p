package org.example;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class P2PProtocol {
    private static final int DISCOVERY_PORT = 12345;
    private static final int PEER_PORT = 54321;

    private static List<PeerInfo> peers = new ArrayList<>();

    public static void main(String[] args) {
        // Start a thread for peer discovery
        Thread discoveryThread = new Thread(P2PProtocol::startPeerDiscovery);
        discoveryThread.start();

        // Start a thread for self-peer registration
        Thread registrationThread = new Thread(P2PProtocol::registerSelf);
        registrationThread.start();

        // Start a thread to handle incoming connections
        Thread serverThread = new Thread(P2PProtocol::startServer);
        serverThread.start();
    }

    private static void startPeerDiscovery() {
        try {
            DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                // Process the received data (e.g., extract peer information)
                String peerInfo = new String(receivePacket.getData(), 0, receivePacket.getLength());
                PeerInfo newPeer = new PeerInfo(receivePacket.getAddress(), PEER_PORT);

                // Add the peer to the list if not already present
                if (!peers.contains(newPeer)) {
                    peers.add(newPeer);
                    System.out.println("Discovered new peer: " + newPeer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerSelf() {
        try {
            DatagramSocket socket = new DatagramSocket();

            // Register the local peer's information with the discovery service
            String registrationMessage = "REGISTER " + InetAddress.getLocalHost().getHostAddress() + " " + PEER_PORT;
            byte[] sendData = registrationMessage.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName("localhost"), DISCOVERY_PORT);

            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PEER_PORT);
            System.out.println("Listening for incoming connections on port " + PEER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Handle incoming connections (e.g., exchange data with other peers)
                // Implement your logic for P2P communication here
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PeerInfo {
        InetAddress address;
        int port;

        public PeerInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PeerInfo peerInfo = (PeerInfo) o;

            return port == peerInfo.port && address.equals(peerInfo.address);
        }

        @Override
        public int hashCode() {
            int result = address.hashCode();
            result = 31 * result + port;
            return result;
        }

        @Override
        public String toString() {
            return address.getHostAddress() + ":" + port;
        }
    }
}
