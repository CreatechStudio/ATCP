package indi.atatc.atcp_client.packages.data;

import indi.atatc.atcp_client.Process;
import indi.atatc.atcp_client.packages.basics.Basics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class Values {
    private static final Values instance = new Values();
    private final ConcurrentHashMap<String, Object> values;

    private Values() {
        values = new ConcurrentHashMap<>();
        put("log_path", new File("log.txt"));
        put("key_length", 2048);
        put("separator_first_grade", "\\\\\\");
        put("separator_second_grade", "@");
        put("separator_third_grade", "#");
        put("separator_flag", ":");
    }

    public static Values getInstance() {
        return instance;
    }

    public void put(String name, Object value) {
        values.put(name, value);
    }

    public Object get(String name) {
        if (!values.containsKey(name)) {
            throw new Basics.AccidentEvents.StatusError("no such attribute: ", name);
        }
        return values.get(name);
    }
}
