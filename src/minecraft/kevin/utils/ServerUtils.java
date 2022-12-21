package kevin.utils;

import kotlin.Pair;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public final class ServerUtils extends MinecraftInstance {

    public static ServerData serverData;

    public static void connectToLastServer() {
        if(serverData == null)
            return;

        mc.displayGuiScreen(new GuiConnecting(new GuiMultiplayer(new GuiMainMenu()), mc, serverData));
    }

    public static String getRemoteIp() {
        String serverIp = "Singleplayer";

        if (mc.theWorld.isRemote) {
            final ServerData serverData = mc.getCurrentServerData();

            if(serverData != null)
                serverIp = serverData.serverIP;
        }

        return serverIp;
    }
    public static Pair<String, Integer> sendGet(String url) {
        return sendGet(url, null);
    }
    public static Pair<String, Integer> sendGet(String url, Proxy proxy) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        int conResult = 0;
        try {
            URL realUrl = new URL(url);
            URLConnection connection;
            if (proxy == null) connection = realUrl.openConnection();
            else connection = realUrl.openConnection(proxy);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            conResult = 1;
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                conResult = 2;
            }
        }
        return new Pair<>(result.toString(), conResult);
    }
}