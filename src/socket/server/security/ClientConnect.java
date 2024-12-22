package socket.server.security;

import java.net.InetAddress;

public class ClientConnect {
    private InetAddress ipNet;
    public ClientConnect(InetAddress ipNet) {
        System.out.println("New client connected : " + ipNet.getHostName() + " : " + ipNet.getHostAddress());
        this.ipNet = ipNet;
    }
}
