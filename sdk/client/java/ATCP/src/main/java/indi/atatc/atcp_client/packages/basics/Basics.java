package indi.atatc.atcp_client.packages.basics;

import indi.atatc.atcp_client.packages.data.ID;
import indi.atatc.atcp_client.packages.data.Values;
import indi.atatc.atcp_client.packages.log.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class Basics {
    public static class AccidentEvents {
        public static class AccidentEvent extends RuntimeException {
            private final String[] args;

            public AccidentEvent(String... args) {
                this.args = args;
            }

            @Override
            public String toString() {
                StringBuilder string = new StringBuilder(this.getClass().toString() + "{");
                for (String arg: args) {
                    string.append(arg);
                    string.append(" ");
                }
                string.append("}");
                return string.toString();
            }
        }

        public static class EmptyMessageError extends AccidentEvent {
            public EmptyMessageError(String... args) {
                super(args);
                Log.getInstance().publish(this);
            }
        }

        public static class EmptyParamError extends AccidentEvent {
            public EmptyParamError(String... args) {
                super(args);
                Log.getInstance().publish(this);
            }
        }

        public static class InvalidParamError extends AccidentEvent {
            public InvalidParamError(String... args) {
                super(args);
                Log.getInstance().publish(this);
            }
        }

        public static class InvalidSignatureError extends AccidentEvent {
            public InvalidSignatureError(String... args) {
                super(args);
                Log.getInstance().publish(this);
            }
        }

        public static class StatusError extends AccidentEvent {
            public StatusError(String... args) {
                super(args);
                Log.getInstance().publish(this);
            }
        }
    }

    public static class Conversion {
        public static String[] stringArrayList2stringArray(ArrayList<String> arrayList) {
            String[] res = new String[arrayList.size()];
            for (int i = 0; i < arrayList.size(); i++) {
                res[i] = arrayList.get(i);
            }
            return res;
        }

        public static ContainerClass.Flag[] flagArrayList2flagArray(ArrayList<ContainerClass.Flag> arrayList) {
            ContainerClass.Flag[] res = new ContainerClass.Flag[arrayList.size()];
            for (int i = 0; i < arrayList.size(); i++) {
                res[i] = arrayList.get(i);
            }
            return res;
        }
    }

    public static class NetClass {
        public static String recv(Socket socket) throws IOException {
            String separator = (String) Values.getInstance().get("separator_first_grade");
            StringBuilder msg = new StringBuilder();
            InputStream inputStream = socket.getInputStream();
            byte[] buf = new byte[10240];
            int line;
            while ((line = inputStream.read(buf)) != -1) {
                msg.append(new String(buf, 0, line));
                if (msg.toString().endsWith(separator)) {
                    break;
                }
            }
            if (msg.length() < 3) {
                return "";
            }
            return msg.substring(0, msg.length() - separator.length());
        }

        public static void send(Socket socket, String msg) throws IOException {
            String separator = (String) Values.getInstance().get("separator_first_grade");
            msg += separator;
            DataOutputStream server_dataOutputStream = new DataOutputStream(socket.getOutputStream());
            server_dataOutputStream.write(msg.getBytes());
            server_dataOutputStream.flush();
        }
    }

    public static class TextClass {
        public static String[] split(String content, String separator) {
            LinkedList<String> res = new LinkedList<>();
            int from_index = 0;
            int target_len = separator.length();
            int content_len = content.length();
            for (int i = 0; i < content_len - target_len + 1; i++) {
                if (content.substring(i, i + target_len).equals(separator)) {
                    res.addLast(content.substring(from_index, i));
                    from_index = i + target_len;
                }
            }
            if (from_index != content_len) {
                res.addLast(content.substring(from_index));
            }

            return res.toArray(String[]::new);
        }
        public static String[] split(String content, char separator) {
            LinkedList<String> res = new LinkedList<>();
            int from_index = 0;
            int content_len = content.length();
            for (int i = 0; i < content_len; i++) {
                if (content.charAt(i) == separator) {
                    res.addLast(content.substring(from_index, i));
                    from_index = i + 1;
                }
            }
            if (from_index != content_len) {
                res.addLast(content.substring(from_index));
            }

            return res.toArray(String[]::new);
        }
    }

    public static class StructureClass {
        public static abstract class Listener {
            private final ID.LID lid;

            public Listener() {
                lid = ID.IDsManager.getInstance().newLID();
            }

            public ID.LID getLid() {
                return lid;
            }

            public abstract boolean when();

            public abstract void run();

            public final void start() {
                run();
            }

            public boolean ready() {
                return when();
            }
        }

        public static class Queue<E> {
            private Object[] queue;
            private int usedLength;

            public Queue() {
                queue = new Object[5];
                usedLength = 0;
            }

            @SuppressWarnings("unchecked")
            public E get() {
                return (E) queue[0];
            }

            public void add(E element) {
                ReentrantLock lock = new ReentrantLock();
                lock.lock();
                try {
                    if (queue.length <= usedLength) {
                        Object[] oldQueue = queue.clone();
                        queue = new Object[(int) (queue.length * 1.5)];
                        System.arraycopy(oldQueue, 0, queue, 0, oldQueue.length);
                    }
                    queue[usedLength] = element;
                    usedLength++;
                } finally {
                    lock.unlock();
                }
            }

            public final void add(Object... elements) {
                for (Object element : elements) {
                    add(element);
                }
            }

            public int length() {
                return usedLength;
            }

            public void remove() {
                ReentrantLock lock = new ReentrantLock();
                lock.lock();
                try {
                    Object[] oldQueue = queue.clone();
                    queue = new Object[queue.length - 1];
                    System.arraycopy(oldQueue, 1, queue, 0, oldQueue.length - 1);
                    usedLength--;
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public static class ContainerClass {
        public static class RemoteConfiguration {
            public String name = "undefined";
            public String type = "usr-g";
            public String project = "undefined";
            public boolean rsa = true;
            public boolean message_blocking = true;
            public String separator_second_grade;
            public String separator_third_grade;
        }
        public static class Flag {
            protected String flag;
            protected String string;
            protected String value;

            public Flag(String flag) {
                if (flag.isEmpty()) {
                    string = this.flag = "";
                } else {
                    string = flag;
                    this.flag = Base64.getEncoder().encodeToString(string.getBytes());
                }
                value = null;
            }

            public Flag(String flag, String value) {
                if (flag.isEmpty()) {
                    string = this.flag = "";
                    this.value = null;
                } else {
                    string = flag;
                    this.flag = Base64.getEncoder().encodeToString(string.getBytes());
                    this.value = value;
                }
            }

            public boolean isSys() {
                return false;
            }

            public String export() {
                return flag + ":" + value;
            }

            public String getValue() {
                return value;
            }

            @Override
            public String toString() {
                return string;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Flag flagObject = (Flag) o;
                return Objects.equals(flag, flagObject.flag);
            }

            @Override
            public int hashCode() {
                return Objects.hash(flag);
            }

        }

    }
}
