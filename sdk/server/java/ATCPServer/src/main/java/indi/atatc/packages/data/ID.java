package indi.atatc.packages.data;

import indi.atatc.packages.basics.Basics;

import java.util.Objects;

public class ID {
    public static final class IP {
        private final String address;
        private final String[] paras;
        private final boolean local, area;

        public IP(String ip) {
            this.address = ip;
            paras = Basics.TextClass.split(ip, '.');
            if (paras.length > 1) {
                local = ip.equals("127.0.0.1");
                area = local || (paras[0] + '.' + paras[1]).equals("192.168");
            } else {
                local = ip.equals("localhost");
                area = local;
            }
        }

        public boolean isLocal() {
            return local;
        }

        public boolean isArea() {
            return area;
        }

        public String[] toArray() {
            return paras;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IP ip = (IP) o;
            return Objects.equals(address, ip.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address);
        }

        @Override
        public String toString() {
            return address;
        }
    }

    public static final class LID {
        private final String id;

        public LID(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LID lid = (LID) o;
            return Objects.equals(id, lid.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return id;
        }

    }

    public static final class MID {
        private final String id;

        public MID(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MID mid = (MID) o;
            return Objects.equals(id, mid.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    public static final class PID {
        private final String id;

        public PID(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PID pid = (PID) o;
            return Objects.equals(id, pid.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    public static final class IDsManager {
        private static final IDsManager instance = new IDsManager();
        private int maxLID, maxPID, maxMID;

        private IDsManager() {
            maxLID = maxPID = 0;
            maxMID = -1;
        }

        public static IDsManager getInstance() {
            return instance;
        }

        public LID newLID() {
            maxLID++;
            String lid = String.valueOf(maxLID);
            return new LID(lid);
        }

        public PID newPID() {
            maxPID++;
            String pid = String.valueOf(maxPID);
            return new PID(pid);
        }

        public MID newMID() {
            maxMID++;
            return new MID(String.valueOf(maxMID));
        }
    }
}
