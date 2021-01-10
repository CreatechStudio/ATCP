import indi.atatc.aknn.AKNN;
import indi.atatc.aknn.Item;
import indi.atatc.atcp_server.*;
import indi.atatc.atcp_server.Process;
import indi.atatc.packages.basics.Basics;
import indi.atatc.packages.data.ID;
import indi.atatc.packages.log.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main {
    static class MyItem extends Item {
        public int a, b, c;
        public String name;

        public MyItem(String name, int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.name = name;
        }

        @Override
        protected int getDimension() {
            return 3;
        }

        @Override
        public double getCoordinate(int coordinateAxis) {
            switch (coordinateAxis) {
                case 0 -> {
                    return a;
                }
                case 1 -> {
                    return b;
                }
                case 2 -> {
                    return c;
                }
                default -> {
                    return 0;
                }
            }
        }
    }
    public static void main(String[] args) {
        // Todo: ATCP testing area
        Server.Configuration configuration = new Server.Configuration();
        configuration.name = "ATCP Official Test";
        configuration.port = 2000;
        configuration.project = "ATCP";
        configuration.type = "official";
        Basics.ContainerClass.Flag flag = new Basics.ContainerClass.Flag("flag text");
        Log.getInstance().debugLevel = Log.DebugLevel.FATAL;
        Server server = new Server(configuration) {
            private int times = 0;

            @Override
            public Process onConnected(Connection connection) {
                times ++;
                if (times == 20) {
                    stop();
                }
                return new Process(this, connection) {
                    @Override
                    protected void onSent(ID.MID mid) {

                    }

                    @Override
                    protected void onRecved(ID.MID mid, String result, Flags flags) {
                        Log.getInstance().publish(result, Log.DebugLevel.INFO);
                    }

                    @Override
                    protected String process(String msg, Flags flags) {
                        Log.getInstance().publish(msg, Log.DebugLevel.INFO);

                        String[] msgInParas = Basics.TextClass.split(msg, "@");
                        Log.getInstance().publish(Arrays.toString(msgInParas), Log.DebugLevel.DEBUG);
                        switch (msgInParas[0]) {
                            case "test":
                                return "test recved: " + msgInParas[1];
                            case "dump":
                                return "OK";
                        }
                        return "recved msg: " + msg;

                    }
                };
            }
        };
        LoopListener loopListener = new LoopListener() {
            @Override
            public boolean when() {
                return false;
            }

            @Override
            public void run() {

            }
        };
        server.addLoopListener(new LoopListener() {
            @Override
            public boolean when() {
                return false;
            }

            @Override
            public void run() {

            }
        });
        server.start(Server.Mode.T);

        // ToDo: profiling area
        /*
        Basics.StructureClass.List<Integer> list = new Basics.StructureClass.List<>();
        LinkedList<Integer> linkedList = new LinkedList<>();

        // 顺序写入10万
        System.out.println("顺序写入10万");
        System.out.println(Log.Time.calculateDuration(() -> {
            for (int i = 0; i < 100000; i++) {
                list.add(i);
            }
        }, Log.Time.Unit.Millisecond));

        System.out.println(Log.Time.calculateDuration(() -> {
            for (int i = 0; i < 100000; i++) {
                linkedList.add(i);
            }
        }, Log.Time.Unit.Millisecond));

        // 随机读取10万个
        System.out.println("随机读取10万");
        System.out.println(Log.Time.calculateDuration(() -> {
            for (int i = 0; i < 100000; i++) {
                list.get(i);
            }
        }, Log.Time.Unit.Millisecond));

        System.out.println(Log.Time.calculateDuration(() -> {
            for (int i = 0; i < 100000; i++) {
                linkedList.get(i);
            }

        }, Log.Time.Unit.Millisecond));

        // 随机删除1个
        System.out.println("随机删除1个");
        System.out.println(Log.Time.calculateDuration(() -> list.remove(4532), Log.Time.Unit.Millisecond));

        System.out.println(Log.Time.calculateDuration(() -> linkedList.remove(4532), Log.Time.Unit.Millisecond));

        // 顺序删除99991个
        Basics.ContainerClass.Range range = new Basics.ContainerClass.Range(0, 99990);
        System.out.println(Log.Time.calculateDuration(() -> list.remove(range), Log.Time.Unit.Millisecond));


        // 顺序删除全部
        System.out.println("顺序删除全部");
        System.out.println(Log.Time.calculateDuration(() -> {
            for (int i = 0; i < 99999; i++) {
                list.removeFirst();
            }
        }, Log.Time.Unit.Millisecond));
        System.out.println(Log.Time.calculateDuration(() -> {
            for (int i = 0; i < 99999; i++) {
                linkedList.removeFirst();
            }
        }, Log.Time.Unit.Millisecond));

        System.out.println(list.length());
        */

        // ToDo: middle node problems debugging area
        /*
        Basics.StructureClass.List<Integer> list = new Basics.StructureClass.List<>();
        list.add(1, 2, 3, 4, 5, 6, 7);
        System.out.println(
                ((Basics.StructureClass.List.Node) list.checkElement("node at", "2")).getIndex()
        );
        list.remove(3);
        System.out.println(Arrays.toString(list.toArray()));
        System.out.println(
                ((Basics.StructureClass.List.Node) list.checkElement("mid node")).getIndex()
        );

         */
    }
}
