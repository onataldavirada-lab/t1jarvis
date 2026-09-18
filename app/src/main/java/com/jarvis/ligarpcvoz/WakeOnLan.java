package com.jarvis.ligarpcvoz;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public final class WakeOnLan {
    private static final String MAC = "F4:B5:20:5C:1B:5E";
    private static final String BROADCAST = "192.168.15.255";
    private static final int PORT = 9;

    private WakeOnLan() {}

    public static void send() throws Exception {
        byte[] mac = parseMac(MAC);
        byte[] packet = new byte[6 + 16 * mac.length];

        for (int i = 0; i < 6; i++) packet[i] = (byte) 0xFF;

        for (int i = 6; i < packet.length; i += mac.length) {
            System.arraycopy(mac, 0, packet, i, mac.length);
        }

        InetAddress address = InetAddress.getByName(BROADCAST);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.send(new DatagramPacket(packet, packet.length, address, PORT));
        }
    }

    private static byte[] parseMac(String macAddress) {
        String normalized = macAddress.replace(":", "").replace("-", "");

        if (normalized.length() != 12) {
            throw new IllegalArgumentException("MAC inválido");
        }

        byte[] mac = new byte[6];
        for (int i = 0; i < 6; i++) {
            int index = i * 2;
            mac[i] = (byte) Integer.parseInt(
                    normalized.substring(index, index + 2),
                    16
            );
        }
        return mac;
    }
}
