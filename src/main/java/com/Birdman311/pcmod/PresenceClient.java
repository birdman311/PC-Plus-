package com.Birdman311.pcmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PresenceClient {

    private static final String BACKEND_URL = "https://pc-plus-player-backend.onrender.com";

    private static final int HEARTBEAT_SECONDS = 30;

    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> task;
    private static String currentServer;
    private static volatile boolean loggedResult = false;
    private static volatile UUID selfUuid;

    public static void start(String serverAddress) {
        stop();
        if (BACKEND_URL.contains("YOUR-BACKEND-URL")) {
            System.out.println("[PCMod] Presence disabled: backend URL is not set.");
            return;
        }
        if (serverAddress == null || serverAddress.isEmpty()) return;

        currentServer = serverAddress.toLowerCase();
        loggedResult = false;
        System.out.println("[PCMod] Presence started for server: " + currentServer);
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PCPlus-Presence");
            t.setDaemon(true);
            return t;
        });
        task = executor.scheduleAtFixedRate(PresenceClient::beat, 0, HEARTBEAT_SECONDS, TimeUnit.SECONDS);
    }

    public static void stop() {
        final String server = currentServer;
        final UUID id = selfUuid;

        if (task != null) {
            task.cancel(false);
            task = null;
        }
        if (executor != null) {
            if (server != null && id != null && !BACKEND_URL.contains("YOUR-BACKEND-URL")) {
                try {
                    executor.submit(() -> sendLeave(id, server));
                } catch (Exception ignored) {}
            }
            executor.shutdown();
            executor = null;
        }
        currentServer = null;
        selfUuid = null;
        ModUserTracker.clear();
    }

    private static void sendLeave(UUID id, String server) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("uuid", id.toString());
            body.addProperty("server", server);
            post(BACKEND_URL + "/leave", body.toString());
        } catch (Exception ignored) {}
    }

    public static String resolveServerKey() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null && mc.getConnection().getConnection() != null) {
                SocketAddress sa = mc.getConnection().getConnection().getRemoteAddress();
                if (sa instanceof InetSocketAddress) {
                    InetSocketAddress isa = (InetSocketAddress) sa;
                    if (isa.getAddress() != null) {
                        return isa.getAddress().getHostAddress() + ":" + isa.getPort();
                    }
                    return isa.getHostString().toLowerCase() + ":" + isa.getPort();
                }
                if (sa != null) {
                    return sa.toString();
                }
            }
            if (mc.getCurrentServer() != null) {
                return mc.getCurrentServer().ip;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static void beat() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || currentServer == null) return;

            UUID uuid = mc.player.getUUID();
            selfUuid = uuid;
            String name = mc.player.getGameProfile().getName();

            String serverName = currentServer;
            if (mc.getCurrentServer() != null && mc.getCurrentServer().ip != null) {
                serverName = mc.getCurrentServer().ip;
            }

            JsonObject body = new JsonObject();
            body.addProperty("uuid", uuid.toString());
            body.addProperty("name", name);
            body.addProperty("server", currentServer);
            body.addProperty("serverName", serverName);

            String response = post(BACKEND_URL + "/heartbeat", body.toString());
            if (response == null) {
                if (!loggedResult) {
                    loggedResult = true;
                    System.out.println("[PCMod] Presence: could not reach backend at " + BACKEND_URL);
                }
                return;
            }

            JsonObject json = new JsonParser().parse(response).getAsJsonObject();
            Set<UUID> users = new HashSet<>();
            if (json.has("users")) {
                JsonArray arr = json.getAsJsonArray("users");
                for (JsonElement el : arr) {
                    try {
                        users.add(UUID.fromString(el.getAsString()));
                    } catch (Exception ignored) {}
                }
            }
            ModUserTracker.setModUsers(users);
            if (!loggedResult) {
                loggedResult = true;
                System.out.println("[PCMod] Presence: connected. " + users.size() + " mod user(s) on this server.");
            }
        } catch (Exception ignored) {
        }
    }

    private static String post(String urlStr, String json) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            byte[] out = json.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
            }

            int code = conn.getResponseCode();
            if (code != 200) return null;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
