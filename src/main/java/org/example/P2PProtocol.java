package org.example;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class P2PProtocol {
    private static final int PORT = 12345;
    private static List<PeerInfo> peers = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try {
            // Create a socket for listening to incoming connections
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("P2P server listening on port " + PORT);

            // Thread for handling peer discovery
            Thread discoveryThread = new Thread(P2PProtocol::discoverPeers);
            discoveryThread.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String request = reader.readLine();
            if (request != null) {
                if (request.equals("LIST")) {
                    // Respond with a list of registered peers
                    String peerList = getRegisteredPeers();
                    writer.write(peerList + "\n");
                    writer.flush();
                } else if (request.startsWith("REGISTER ")) {
                    // Extract the peer's name
                    String peerName = request.substring("REGISTER ".length());

                    // Register the peer
                    PeerInfo newPeer = new PeerInfo(peerName, clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                    peers.add(newPeer);
                    System.out.println("Peer registered: " + newPeer);

                    // Respond with a confirmation message
                    writer.write("REGISTERED\n");
                    writer.flush();
                }
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRegisteredPeers() {
        StringBuilder peerList = new StringBuilder();
        for (PeerInfo peer : peers) {
            peerList.append(peer.getName()).append(" (").append(peer.getIpAddress()).append(":").append(peer.getPort()).append(")\n");
        }
        return peerList.toString();
    }

    private static void discoverPeers() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            while (true) {
                // Broadcast a discovery message
                String discoveryMessage = "DISCOVER P2P_PEERS";
                byte[] sendData = discoveryMessage.getBytes();
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, PORT);
                socket.send(sendPacket);

                Thread.sleep(5000); // Broadcast every 5 seconds
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class PeerInfo {
    private String name;
    private String ipAddress;
    private int port;

    public PeerInfo(String name, String ipAddress, int port) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return name + " (" + ipAddress + ":" + port + ")";
    }
}
