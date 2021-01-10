package indi.atatc.atcp_server;

import com.google.gson.Gson;
import indi.atatc.aknn.Item;
import indi.atatc.packages.basics.Basics;
import indi.atatc.packages.data.ID;
import indi.atatc.packages.log.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Regulator extends Thread {
    private static class ClientCrimeRecord extends Item {
        public enum CrimeType {
            Evil, Primary, Rookie, Accident
        }

        private int evil, primary, rookie, accident = 0;
        final Connection connection;

        public ClientCrimeRecord(Connection connection) {
            this.connection = connection;
        }

        @Override
        protected int getDimension() {
            return 4;
        }

        @Override
        public double getCoordinate(int coordinateAxis) {
            return 0;
        }

        public void recordCrime(CrimeType crimeType) {
            switch (crimeType) {
                case Evil -> evil++;
                case Primary -> primary++;
                case Rookie -> rookie++;
                case Accident -> accident++;
            }
        }

        private int judge() {
            return evil * 4 + primary * 3 + rookie * 2 + accident;
        }
    }

    public abstract static class BaseLine {
        public abstract boolean check(ClientCrimeRecord clientCrimeRecord);
    }

    private final static Regulator instance = new Regulator();

    public static Regulator getInstance() {
        return instance;
    }

    private BaseLine baseLine = new BaseLine() {
        @Override
        public boolean check(ClientCrimeRecord clientCrimeRecord) {
            return true;
        }
    };
    private double rejectionRate = 1;
    private final ConcurrentHashMap<String, ClientCrimeRecord> clientCrimeRecordMap = new ConcurrentHashMap<>();
    private final Basics.StructureClass.Queue<String> blockList = new Basics.StructureClass.Queue<>();
    private ClientCrimeRecord[] clientCrimeRecords = new ClientCrimeRecord[0];

    private Regulator() {
        super.start();
    }

    public void setBaseLine(BaseLine baseLine) {
        this.baseLine = baseLine;
    }

    public void setRejectionRate(double rejectionRate) {
        this.rejectionRate = rejectionRate;
    }

    private String connection2id(Connection connection) {
        return connection.getIP().toString();
    }

    private void importFile() {
    }

    private void exportFile() {
        Gson gson = new Gson();
    }

    // 堆排序
    private void sortClientCrimeRecords() {
        for (int i = clientCrimeRecords.length - 1; i > 0; i--) {
            if (clientCrimeRecords[0].judge() > clientCrimeRecords[i].judge()) {
                ClientCrimeRecord bucket = clientCrimeRecords[0];
                clientCrimeRecords[0] = clientCrimeRecords[i];
                clientCrimeRecords[i] = bucket;
            }
        }
    }

    @Override
    public synchronized void run() {
        try {
            for (; ; ) {
                sortClientCrimeRecords();
                for (int i = 0; i < clientCrimeRecords.length * rejectionRate; i++) {
                    if (baseLine.check(clientCrimeRecords[i])) {
                        clientCrimeRecords[i].connection.abort();
                        blockList.add(connection2id(clientCrimeRecords[i].connection));
                    }
                }
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } finally {
            exportFile();
        }
    }

    public synchronized void report(Connection connection, ClientCrimeRecord.CrimeType crimeType) {
        if (clientCrimeRecordMap.containsKey(connection2id(connection))) {
            clientCrimeRecordMap.get(connection2id(connection)).recordCrime(crimeType);
        } else {
            ClientCrimeRecord clientCrimeRecord = new ClientCrimeRecord(connection);
            clientCrimeRecord.recordCrime(crimeType);
            clientCrimeRecordMap.put(connection2id(connection), clientCrimeRecord);
            clientCrimeRecords = Arrays.copyOf(clientCrimeRecords, clientCrimeRecords.length + 1);
            clientCrimeRecords[clientCrimeRecords.length - 1] = clientCrimeRecord;
        }
    }

}
