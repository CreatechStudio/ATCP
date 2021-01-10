package indi.atatc.atcp_client.packages.log;

import indi.atatc.atcp_client.packages.basics.Basics;
import indi.atatc.atcp_client.packages.data.Values;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Log {
    public static final class Prison {
        public static final class Column {
            public String name;
            private final ConcurrentLinkedQueue<Object> list = new ConcurrentLinkedQueue<>();

            public Column(String name) {
                this.name = name;
            }

            public void add(Object element) {
                list.add(element);
            }

            public boolean contains(Object element) {
                return list.contains(element);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Column column = (Column) o;
                return Objects.equals(name, column.name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name);
            }
        }

        private final ConcurrentLinkedQueue<Column> blacklist = new ConcurrentLinkedQueue<>();

        private Prison() {
        }

        public void add(Column column) {
            if (blacklist.contains(column)) {
                throw new Basics.AccidentEvents.StatusError("column already exists");
            }
            blacklist.add(column);
        }

        public Column getColumnByName(String name) {
            for (Column column : blacklist) {
                if (column.name.equals(name)) {
                    return column;
                }
            }
            throw new Basics.AccidentEvents.StatusError("no such column");
        }

        public boolean contains(Object element) {
            for (Column column : blacklist) {
                if (column.contains(element)) return true;
            }
            return false;
        }

        public void clear() {
            blacklist.clear();
        }
    }

    public enum DebugLevel {
        INFO, DEBUG, WARNING, ERROR, FATAL
    }

    private static int valueOfDebugLevel(DebugLevel debugLevel) {
        return switch (debugLevel) {
            case INFO -> 0;
            case DEBUG -> 1;
            case WARNING -> 2;
            case ERROR -> 3;
            case FATAL -> 4;
        };
    }

    public final Prison prison = new Prison();
    private static final indi.atatc.atcp_client.packages.log.Log instance = new indi.atatc.atcp_client.packages.log.Log();
    public DebugLevel debugLevel = DebugLevel.INFO;

    private Log() {
        Prison.Column column = new Prison.Column("ip");
        prison.add(column);
    }

    public static indi.atatc.atcp_client.packages.log.Log getInstance() {
        return instance;
    }

    public static String time() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static String time(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }

    synchronized public void clientRecord(String cid, String info, String recv, String send) {
    }

    synchronized public void accidentRecord(String code, String... args) {
    }

    private static void record(String message) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile((File) Values.getInstance().get("log_path"), "rw");
            long fileLength = randomAccessFile.length();
            randomAccessFile.seek(fileLength);
            randomAccessFile.writeBytes(message);
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void publish(String message, DebugLevel debugLevel) {
        if (valueOfDebugLevel(this.debugLevel) >= valueOfDebugLevel(debugLevel)) {
            System.out.println(message);
        }
        record(message + '\n');
    }

    public synchronized void publish(String message, String end, DebugLevel debugLevel) {
        message += end;
        if (valueOfDebugLevel(this.debugLevel) >= valueOfDebugLevel(debugLevel)) {
            System.out.print(message);
        }
        record(message);
    }

    public synchronized void publish(boolean bool, DebugLevel debugLevel) {
        String message = "false";
        if (bool) message = "true";
        if (valueOfDebugLevel(this.debugLevel) >= valueOfDebugLevel(debugLevel)) {
            System.out.println(message);
        }
        record(message + '\n');
    }

    public synchronized void publish(boolean bool, String end, DebugLevel debugLevel) {
        String message = "false";
        if (bool) message = "true";
        message += end;
        if (valueOfDebugLevel(this.debugLevel) >= valueOfDebugLevel(debugLevel)) {
            System.out.print(message);
        }
        record(message);
    }

    public synchronized void publish(Exception e) {
        String message = e.toString();
        if (debugLevel == DebugLevel.ERROR) {
            System.out.println(message);
        }
        if (debugLevel == DebugLevel.FATAL) {
            e.printStackTrace();
        }

        record(message + '\n');
    }


    public synchronized void publish(Exception e, String end) {
        String message = e.toString() + end;
        if (debugLevel == DebugLevel.ERROR) {
            System.out.print(message);
        }
        if (debugLevel == DebugLevel.FATAL) {
            e.printStackTrace();
        }

        record(message);
    }

}
