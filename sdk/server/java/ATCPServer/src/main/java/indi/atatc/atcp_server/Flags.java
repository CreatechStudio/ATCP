package indi.atatc.atcp_server;

import indi.atatc.packages.basics.Basics;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Flags {
    final static Flag KILL = new Flag("\\kill\\", true);
    public final static Flag RESULT_NO_NEED = new Flag("\\res_no_need\\", true);
    final static Flag RESULT = new Flag("\\res\\", true);
    public final static Flag TO_PID = new Flag("\\to_pid\\", true);
    final static Flag FROM_PID = new Flag("\\from_pid\\", true);

    private final ConcurrentLinkedQueue<Basics.ContainerClass.Flag> flags = new ConcurrentLinkedQueue<>();

    public Flags(Basics.ContainerClass.Flag... flags) {
        this.flags.addAll(Arrays.asList(flags));
    }

    Flags(String... flags) {
        for (String flag : flags) {
            this.flags.add(Flag.readFlag(flag));
        }
    }

    public Flags() {
    }

    public boolean contains(Basics.ContainerClass.Flag flag) {
        return flags.contains(flag);
    }

    public void add(Basics.ContainerClass.Flag flag) {
        flags.add(flag);
    }

    public String valueOf(String flagName) {
        return valueOf(new Basics.ContainerClass.Flag(flagName));
    }
    public String valueOf(Basics.ContainerClass.Flag flag) {
        for (Basics.ContainerClass.Flag f: flags) {
            if (f.equals(flag)) {
                return f.getValue();
            }
        }
        return null;
    }

    public Basics.ContainerClass.Flag[] toArray() {
        return flags.toArray(Basics.ContainerClass.Flag[]::new);
    }

    Flags userFlags() {
        Flags flags = new Flags() {
            @Override
            Flags userFlags() {
                return this;
            }
        };
        for (Basics.ContainerClass.Flag f: this.flags) {
            if (!f.isSys() || f.equals(Flags.FROM_PID)) {
                flags.add(f);
            }
        }
        return flags;
    }

    @Override
    public String toString() {
        if (flags.size() == 0) {
            return "";
        }
        StringBuilder string = new StringBuilder();
        for (Basics.ContainerClass.Flag f : flags) {
            string.append(f.export());
            string.append("#");
        }
        return string.substring(0, string.length() - 1);
    }
}
