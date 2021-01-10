package indi.atatc.atcp_server;

import indi.atatc.packages.data.ID;
import indi.atatc.packages.log.Log;

import java.io.IOException;
import java.net.Socket;

public final class Connection {
    private final Socket cSocket;
    private final ID.IP ip;

    Socket getSocket() {
        return cSocket;
    }

    Connection(Socket cSocket) {
        this.cSocket = cSocket;
        ip = new ID.IP(cSocket.getInetAddress().getHostName());
    }

    public ID.IP getIP() {
        return ip;
    }

    public boolean isClosed() {
        try {
            cSocket.sendUrgentData(0xFF); // 发送心跳包System.out.println("目前是处于链接状态！");
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    void abort() {
        if (!isClosed()) {
            try {
                cSocket.close();
            } catch (IOException e) {
                Log.getInstance().publish(e);
            }
        }
    }

    @Override
    public String toString() {
        return getIP().toString();
    }
}
