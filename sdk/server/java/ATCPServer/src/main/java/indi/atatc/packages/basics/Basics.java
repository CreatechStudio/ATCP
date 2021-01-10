package indi.atatc.packages.basics;

import indi.atatc.packages.data.ID;
import indi.atatc.packages.data.Values;
import indi.atatc.packages.log.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;

public class Basics {
    public static class NativeHandler {
        public static final String SYSTEM_SEPARATOR = System.getProperty("line.separator");
        public static final Runtime runtime = Runtime.getRuntime();
        public static final Properties properties = System.getProperties();

        public static Object getHardwareStatus(String tar) {
            return switch (tar) {
                case "system_name" -> properties.get("os.name");
                case "system_architecture" -> properties.get("os.arch");
                case "cpu_cores" -> runtime.availableProcessors();
                case "mem_all" -> runtime.maxMemory();
                case "mem_free" -> runtime.freeMemory();
                default -> null;
            };
        }

        public static Object[] getHardwareStatus(String... tars) {
            Object[] res = new Object[tars.length];
            int i = 0;
            for (String t : tars) {
                res[i] = getHardwareStatus(t);
                i++;
            }
            return res;
        }
    }

    public static class AccidentEvents {
        public static class AccidentEvent extends RuntimeException {
            private final String[] args;

            public AccidentEvent(String... args) {
                this.args = args;
            }

            protected void recordCrime() {
                if (args.length > 0) {
                    ID.IP ip = new ID.IP(args[0]);
                }
            }

