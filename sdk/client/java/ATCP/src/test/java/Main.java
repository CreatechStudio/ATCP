import indi.atatc.atcp_client.Client;
import indi.atatc.atcp_client.Connection;
import indi.atatc.atcp_client.Flags;
import indi.atatc.atcp_client.Process;
import indi.atatc.atcp_client.packages.basics.Basics;
import indi.atatc.atcp_client.packages.data.ID;
import indi.atatc.atcp_client.packages.log.Log;

import java.sql.Time;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Log.getInstance().debugLevel = Log.DebugLevel.FATAL;
        Client.Configuration configuration = new Client.Configuration();
        configuration.port = 2000;
        Client client = new Client(configuration) {
            @Override
            protected Process onConnected(Connection connection) {
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
                        System.out.println(msg);
                        return msg;
                    }
                };
            }
        };
        client.start();
        client.send("dump");
        client.send("test@1");
        Thread.sleep(5000);
        client.stop();
    }
}
