package indi.atatc.atcp_client;

import indi.atatc.atcp_client.packages.basics.Basics;
import indi.atatc.atcp_client.packages.data.ID;
import indi.atatc.atcp_client.packages.data.Values;
import indi.atatc.atcp_client.packages.log.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Client {
    public static final class Configuration {
        public String name = "undefined";
        public String ip = "127.0.0.1";
        public int port = 4747;
        public String project = "undefined";
        public String type = "usr-g";
    }

    public static enum Mode {
        MT
    }

    public final String name, project, type;
    private final String ip;
    private final int port;
    private Process cProcess;
    private boolean isAlive, hasStarted, hasStopped = false;

    public Client(Configuration configuration) {
        ip = configuration.ip;
        port = configuration.port;

        name = configuration.name;
        type = configuration.type;
        project = configuration.project;
    }

    protected abstract Process onConnected(Connection connection);

    public final ID.MID send(String message) {
        return cProcess.send(message);
    }

    public final ID.MID send(String message, Flags flags) throws Basics.AccidentEvents.AccidentEvent {
        return cProcess.send(message, flags);
    }

    public final void run() {
        try {
            Socket socket = new Socket(ip, port);
            Connection connection = new Connection(socket);
            isAlive = true;
            cProcess = onConnected(connection);
            cProcess.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized final void start() {
        if (hasStarted) return;
        hasStarted = true;
        new Thread(this::run, "client thread").start();
        try {
            wait();
        } catch (InterruptedException e) {
            Log.getInstance().publish(e);
        }
    }

    public final void stop() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            if (!isAlive) return;
            hasStopped = true;
            cProcess.stop();
            isAlive = false;
        } finally {
            lock.unlock();
        }
    }

    public final void interrupt() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            if (!isAlive) return;
            hasStopped = true;
            cProcess.getConnection().abort();
            isAlive = false;
        } finally {
            lock.unlock();
        }
    }
}
