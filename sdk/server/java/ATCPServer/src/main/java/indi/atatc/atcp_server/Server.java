package indi.atatc.atcp_server;

import indi.atatc.packages.basics.Basics;
import indi.atatc.packages.log.Log;
import indi.atatc.packages.data.ID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ProcessList {
    private static final ProcessList instance = new ProcessList();
    private final ConcurrentMap<ID.PID, Process> processList;

    private ProcessList() {
        processList = new ConcurrentHashMap<>();
    }

    Process getProcessByPID(ID.PID pid) {
        return processList.get(pid);
    }

    public ID.PID[] getPIDs() {
        return processList.keySet().toArray(ID.PID[]::new);
    }

    public static ProcessList getInstance() {
        return instance;
    }

    public void add(Process process) {
        processList.put(process.getPID(), process);
        process.start();
    }

    public void remove(ID.PID pid) throws Basics.AccidentEvents.AccidentEvent {
        processList.get(pid).stop();
        processList.remove(pid);
    }

    public boolean contains(ID.PID pid) {
        return processList.containsKey(pid);
    }

    @Override
    public String toString() {
        return processList.toString();
    }
}

class ListenerList {
    private static final ListenerList instance = new ListenerList();
    private final ConcurrentHashMap<ID.LID, Basics.StructureClass.Listener> listenerList;

    private ListenerList() {
        listenerList = new ConcurrentHashMap<>();
    }

    public static ListenerList getInstance() {
        return instance;
    }

    public void add(Basics.StructureClass.Listener listener) {
        listenerList.put(listener.getLid(), listener);
    }

    public void remove(ID.LID lid) {
        listenerList.remove(lid);
    }

    public boolean contains(ID.LID lid) {
        return listenerList.containsKey(lid);
    }

    public Collection<Basics.StructureClass.Listener> getListeners() {
        return listenerList.values();
    }
}

public abstract class Server {
    public static final class Configuration {
        public String name = "undefined";
        public int port = 4747;
        public String project = "undefined";
        public String type = "usr-g";
    }

    public enum Mode {
        MT, T, MT_T
    }

    public final String name, project, type;
    private boolean test = false;
    private final ProcessList processList;
    private final ListenerList listenerList;
    private final int port;
    private final Log log;
    private boolean isAlive, hasStarted, hasStopped = false;

    public Server(Configuration configuration) {
        processList = ProcessList.getInstance();
        listenerList = ListenerList.getInstance();
        port = configuration.port;
        log = Log.getInstance();

        name = configuration.name;
        type = configuration.type;
        project = configuration.project;
    }

    protected void onStart() {
    }

    protected void onStarted() {
    }

    protected abstract Process onConnected(Connection connection);

    protected void onInterrupt() {
    }

    protected void onInterrupted() {
    }

    public final Log getLog() {
        return log;
    }

    public final void addLoopListener(LoopListener loopListener) {
        listenerList.add(loopListener);
    }

    public final void whenListener() {
        while (!hasStopped) {
            while (isAlive) {
                for (Basics.StructureClass.Listener listener : listenerList.getListeners()) {
                    if (listener.ready()) {
                        listener.start();
                    }
                }
            }
        }
    }

    public final ID.MID sendToPID(ID.PID pid, String msg, Flags flags) throws Basics.AccidentEvents.AccidentEvent {
        return processList.getProcessByPID(pid).send(msg, flags);
    }

    public final ID.MID sendToPID(ID.PID pid, String msg) throws Basics.AccidentEvents.AccidentEvent {
        return processList.getProcessByPID(pid).send(msg);
    }

    private void run() {
        if (isAlive) return;
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            Log.getInstance().publish("server start failed", Log.DebugLevel.WARNING);
            return;
        }
        isAlive = true;
        Thread whenListener = new Thread(this::whenListener);
        whenListener.start();
        while (isAlive) {
            try {
                Connection connection = new Connection(socket.accept());
                ID.IP ip = connection.getIP();
                if (test) {
                    if (!ip.isArea()) {
                        connection.abort();
                        continue;
                    }
                }
                if (!judge(log, ip)) {
                    log.prison.getColumnByName("ip").add(ip);
                    connection.abort();
                    continue;
                }
                if (log.prison.contains(ip)) {
                    connection.abort();
                    continue;
                }
                log.publish("connected IP: " + connection.getIP().toString(), Log.DebugLevel.INFO);
                Process process = onConnected(connection);
                processList.add(process);
                log.publish(processList.toString(), Log.DebugLevel.DEBUG);
            } catch (IOException e) {
                Log.getInstance().publish(e);
                return;
            }
        }
    }

    public boolean judge(Log log, ID.IP ip) {
        return true;
    }

    public final void start() {
        if (hasStarted) return;
        hasStarted = true;
        run();
    }


    public final void start(Mode mode) {
        if (hasStarted) return;
        hasStarted = true;
        switch (mode) {
            case MT -> new Thread(this::run, "server thread").start();
            case T -> {
                test = true;
                run();
            }
            case MT_T -> {
                test = true;
                new Thread(this::run, "server thread").start();
            }
        }
    }

    public final void stop() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            if (!isAlive) return;
            hasStopped = true;
            for (ID.PID pid : processList.getPIDs()) {
                processList.getProcessByPID(pid).stop();
            }
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
            for (ID.PID pid : processList.getPIDs()) {
                processList.getProcessByPID(pid).getConnection().abort();
            }
            isAlive = false;
        } finally {
            lock.unlock();
        }
    }
}