            @Override
            public String toString() {
                StringBuilder string = new StringBuilder(this.getClass().toString() + "{");
                for (String arg : args) {
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
            Log.getInstance().publish(msg.substring(0, msg.length() - separator.length()), Log.DebugLevel.INFO);
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
        public static String getSHA256(String str) {
            MessageDigest messageDigest;
            String encodestr = "";
            try {
                messageDigest = MessageDigest.getInstance("SHA-256");
                messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
                encodestr = byte2hex(messageDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return encodestr;
        }

        private static String byte2hex(byte[] bytes) {
            StringBuilder stringBuilder = new StringBuilder();
            String temp;
            for (byte aByte : bytes) {
                temp = Integer.toHexString(aByte & 0xFF);
                if (temp.length() == 1) {
                    stringBuilder.append("0");
                }
                stringBuilder.append(temp);
            }
            return stringBuilder.toString();
        }

        public static String[] split(String content, String separator) {
            if (content.isEmpty()) return new String[]{""};
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
        public static abstract class Structure {
        }

        public static class Concurrent {
            public static class ConcurrentStructure {
            }

            public static class ConcurrentList extends ConcurrentStructure {
            }
        }

        public static abstract class Listener extends Structure {
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

        // Fixme: middle node offset problems
        public static class Queue<E> extends Structure {
            private Object[] queue;
            private int usedLength;

            public Queue() {
                queue = new Object[5];
                usedLength = 0;
            }

            public Queue(E[] origin) {
                queue = origin;
                usedLength = origin.length;
            }

            private void dilatation() {
                /*
                Object[] oldQueue = queue.clone();
                queue = new Object[(int) (queue.length * 1.5)];
                System.arraycopy(oldQueue, 0, queue, 0, oldQueue.length);
                 */
                queue = Arrays.copyOf(queue, (int) (queue.length * 1.5));
            }

            @SuppressWarnings("unchecked")
            public E get() {
                if (queue.length == 0) {
                    return null;
                }
                return (E) queue[0];
            }
            public void add(E element) {
                if (queue.length <= usedLength) {
                    dilatation();
                }
                queue[usedLength] = element;
                usedLength++;
            }

            @SafeVarargs
            public final void add(E... elements) {
                for (E element : elements) {
                    add(element);
                }
            }

            public int length() {
                return usedLength;
            }

            public void remove() {
                Object[] oldQueue = queue.clone();
                queue = new Object[queue.length - 1];
                System.arraycopy(oldQueue, 1, queue, 0, oldQueue.length - 1);
                usedLength--;
            }

            @SuppressWarnings("unchecked")
            public void forEach(Consumer<? super E> action) {
                for (Object element : queue) {
                    action.accept((E) element);
                }
            }

            @SuppressWarnings("unchecked")
            public E[] toArray() {
                Object[] array = new Object[length()];
                System.arraycopy(queue, 0, array, 0, length());
                return (E[]) array;
            }
        }

        public static class List<E> extends Structure {
            public final class Node {
                private boolean base, mid, linkRelay = false;
                private int index = -1;
                public final E value;
                public Node last, next, jumpBack, jumpForward = null;

                public Node(E value) {
                    base = true;
                    index = 0;
                    this.value = value;
                }

                public Node(Node last, E value) {
                    this.value = value;
                    this.last = last;
                }

                public int getIndex() {
                    if (index != -1) {
                        return index;
                    }
                    return last.getIndex() + 1;
                }

                void knownIndex(int index) {
                    mid = true;
                    this.index = index;
                }

                void indexOutdated() {
                    mid = false;
                    index = -1;
                }

                public void insertBefore(Node node) {
                    if (base) {
                        base = false;
                        node.base = true;
                        node.last = null;
                        baseNode = node;
                    } else {
                        node.base = false;
                        node.last = last;
                        last.next = node;
                    }
                    node.next = this;
                    last = node;
                }

                public void insertAfter(Node node) {
                    node.base = false;
                    if (next == null) {
                        node.next = null;
                        lastNode = node;
                    } else {
                        node.next = next;
                        next.last = node;
                    }
                    node.last = this;
                    next = node;
                }
            }

            private enum IndexType {
                F, S, T, L
            }

            protected Node baseNode, lastNode, midNode = null;
            protected int maxIndex = -1; // the last index
            protected boolean odd, reversed = false;

            public Object checkElement(String... args) {
                switch (args[0]) {
                    case "mid node" -> {
                        return midNode;
                    }
                    case "node at" -> {
                        return getNodeAt(Integer.parseInt(args[1]));
                    }
                    default -> {
                        return null;
                    }
                }
            }

            public List() {
            }

            public final int length() {
                return maxIndex + 1;
            }

            public final int size() {
                return maxIndex + 1;
            }

            private IndexType classifyIndex(int index) {
                if (index <= maxIndex / 4 || maxIndex < 2) return IndexType.F;
                if (index <= midNode.getIndex()) return IndexType.S;
                if (index <= maxIndex - maxIndex / 4) return IndexType.T;
                return IndexType.L;
            }

            private Node getNodeAt(int index) {
                Node node;
                int t;
                switch (classifyIndex(index)) {
                    case F -> {
                        node = baseNode;
                        for (t = 0; t < index; t++) {
                            node = node.next;
                        }
                    }
                    case S -> {
                        node = midNode;
                        for (t = midNode.getIndex(); t < index; t--) {
                            node = node.last;
                        }
                    }
                    case T -> {
                        node = midNode;
                        for (t = midNode.getIndex(); t < index; t++) {
                            node = node.next;
                        }
                    }
                    default -> {
                        node = lastNode;
                        for (t = maxIndex; t > index; t--) {
                            node = node.last;
                        }
                    }
                }
                return node;
            }

            public final E get(int index) {
                if (index > maxIndex) {
                    return null;
                }
                return getNodeAt(index).value;
            }

            public final int indexOf(E element) {
                Node pN = baseNode;
                Node nN = lastNode;
                for (int t = 0; t <= maxIndex; t++) {
                    if (pN.value.equals(element)) {
                        return t;
                    }
                    if (nN.value.equals(element)) {
                        return nN.getIndex();
                    }

                    pN = pN.next;
                    nN = nN.last;
                }
                return -1;
            }

            public final void add(E element) {
                if (baseNode == null) {
                    lastNode = baseNode = new Node(element);
                } else {
                    Node node = new Node(lastNode, element);
                    lastNode.insertAfter(node);
                }
                maxIndex++;
                odd = !odd;
                if (maxIndex == 2) {
                    midNode = baseNode.next;
                    midNode.knownIndex(maxIndex / 2);
                }
                if (midNode != null && odd) {
                    midNode.indexOutdated();
                    midNode = midNode.next;
                    midNode.knownIndex(maxIndex / 2);
                }
            }

            @SafeVarargs
            public final void add(E... elements) {
                for (E element : elements) {
                    add(element);
                }
            }

            public final void insertAfter(int index, E element) {
                getNodeAt(index).insertAfter(new Node(element));
                maxIndex++;
            }

            public final void insertBefore(int index, E element) {
                getNodeAt(index).insertBefore(new Node(element));
                maxIndex++;
            }

            public final void reverse() {
                reversed = true;
            }

            /*
            public final void subList(int fromIndex) {
                List<E> list = new List<>();
                Node node = lastNode;
                for (int t = maxIndex; t > fromIndex; t--) {
                    list
                }
            }

             */
            public final void subList(int fromIndex, int toIndex) {

            }

            private void remove(Node node) {
                if (node.base && node.next == null) {
                    lastNode = baseNode = null;
                } else if (node.base) {
                    node.next.last = null;
                    node.next.base = true;
                    baseNode = node.next;
                } else if (node.next == null) {
                    node.last.next = null;
                    lastNode = node.last;
                } else {
                    node.last.next = node.next;
                    node.next.last = node.last;
                }
                maxIndex--;
                odd = !odd;
                if (node.mid || odd) {
                    midNode = midNode.last;
                }
            }

            public final void removeFirst() {
                if (maxIndex == 0) {
                    removeAll();
                    return;
                }
                baseNode.next.last = null;
                baseNode.next.base = true;
                baseNode = baseNode.next;
                maxIndex--;
                odd = !odd;
                if (midNode != null && odd) {
                    if (maxIndex == 2) midNode = null;
                    else midNode = midNode.last;
                }
            }

            public final void removeLast() {
                if (maxIndex == 0) {
                    removeAll();
                    return;
                }
                lastNode.last.next = null;
                lastNode = lastNode.last;
                maxIndex--;
                odd = !odd;
                if (midNode != null && odd) {
                    if (maxIndex == 2) midNode = null;
                    else midNode = midNode.last;
                }
            }

            public final void remove(int index) {
                if (index > maxIndex) {
                    return;
                }
                Node node = getNodeAt(index);
                remove(node);
            }

            public final void remove(ContainerClass.Range indexRange) {
                if ((checkRange(indexRange)) || indexRange.length() == 0) {
                    return;
                }
                Node node = getNodeAt(indexRange.get());
                remove(node);
                int lastIndex = indexRange.getThenRemove();
                int currentIndex = indexRange.getThenRemove();
                for (; ; ) {
                    node = node.next;
                    lastIndex++;
                    if (lastIndex != currentIndex) {
                        continue;
                    }
                    remove(node);
                    if (indexRange.get() == null) {
                        break;
                    }
                    currentIndex = indexRange.getThenRemove();
                }
            }

            public final void removeAll() {
                midNode = lastNode = baseNode = null;
                maxIndex = -1;
                odd = false;
            }

            public final void forEach(Consumer<? super E> action) {
                Node node = baseNode;
                for (int t = 0; t <= maxIndex; t++) {
                    action.accept(node.value);
                    node = node.next;
                }
            }

            public final void forEach(ContainerClass.Range indexRange, Consumer<? super E> action) {
                if (checkRange(indexRange)) {
                    return;
                }
                Node node = getNodeAt(indexRange.get());
                action.accept(node.value);
                int lastIndex = indexRange.getThenRemove();
                int currentIndex = indexRange.getThenRemove();
                for (; ; ) {
                    node = node.next;
                    lastIndex++;
                    if (lastIndex != currentIndex) {
                        continue;
                    }
                    action.accept(node.value);
                    if (indexRange.get() == null) {
                        break;
                    }
                    currentIndex = indexRange.getThenRemove();
                }
            }

            // ToDo
            public String toImage() {
                return "";
            }

            /**
             * @param range: the Range object
             * @return true: not ok
             * false: ok
             */
            private boolean checkRange(ContainerClass.Range range) {
                return range.start() < 0 || range.ending() > maxIndex;
            }

            public final ContainerClass.Range rangeOf(int startIndex) {
                if (startIndex < 0) {
                    return null;
                }
                return new ContainerClass.Range(startIndex, maxIndex + 1);
            }

            public final ContainerClass.Range rangeOf(int startIndex, int endIndex) {
                if (startIndex < 0 || endIndex > maxIndex) {
                    return null;
                }
                return new ContainerClass.Range(startIndex, endIndex);
            }

            @SuppressWarnings("unchecked")
            public final E[] toArray() {
                Object[] array = new Object[(int) (maxIndex + 1)];
                if (maxIndex == -1) {
                    return (E[]) array;
                }
                Node node = baseNode;
                for (int t = 0; t < maxIndex; t++) {
                    array[t] = node.value;
                    node = node.next;
                }
                array[maxIndex] = node.value;
                return (E[]) array;
            }

            @SuppressWarnings("unchecked")
            public final E[] toArray(ContainerClass.Range indexRange) {
                if (checkRange(indexRange)) {
                    return null;
                }
                Object[] array = new Object[indexRange.length()];
                if (indexRange.length() == 0) {
                    return (E[]) array;
                }
                Node node = getNodeAt(indexRange.get());
                array[0] = node.value;
                int i = 1;
                int lastIndex = indexRange.getThenRemove();
                int currentIndex = indexRange.getThenRemove();
                for (; ; ) {
                    node = node.next;
                    lastIndex++;
                    if (lastIndex != currentIndex) {
                        continue;
                    }
                    array[i] = node.value;
                    i++;
                    if (indexRange.get() == null) {
                        break;
                    }
                    currentIndex = indexRange.getThenRemove();
                }
                return (E[]) array;
            }
        }

        @SuppressWarnings("unchecked")
        public static class HashMap<K, V> extends Structure {

        }
    }

    public static class ContainerClass {
        public static final class Range {
            public interface Rule {
                boolean pass(int index);
            }

            protected final StructureClass.Queue<Integer> range = new StructureClass.Queue<>();
            protected final int n;

            public Range(int startIndex, int endIndex) {
                if (startIndex > endIndex) {
                    throw new IndexOutOfBoundsException("not a range");
                }
                for (int i = startIndex; i < endIndex; i++) {
                    range.add(i);
                }
                n = endIndex - 1;
            }

            public Range(int startIndex, int endIndex, Rule rule) {
                if (startIndex > endIndex) {
                    throw new IndexOutOfBoundsException("not a range");
                }
                int n = endIndex - 1;
                for (int i = startIndex; i < endIndex; i++) {
                    if (rule.pass(i)) {
                        range.add(i);
                        n = i;
                    }
                }
                this.n = n;
            }

            public int length() {
                return range.length();
            }

            public int start() {
                return range.get();
            }

            public int ending() {
                return n;
            }

            public boolean startsWith(int index) {
                return start() == index;
            }

            public boolean endsWith(int index) {
                return ending() == index;
            }

            public Integer get() {
                return range.get();
            }

            public int getThenRemove() {
                try {
                    return get();
                } finally {
                    remove();
                }
            }

            public void remove() {
                range.remove();
            }

            public Integer[] toArray() {
                return range.toArray();
            }
        }

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

            public String getFlag() {
                return flag;
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
