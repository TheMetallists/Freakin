package aq.metallists.freundschaft.tools;

import android.os.StrictMode;

import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkTool {
    public static String getBestServer(String serversCS, int port) {
        if (!serversCS.contains(";")) {
            return serversCS;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String[] servers = serversCS.split(";");
        String bestSV = servers[0];
        int bestResult = 5000;

        for (String server : servers) {
            try {
                Logger.getInstance().i("Testing server: ".concat(server));
                long before = System.currentTimeMillis();
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(server, port), 1000);
                socket.close();
                int total = (int) (System.currentTimeMillis() - before);
                Logger.getInstance().i(
                        "Testing server: ".concat(server).concat(" time: ").concat(Integer.toString(total))
                );
                if (total < bestResult) {
                    Logger.getInstance().i("And it is the new best!");
                    bestResult = total;
                    bestSV = server;
                }
            } catch (Exception x) {
                Logger.getInstance().w("hmm...", x);
            }
        }


        return bestSV;
    }
}
